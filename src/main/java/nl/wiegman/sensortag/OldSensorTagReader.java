package nl.wiegman.sensortag;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;

@Component
public class OldSensorTagReader {

    private static final Logger LOG = LoggerFactory.getLogger(OldSensorTagReader.class);

    private static String GATTOOL_RESULTLINE_PREFIX = "Characteristic value/descriptor: ";
    private static String SENSORTAG_ID = "BC:6A:29:AC:7D:31";

    @Autowired
    private KlimaatReadingPersister klimaatReadingPersister;

//    @Value("${installation-directory}")
//    private String installationDirectory;

    @Value("${sensortag.probetime.seconds}")
    private int sensortagProbeTimeInSeconds;

//    @PostConstruct
    private void connectAndListenForData() throws Exception {
        LOG.info("Starting SensorTagReader");

        try {
            while (true) {
                takeSnapshotOfSensorValues();
                TimeUnit.SECONDS.sleep(sensortagProbeTimeInSeconds);
            }
        } catch (InterruptedException | IOException e) {
            LOG.error("Oops, and unexpected error occurred.", e);
        }
    }

    private void takeSnapshotOfSensorValues() throws IOException, InterruptedException {
//        Process process = new ProcessBuilder()
//                .command("sh", installationDirectory + "/get-sensortag-values.sh")
//                .redirectErrorStream(true)
//                .start();
//
//        process.waitFor();
//
//        String output = IOUtils.toString(process.getInputStream());
//        processGatttoolOutput(output);
    }

    private void processGatttoolOutput(String gatttoolOutput) {

        try {
            String[] lines = gatttoolOutput.split("\n");

            BigDecimal ambientTemperature = null;
            BigDecimal humidity = null;

            for (String line : lines) {
                if (line.startsWith("THERMOMETER")) {
                    ambientTemperature = getAmbientTemperature(line);
                } else if (line.startsWith("HYGROMETER")) {
                    humidity = getHygrometerHumidity(line);
//                    ambientTemperature = getHygrometerTemperature(line);
                } else {
                    LOG.warn("Invalid line from gattool: " + line);
                }
            }

            LOG.info("Temperature = " + ambientTemperature + " Humidity = " + humidity);
            if (ambientTemperature != null && humidity != null) {
                klimaatReadingPersister.persist(ambientTemperature, humidity);
            }

        } catch (NumberFormatException e) {
            LOG.warn("Ignoring invalid output: " + gatttoolOutput);
        }
    }

    private BigDecimal getHygrometerTemperature(String line) {
        BigDecimal temperature = null;
        String hygrometerHexValues = line.replace("HYGROMETER: " + GATTOOL_RESULTLINE_PREFIX, "");
        BigDecimal converted = BigDecimal.valueOf(HygrometerGatt.temperatureFromHex(hygrometerHexValues));
        // Sometimes the meter reading is 0.0, which is strange and probably caused by a false reading. Ignore these values.
        if (converted.doubleValue() != 0.0d) {
            temperature = converted;
            temperature = temperature.setScale(2, BigDecimal.ROUND_CEILING);
        } else {
            LOG.warn("Ignoring invalid line from gattool: " + line);
        }
        return temperature;
    }

    private BigDecimal getAmbientTemperature(String line) {
        BigDecimal ambientTemperature = null;
        String thermometerHexValues = line.replace("THERMOMETER: " + GATTOOL_RESULTLINE_PREFIX, "");
        BigDecimal converted = BigDecimal.valueOf(ThermometerGatt.ambientTemperatureFromHex(thermometerHexValues));
        // Sometimes the meter reading is 0.0, which is strange and probably caused by a false reading. Ignore these values.
        if (converted.doubleValue() != 0.0d) {
            ambientTemperature = converted;
            ambientTemperature = ambientTemperature.setScale(2, BigDecimal.ROUND_CEILING);
        } else {
            LOG.warn("Ignoring invalid line from gattool: " + line);
        }
        return ambientTemperature;
    }

    private BigDecimal getHygrometerHumidity(String line) {
        BigDecimal humidity = null;
        String hygrometerHexValues = line.replace("HYGROMETER: " + GATTOOL_RESULTLINE_PREFIX, "");
        BigDecimal converted = BigDecimal.valueOf(HygrometerGatt.humidityFromHex(hygrometerHexValues));
        // Sometimes the meter reading is 0.0, which is strange and probably caused by a false reading. Ignore these values.
        if (converted.doubleValue() != 0.0d) {
            humidity = converted;
            humidity = humidity.setScale(1, BigDecimal.ROUND_CEILING);
        } else {
            LOG.warn("Ignoring invalid line from gattool: " + line);
        }
        return humidity;
    }

}
