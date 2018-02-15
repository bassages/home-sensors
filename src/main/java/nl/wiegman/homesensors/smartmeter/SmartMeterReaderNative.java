package nl.wiegman.homesensors.smartmeter;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Reads data from a serial device which is connected to the P1 port of a Smart Meter.
 * The data will be posted to home-server.
 *
 * Needs the "cu" command to be installed on the host OS.
 */
@Component
public class SmartMeterReaderNative {

    private static final Logger LOG = LoggerFactory.getLogger(SmartMeterReaderNative.class);

    @Value("${smart-meter-serial-port-path:#{null}}")
    private String smartMeterSerialPortPath;
    @Value("${smart-meter-serial-port-baudrate:#{null}}")
    private String smartMeterSerialPortBaudRate;
    @Value("${smart-meter-serial-port-parity:#{null}}")
    private String smartMeterSerialPortParity;

    private final MessageBuffer messageBuffer;

    @Autowired
    public SmartMeterReaderNative(MessageBuffer messageBuffer) {
        this.messageBuffer = messageBuffer;
    }

    @Async
    public void run() {
        if (smartMeterSerialPortPath == null || smartMeterSerialPortBaudRate == null || smartMeterSerialPortParity == null) {
            LOG.info("Not started SmartMeterReader, because the configuration for it is not defined.");
        } else {
            LOG.info("Start SmartMeterReader");
            connectAndListenForData();
        }
    }

    private void connectAndListenForData() {
        LOG.info("Starting SmartMeterReaderNative");

        try {
            String command = "cu -l " + smartMeterSerialPortPath + " --speed " + smartMeterSerialPortBaudRate + " --parity=" + smartMeterSerialPortParity + " -E q";

            Process process = Runtime.getRuntime().exec(command);

            final Thread ioThread = new Thread(() -> {
                handleInputStream(process.getInputStream());
                handleErrorStream(process.getErrorStream());
            });
            ioThread.start();

            int exitValue = process.waitFor();
            if (exitValue != 0) {
                LOG.warn("Unexpected exit value from command. Exit value=[" + exitValue + "]");
            }

        } catch (InterruptedException | IOException e) {
            LOG.error("An unexpected error occurred.", e);
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