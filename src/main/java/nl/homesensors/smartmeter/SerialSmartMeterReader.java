package nl.homesensors.smartmeter;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.function.Consumer;

import lombok.RequiredArgsConstructor;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Reads data from a serial device which is connected to the P1 port of a Smart Meter.
 * The data will be posted to home-server.
 *
 * Needs the "cu" command to be installed on the host OS.
 */
@Component
@RequiredArgsConstructor
public class SerialSmartMeterReader {

    private static final Logger LOG = LoggerFactory.getLogger(SerialSmartMeterReader.class);

    private final SmartMeterSerialPortConfiguration smartMeterSerialPortConfiguration;
    private final MessageBuffer messageBuffer;
    private final Runtime runtime;

    @Async
    public void run() {
        if (!smartMeterSerialPortConfiguration.isComplete()) {
            LOG.warn("Not started SmartMeterReader, because the configuration for it is incomplete.");
            return;
        }
        LOG.info("Start SmartMeterReader");
        connectAndListenForData();
    }

    private void connectAndListenForData() {
        try {
            final String command = "cu -l " + smartMeterSerialPortConfiguration.getPath() + " --speed " + smartMeterSerialPortConfiguration.getBaudRate() + " --parity=" + smartMeterSerialPortConfiguration.getParity() + " -E q";
            final Process process = runtime.exec(command);

            final Thread ioThread = new Thread(() -> {
                handleInputStreamLines(process.getInputStream(), messageBuffer::addLine);
                handleInputStreamLines(process.getErrorStream(), LOG::error);
            });

            ioThread.start();
            ioThread.join();

            final int exitValue = process.waitFor();
            if (exitValue != 0) {
                LOG.warn("Unexpected exit value from command. Exit value=[{}]", exitValue);
            }

        } catch (final InterruptedException  e) {
            LOG.error("An unexpected InterruptedException occurred.", e);
            Thread.currentThread().interrupt();
        } catch (final IOException e) {
            LOG.error("An unexpected IOException occurred.", e);
        }
    }

    private void handleInputStreamLines(final InputStream inputStream, final Consumer<String> lineHandler) {
        try (final var inputStreamReader = new InputStreamReader(inputStream)) {
            final LineIterator it = IOUtils.lineIterator(inputStreamReader);
            while (it.hasNext()) {
                lineHandler.accept(it.nextLine());
            }
        } catch (final IOException e) {
            LOG.error("InputStream failure", e);
        }
    }
}
