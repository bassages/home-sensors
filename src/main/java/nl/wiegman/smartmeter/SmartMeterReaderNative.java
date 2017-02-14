package nl.wiegman.smartmeter;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

@Component
public class SmartMeterReaderNative implements CommandLineRunner {

    private static final Logger LOG = LoggerFactory.getLogger(SmartMeterReaderNative.class);

    private static final String SERIAL_BAUD_RATE_SPEED = "115200";
    private static final String SERIAL_PARITY_EVEN = "even";

    @Value("${smart-meter-port-name}")
    private String smartMeterPort;

    @Autowired
    private MessageBuffer messageBuffer;

    @Override
    public void run(String... args) throws Exception {
//        connectAndListenForData();
    }

    private void connectAndListenForData() {
        LOG.info("Starting SmartMeterReaderNative");

        try {
            String command = "cu -l /dev/" + smartMeterPort + " --speed " + SERIAL_BAUD_RATE_SPEED + " --parity=" + SERIAL_PARITY_EVEN;

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
                messageBuffer.addLine(it.nextLine());
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
}