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

    private static final Logger LOG = LoggerFactory.getLogger(OldSensorTagReader.class);

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
        Process process = getShellProcess();
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
            expect.sendLine("disconnect");
            TimeUnit.SECONDS.sleep(1);

            expect.sendLine("exit"); // Exit from gattool
            TimeUnit.SECONDS.sleep(1);

            expect.sendLine("exit"); // Exit from terminal
            expect.expect(eof());

            process.waitFor();
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
        Process process = getShellProcess();

        try (Expect expect = getExpectBuilder(process).build()) {

            expect.sendLine("sudo hcitool con");
            Result result = expect.expect(regexp("handle (\\d+) state 1 lm MASTER"));
            String handle = result.group(1);

            /**
             * From http://processors.wiki.ti.com/images/4/4a/Sensor_Tag_and_BTool_Tutorial1.pdf
             *
             * 0x2A04 GAP_PERI_CONN_PARAM_UUID (Peripheral Preferred Connection Parameters)
             *
             * 50 00 (100ms preferred min connection interval)
             * A0 00 (200ms preferred max connection interval)
             * 00 00 (0 preferred slave latency)
             * E8 03 (10000ms preferred supervision timeout)
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
            expect.sendLine("sudo hcitool lecup --handle " + handle + " --min 80 --max 160 --latency 0 --timeout 1000");

            expect.sendLine("exit");
            expect.expect(eof());
        }
    }

    private Process getShellProcess() throws IOException {
        return Runtime.getRuntime().exec("/bin/bash");
    }

    private ExpectBuilder getExpectBuilder(Process process) throws IOException {
        return new ExpectBuilder()
                    .withOutput(process.getOutputStream())
                    .withInputs(process.getInputStream(), process.getErrorStream())
//                    .withEchoOutput(System.out)
//                    .withEchoInput(System.out)
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

        Result result = expect.expect(regexp("Notification handle = 0x0025 value: (?!00 00 00 00)(\\w{2} \\w{2} \\w{2} \\w{2})"));

        if (result.isSuccessful()) {
            String value = result.group(1);
            expect.sendLine("char-write-cmd 0x29 00"); // Disable temperature sensor

            return value;
        } else {
            throw new SensortagException("Failed to get temperature. " + result.getInput());
        }
    }

    private String readHumidity(Expect expect) throws IOException {
        expect.sendLine("char-write-cmd 0x3f 01"); // Enable humidity sensor

        Result result = expect.expect(regexp("Notification handle = 0x003b value: (?!00 00 00 00)(\\w{2} \\w{2} \\w{2} \\w{2})"));
        String value = result.group(1);
        expect.sendLine("char-write-cmd 0x3f 00"); // Disable humidity sensor

        return value;
    }
}
