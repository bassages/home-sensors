package nl.wiegman.smartmeter;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.*;

@Component
public class SmartMeterReaderNative {

    private static final Logger LOG = LoggerFactory.getLogger(SmartMeterReaderNative.class);

    @Value("${smart-meter-port-name}")
    private String smartMeterPort;

    @Autowired
    private MessageBuffer messageBuffer;

    @PostConstruct
    public void start() {
        connectAndListenForData();
    }

    private void connectAndListenForData() {
        try {
            String command = "cu -l /dev/" + smartMeterPort + " --speed 115200 --parity=even";

            Process process = Runtime.getRuntime().exec(command);

            final Thread ioThread = new Thread() {
                @Override
                public void run() {
                    handleInputStream(process.getInputStream());
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
                String line = it.nextLine();
                messageBuffer.addLine(line);
            }
        } finally {
            IOUtils.closeQuietly(in);
        }
    }
}
