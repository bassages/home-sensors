package nl.wiegman.sensortag;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;

@Component
public class SensorTagReader {

    private static final Logger LOG = LoggerFactory.getLogger(SensorTagReader.class);

    @Autowired
    private SensortagPersister sensortagPersister;

    @PostConstruct
    private void connectAndListenForData() {
        LOG.info("Starting SensorTagReader");

        try {
            String command = "sh /home/pi/sensortag/ambienttemperature.sh 120";

            Process process = Runtime.getRuntime().exec(command);

            final Thread ioThread = new Thread() {
                @Override
                public void run() {
                    handleInputStream(process.getInputStream());
                    handleErrorStream(process.getErrorStream());
                }
            };
            ioThread.start();

            int exitValue = process.waitFor();
            if (exitValue != 0) {
                LOG.warn("Unexpected exit value from command. Exit value=[" + exitValue + "]");
            }

        } catch (InterruptedException | IOException e) {
            LOG.error("Oops, and unexpected error occurred.", e);
        }
    }

    private void handleInputStream(InputStream inputStream) {
        InputStreamReader in = new InputStreamReader(inputStream);
        try {
            LineIterator it = IOUtils.lineIterator(in);
            while (it.hasNext()) {
                persist(it.nextLine());
            }
        } finally {
            IOUtils.closeQuietly(in);
        }
    }

    private void handleErrorStream(InputStream inputStream) {
        InputStreamReader in = new InputStreamReader(inputStream);
        try {
            LineIterator it = IOUtils.lineIterator(in);
            while (it.hasNext()) {
                LOG.error(it.nextLine());
            }
        } finally {
            IOUtils.closeQuietly(in);
        }
    }

    private void persist(String line) {
        try {
            BigDecimal bigDecimal = new BigDecimal(line);
            if (!BigDecimal.ZERO.equals(bigDecimal)) {
                LOG.info(line);
                sensortagPersister.persist(bigDecimal);
            } else {
                LOG.warn("Ignoring invalid temperature: " + line);
            }

        } catch (NumberFormatException e) {
            LOG.warn("Ignoring invalid temperature: " + line);
        }
    }
}
