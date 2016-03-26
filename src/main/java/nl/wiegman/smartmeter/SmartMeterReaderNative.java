package nl.wiegman.smartmeter;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

@Component
public class SmartMeterReaderNative {

    private static final Logger LOG = LoggerFactory.getLogger(SmartMeterReaderNative.class);

    @Value("${smart-meter-port-name}")
    private String smartMeterPort;

    @Autowired
    private MessageBuffer messageBuffer;

//    @PostConstruct
    public void start() {
        connectAndListenForData();
    }

    private void connectAndListenForData() {
        try {
            String command = "sudo cu -l /dev/" + smartMeterPort + " --speed 115200 --parity=even";

            LOG.info("Connecting to smart meter using command=[" + command + "]");

            Process process = Runtime.getRuntime().exec(command);

            final Thread ioThread = new Thread() {
                @Override
                public void run() {
                    handleErrorStream(process.getErrorStream());
                    handleInputStream(process.getInputStream());
                }
            };
            ioThread.start();

            int exitValue = process.waitFor();
            if (exitValue != 0) {
                LOG.warn("Unexpected exit value from command. Exit value=[" + exitValue + "]");
            }

        } catch (IOException | InterruptedException e) {
            LOG.error("Oops, and unexpected error occurred.", e);
        }
    }

    private void handleInputStream(InputStream inputStream) {
        try {
            final BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                if (messageBuffer.getPendingLinesSize() == 0 && StringUtils.startsWithIgnoreCase(line, "connected")) {
                    LOG.info("Successfully connected to smart meter");
                } else {
                    messageBuffer.addLine(line);
                }

            }
            reader.close();
        } catch (IOException e) {
            LOG.error("Oops, and unexpected error occurred.", e);
        }
    }

    private void handleErrorStream(InputStream inputStream) {
        try {
            final BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                LOG.error(line);
            }
            reader.close();
        } catch (IOException e) {
            LOG.error("Oops, and unexpected error occurred.", e);
        }
    }

}
