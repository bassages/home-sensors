package nl.homesensors.sensortag;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.sf.expectit.Expect;
import net.sf.expectit.ExpectBuilder;
import net.sf.expectit.Result;
import nl.homesensors.sensortag.publisher.ClimatePublisher;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static net.sf.expectit.filter.Filters.removeColors;
import static net.sf.expectit.filter.Filters.removeNonPrintable;
import static net.sf.expectit.matcher.Matchers.*;

/**
 * <p>
 * Connects to a Texas Instruments Sensortag over Bluetooth low energy to periodically
 * obtain the actual temperature and humidity.
 * </p>
 * <p>
 * The obtained values will be published to each {@link ClimatePublisher}.
 * </p>
 * <p>
 * Dependencies:
 * blueZ (gattool and hcitool) needs to be installed on the host OS (see <a href="http://www.bluez.org/">...</a>).
 * </p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SensorTagReader {

    private static final int NR_OF_SECONDS_TO_WAIT_BEFORE_ATTEMPT_RECONNECT = 10;

    private final List<ClimatePublisher> climatePublishers;

    private final SensortagConfig sensortagConfig;

    private final Thermometer thermometer = new Thermometer();
    private final Hygrometer hygrometer = new Hygrometer();

    @Async
    public void run() throws InterruptedException {
        if (!sensortagConfig.isComplete()) {
            log.warn("Not started SensorTagReader, because the configuration for it is not defined.");
            return;
        }
        log.info("Start SensorTagReader");
        foreverTryToConnectAndListenForSensorValues();
    }

    // SonarQube: "Loops should not be infinite".
    // Ignore because this is intentional.
    @SuppressWarnings("java:S2189")
    private void foreverTryToConnectAndListenForSensorValues() throws InterruptedException {
        //noinspection InfiniteLoopStatement
        while (true) {
            try {
                connectAndListenForSensorValues();
            } catch (final SensortagException | IOException e) {
                log.error("Error occurred: {}. Trying to reconnect in {} seconds...", e.getMessage(), NR_OF_SECONDS_TO_WAIT_BEFORE_ATTEMPT_RECONNECT);
                TimeUnit.MILLISECONDS.sleep(NR_OF_SECONDS_TO_WAIT_BEFORE_ATTEMPT_RECONNECT);
            }
        }
    }

    // SonarQube: "Loops should not be infinite".
    // Ignore because this is intentional.
    @SuppressWarnings("java:S2189")
    private void connectAndListenForSensorValues() throws IOException, InterruptedException, SensortagException {
        final Process process = createShellProcess();
        final Expect expect = getExpectBuilder(process).build();

        killAllGatttools(expect);

        try {
            startInteractiveGattool(expect);
            connect(expect);
            setConnectionParameters();

            /*
             * From the TI SensorTag Guide:
             *
             * The most power efficient way to obtain measurements for a sensor is to:
             * 1. Enable notification
             * 2. Enable Sensor
             * 3. When notification with data is obtained at the Master side, disable the sensor (notification still on though)
             */

            thermometer.enableNotifications(expect);
            hygrometer.enableNotifications(expect);

            //noinspection InfiniteLoopStatement
            while (true) {
                final long start = System.currentTimeMillis();
                readAndPersistSensorValues(expect);
                final long processingTime = System.currentTimeMillis() - start;

                final long sleepDurationInMilliseconds = (sensortagConfig.probetimeSeconds() * 1000) - processingTime;
                log.debug("Sleep for {} milliseconds", sleepDurationInMilliseconds);
                TimeUnit.MILLISECONDS.sleep(sleepDurationInMilliseconds);
            }

        } finally {
            disconnectAndClose(process, expect);
        }
    }

    private void startInteractiveGattool(final Expect expect) throws IOException, SensortagException {
        expect.sendLine("gatttool -b " + sensortagConfig.bluetoothAddress() + " --interactive");

        final Result startGattoolResult = expect.withTimeout(20, TimeUnit.SECONDS).expect(contains("[LE]>"));
        if (!startGattoolResult.isSuccessful()) {
            throw new SensortagException("Failed to start gatttool. " + startGattoolResult.getInput());
        }
    }

    private void connect(final Expect expect) throws IOException, SensortagException {
        expect.sendLine("connect");

        final Result connectResult = expect.withTimeout(20, TimeUnit.SECONDS).expect(contains("Connection successful"));
        if (!connectResult.isSuccessful()) {
            throw new SensortagException("Failed to connect. " + connectResult.getInput());
        }
    }

    private void readAndPersistSensorValues(final Expect expect) throws IOException, SensortagException {
        final Temperature temperature = thermometer.getAmbientTemperature(expect);
        final Humidity humidity = hygrometer.getHumidity(expect);
        climatePublishers.forEach(climatePublisher ->
                climatePublisher.publish(SensorCode.of(sensortagConfig.climateSensorCode()), temperature, humidity));
    }

    private void setConnectionParameters() throws IOException {
        final Process process = createShellProcess();

        try (final Expect expect = getExpectBuilder(process).build()) {

            final String handle = getCurrentConnectionHandleId(expect);

            /*
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

            expect.sendLine("sudo hcitool lecup --handle " + handle + " --min 320 --max 640 --latency 0 --timeout 3200");

            expect.sendLine("exit");
            expect.expect(eof());
        }
    }

    private void killAllGatttools(final Expect expect) throws IOException {
        expect.sendLine("pkill -f gatttool");
    }

    private String getCurrentConnectionHandleId(final Expect expect) throws IOException {
        expect.sendLine("sudo hcitool con");
        final Result result = expect.expect(regexp("handle (\\d+) state 1 lm MASTER"));
        return result.group(1);
    }

    private Process createShellProcess() throws IOException {
        return Runtime.getRuntime().exec(new String[]{"/bin/bash"});
    }

    private ExpectBuilder getExpectBuilder(final Process process) {
        return new ExpectBuilder()
                    .withOutput(process.getOutputStream())
                    .withInputs(process.getInputStream(), process.getErrorStream())
                    .withInputFilters(removeColors(), removeNonPrintable())
                    .withTimeout(15, TimeUnit.SECONDS);
    }

    private void disconnectAndClose(final Process process, final Expect expect) throws IOException, InterruptedException {
        expect.sendLine("disconnect");
        TimeUnit.SECONDS.sleep(1);

        expect.sendLine("exit"); // Exit from gattool
        TimeUnit.SECONDS.sleep(1);

        // maybe gattool was not running and now the terminal was closed
        if (process.isAlive()) {
            expect.sendLine("exit"); // Exit from terminal
        }

        expect.expect(eof());
        expect.close();

        cleanup(process);
    }

    private void cleanup(final Process process) throws InterruptedException {
        process.waitFor(30, TimeUnit.SECONDS);
        if (process.isAlive()) {
            log.warn("Killing process with PID {}", process.toHandle().pid());
            process.destroyForcibly();
        }
    }
}
