package nl.wiegman.sensortag;

import net.sf.expectit.Expect;
import net.sf.expectit.ExpectBuilder;
import net.sf.expectit.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;

import static net.sf.expectit.filter.Filters.removeColors;
import static net.sf.expectit.filter.Filters.removeNonPrintable;
import static net.sf.expectit.matcher.Matchers.*;

@Component
public class SensorTagReader {

    private static final Logger LOG = LoggerFactory.getLogger(SensorTagReader.class);

    @Autowired
    private KlimaatReadingPersister klimaatReadingPersister;

    @Value("${sensortag.bluetooth.address}")
    private String sensortagBluetoothAddress;

    @Value("${sensortag.probetime.seconds}")
    private int sensortagProbeTimeInSeconds;

    @PostConstruct
    private void reconnectToSensorTagOnException() throws InterruptedException, IOException {
        LOG.info("Starting SensorTagReader");

        while (1 == 1) {
            try {
                connectAndListenForSensorValues();
            } catch (SensortagException e) {
                LOG.error("Error occurred: " + e.getMessage());
                LOG.error("Trying to reconnect in 10 seconds...");
                TimeUnit.SECONDS.sleep(10);
            }
        }
    }

    private void connectAndListenForSensorValues() throws IOException, InterruptedException, SensortagException {
        Process process = createShellProcess();
        Expect expect = getExpectBuilder(process).build();

        try {
            connectToSensortag(expect);
            setConnectionParameters();

            /**
             * From the TI Sensortag Guide:
             *
             * The most power efficient way to obtain measurements for a sensor is to
             * 1. Enable notification
             * 2. Enable Sensor
             * 3. When notification with data is obtained at the Master side, disable the sensor (notification still on though)
             */

            enableNotifications(expect);

            while (1 == 1) {
                readAndPersistValues(expect);
                TimeUnit.SECONDS.sleep(sensortagProbeTimeInSeconds);
            }

        } finally {
            disconnect(process, expect);
        }
    }

    private void readAndPersistValues(Expect expect) throws IOException, SensortagException {
        String temperatureHex = readTemperature(expect);
//        String humidityHex = readHumidity(expect);

        BigDecimal temperature = BigDecimal.valueOf(ThermometerGatt.ambientTemperatureFromHex(temperatureHex));
//        BigDecimal humidity = BigDecimal.valueOf(HygrometerGatt.humidityFromHex(humidityHex));

        klimaatReadingPersister.persist(temperature, null);
    }

    private void setConnectionParameters() throws IOException {
        Process process = createShellProcess();

        try (Expect expect = getExpectBuilder(process).build()) {

            String handle = getCurrentConnectionHandleId(expect);

            /**
             * From http://processors.wiki.ti.com/images/4/4a/Sensor_Tag_and_BTool_Tutorial1.pdf:
             *
             * 0x2A04 GAP_PERI_CONN_PARAM_UUID (Peripheral Preferred Connection Parameters)
             *
             * 50 00 (100ms preferred min connection interval)
             * A0 00 (200ms preferred max connection interval)
             * 00 00 (0 preferred slave latency)
             * E8 03 (10000ms preferred supervision timeout)
             *
             * The connection parameters for a BLE connection is a set of parameters that determine when and how the Central and a Peripheral in a link transmits data.
             * It is always the Central that actually sets the connection parameters used, but the Peripheral can send a so-called Connection Parameter Update Request,
             * that the Central can then accept or reject.
             *
             * -------------------------------------------------------------------------------------
             *
             * There are multiple connection parameters (defined in Bluetooth 4.0 specification, Volume 3, Part A, Section 4.20) that will determine the throughput.
             * Keep in mind that higher throughput will result in more power consumption.
             *
             * Connection interval:
             * This defines how often that the central communicates with the peripheral.
             * There can be a maximum of four packets sent per connection interval, and each packet can have up to 20B of payload.
             * According to the BLE specification, the allowable range for connection parameters is from 7.5mSec to 4000mSec.
             * The Connection Interval is the parameter that most affects data rate. Think of the Connection Interval as the train schedule:
             * for example, trains leave the station every half hour. Each train can have 1-4 cars, and each car can hold 0 to 20 bytes.
             * So if you have a 20 ms Connection Interval, then the theoretical maximum for sending a message and receiving the ACK is
             * 80 bytes (of data) in 40 mS (one Connection Interval to send, plus one Interval to receive the ACK).
             * But trains leave whether full or not. So you might only send 20 bytes every 80 mS -- or less.
             *
             * Slave latency:
             * This is the number of connection intervals that the slave is allowed to skip. For example, if the connection interval is 20mSec and slave latency is 4,
             * then if the peripheral wants to then it only needs to answer the master every 80mSec.
             * Slave latency is useful if you want to mostly stay asleep, but then burst out data at a faster rate occasionally.
             * In this case, the peripheral only needs to respond to the master every 80mSec to keep the connection alive,
             * but if it has a lot of data then it can send data every 20mSec. Slave Latency is any value between 0 and 499,
             * though it cannot exceed: ((supervisionTimeout / connInterval) – 1)
             *
             * Connection supervision timeout:
             * The supervision timeout parameter specifies the maximum amount of time that either the master or slave can go before receiving a link-layer packet.
             * Both slave and master device maintain their own “Supervision timer”, which resets to zero every time a packet is received.
             * If supervision timer ever reaches the supervision timeout, the device considers the connection lost,
             * and exits the connection state (returning to the advertising, scanning, or standby state).
             * The Supervision Timeout is a multiple of 10ms in the range of 100ms and 32.0s. It Must be larger than (1 + slaveLatency) * (ConnInterval) where,
             *
             * -------------------------------------------------------------------------------------
             *
             * hcitool lecup <handle> <min> <max> <latency> <timeout>
             * Options:
             * -H, --handle <0xXXXX> LE connection handle
             * -m, --min <interval> Range: 0x0006 to 0x0C80
             * -M, --max <interval> Range: 0x0006 to 0x0C80
             * -l, --latency <range> Slave latency. Range: 0x0000 to 0x03E8
             * -t, --timeout <time> N * 10ms. Range: 0x000A to 0x0C80
             *
             * min/max range: 7.5ms to 4s. Multiply factor: 1.25ms
             * timeout range: 100ms to 32.0s. Larger than max interval
             */

            expect.sendLine("sudo hcitool lecup --handle " + handle + " --min 320 --max 640 --latency 0 --timeout 3000");

            expect.sendLine("exit");
            expect.expect(eof());
        }
    }

    private String getCurrentConnectionHandleId(Expect expect) throws IOException {
        expect.sendLine("sudo hcitool con");
        Result result = expect.expect(regexp("handle (\\d+) state 1 lm MASTER"));
        return result.group(1);
    }

    private Process createShellProcess() throws IOException {
        return Runtime.getRuntime().exec("/bin/bash");
    }

    private ExpectBuilder getExpectBuilder(Process process) throws IOException {
        return new ExpectBuilder()
                    .withOutput(process.getOutputStream())
                    .withInputs(process.getInputStream(), process.getErrorStream())
                    .withInputFilters(removeColors(), removeNonPrintable())
                    .withTimeout(15, TimeUnit.SECONDS);
    }

    private void connectToSensortag(Expect expect) throws IOException, SensortagException {
        expect.sendLine("gatttool -b " + sensortagBluetoothAddress + " --interactive");

        Result startGattoolResult = expect.expect(contains("[LE]>"));
        if (!startGattoolResult.isSuccessful()) {
            throw new SensortagException("Failed to start gatttool. " + startGattoolResult.getInput());
        }

        expect.sendLine("connect");

        Result connectResult = expect.expect(contains("Connection successful"));
        if (!connectResult.isSuccessful()) {
            throw new SensortagException("Failed to connect. " + connectResult.getInput());
        }
    }

    private void enableNotifications(Expect expect) throws IOException {
        expect.sendLine("char-write-cmd 0x26 0100"); // Enable temperature sensor notifications
        expect.sendLine("char-write-cmd 0x3c 0100"); // Enable humidity sensor notifications
    }

    private void disableNotifications(Expect expect) throws IOException {
        expect.sendLine("char-write-cmd 0x3c 0000"); // Disable humidity sensor notifications
        expect.sendLine("char-write-cmd 0x26 0000"); // Disable temperature sensor notifications
    }

    private String readTemperature(Expect expect) throws IOException, SensortagException {
        expect.sendLine("char-write-cmd 0x29 01"); // Enable temperature sensor

        String value = null;

        int i = 0;
        while (i < 3) {
            Result result = expectTemperatureNotification(expect, 10, TimeUnit.SECONDS);
            if (result.isSuccessful()) {
                value = result.group(1);
                LOG.debug("Temperature notification: " + ThermometerGatt.ambientTemperatureFromHex(value));
            } else {
                throw new SensortagException("Failed to get temperature. " + result.getInput());
            }
            i++;
        }

        expect.sendLine("char-write-cmd 0x29 00"); // Disable temperature sensor

        while(expectTemperatureNotification(expect, 3, TimeUnit.SECONDS).isSuccessful()) {
            LOG.debug("Discarding temperature notification: " + ThermometerGatt.ambientTemperatureFromHex(value));
        }
        return value;
    }

    private Result expectTemperatureNotification(Expect expect, long duration, TimeUnit timeUnit) throws IOException {
        return expect.withTimeout(duration, timeUnit).expect(regexp("Notification handle = 0x0025 value: (?!00 00 00 00)(\\w{2} \\w{2} \\w{2} \\w{2})"));
    }

    private String readHumidity(Expect expect) throws IOException {
        expect.sendLine("char-write-cmd 0x3f 01"); // Enable humidity sensor

        Result result = expect.expect(regexp("Notification handle = 0x003b value: (?!00 00 00 00)(\\w{2} \\w{2} \\w{2} \\w{2})"));
        String value = result.group(1);
        expect.sendLine("char-write-cmd 0x3f 00"); // Disable humidity sensor

        return value;
    }

    private void disconnect(Process process, Expect expect) throws IOException, InterruptedException {
        expect.sendLine("disconnect");
        TimeUnit.SECONDS.sleep(1);

        expect.sendLine("exit"); // Exit from gattool
        TimeUnit.SECONDS.sleep(1);

        expect.sendLine("exit"); // Exit from terminal
        expect.expect(eof());

        process.waitFor();
    }
}
