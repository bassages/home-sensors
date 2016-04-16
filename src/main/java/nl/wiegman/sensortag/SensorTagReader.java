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
    private void reconnectToSensorTagOnException() throws InterruptedException {
        LOG.info("Starting SensorTagReader");

        while (1 == 1) {
            try {
                connectAndListenForSensorValues();
            } catch (IOException e) {
                LOG.error("Error occurred, trying to reconnect in 3 seconds...", e);
                TimeUnit.SECONDS.sleep(3);
            }
        }
    }

    private void connectAndListenForSensorValues() throws IOException, InterruptedException {
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
            disableNotifications(expect);
            expect.sendLine("disconnect");
            expect.sendLine("exit");
            expect.expect(eof());
            process.destroyForcibly();
        }
    }

    private void readAndPersistValues(Expect expect) throws IOException {
        String temperatureHex = readTemperature(expect);
        String humidityHex = readHumidity(expect);

        BigDecimal temperature = BigDecimal.valueOf(ThermometerGatt.ambientTemperatureFromHex(temperatureHex));
        BigDecimal humidity = BigDecimal.valueOf(HygrometerGatt.humidityFromHex(humidityHex));

        klimaatReadingPersister.persist(temperature, humidity);
    }

    private void setConnectionParameters() throws IOException {
        Process process = getShellProcess();

        try (Expect expect = getExpectBuilder(process).build()) {

            expect.sendLine("sudo hcitool con");
            Result result = expect.expect(regexp("handle (\\d+) state 1 lm MASTER"));
            String handle = result.group(1);

            expect.sendLine("sudo hcitool lecup --handle " + handle + " --min 200 --max 230");

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
                    .withEchoOutput(System.out)
                    .withEchoInput(System.out)
                    .withInputFilters(removeColors(), removeNonPrintable())
                    .withExceptionOnFailure()
                    .withTimeout(15, TimeUnit.SECONDS);
    }

    private void connectToSensortag(Expect expect) throws IOException {
        expect.sendLine("gatttool -b " + sensortagBluetoothAddress + " --interactive");
        expect.expect(contains("[LE]>"));

        expect.sendLine("connect");
        expect.expect(contains("Connection successful"));
    }

    private void enableNotifications(Expect expect) throws IOException {
        expect.sendLine("char-write-cmd 0x26 0100"); // Enable temperature sensor notifications
        expect.sendLine("char-write-cmd 0x3c 0100"); // Enable humidity sensor notifications
    }

    private void disableNotifications(Expect expect) throws IOException {
        expect.sendLine("char-write-cmd 0x3c 0000"); // Disable humidity sensor notifications
        expect.sendLine("char-write-cmd 0x26 0000"); // Disable temperature sensor notifications
    }

    private String readTemperature(Expect expect) throws IOException {
        expect.sendLine("char-write-cmd 0x29 01"); // Enable temperature sensor

        Result result = expect.expect(regexp("Notification handle = 0x0025 value: (?!00 00 00 00)(\\w{2} \\w{2} \\w{2} \\w{2})"));
        String value = result.group(1);
        expect.sendLine("char-write-cmd 0x29 00"); // Disable temperature sensor

        return value;
    }

    private String readHumidity(Expect expect) throws IOException {
        expect.sendLine("char-write-cmd 0x3f 01"); // Enable humidity sensor

        Result result = expect.expect(regexp("Notification handle = 0x003b value: (?!00 00 00 00)(\\w{2} \\w{2} \\w{2} \\w{2})"));
        String value = result.group(1);
        expect.sendLine("char-write-cmd 0x3f 00"); // Disable humidity sensor

        return value;
    }
}
