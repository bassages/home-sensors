package nl.wiegman.smartmeter;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortEvent;
import com.fazecast.jSerialComm.SerialPortPacketListener;
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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Component
public class SmartMeterReaderJava {

    private static final Logger LOG = LoggerFactory.getLogger(SmartMeterReaderJava.class);

    @Value("${smart-meter-port-name}")
    private String smartMeterPortName;

    @Autowired
    private MessageBuffer messageBuffer;

    @PostConstruct
    public void start() {
        SerialPort smartMeterPort = findSmartMeterPort();

        if (smartMeterPort == null) {
            LOG.error("Failed to connect to port " + smartMeterPortName);
            logAvailablePorts();
        } else {
            smartMeterPort.setComPortParameters(115200, 7, SerialPort.ONE_STOP_BIT, SerialPort.EVEN_PARITY);
            smartMeterPort.setFlowControl(SerialPort.FLOW_CONTROL_DISABLED);

            boolean isPortOpened = smartMeterPort.openPort();

            if (isPortOpened) {
                LOG.info("Connected to port " + smartMeterPort.getDescriptivePortName() + " on " + smartMeterPort.getSystemPortName());

                // Uncomment one of the following:
                smartMeterPort.addDataListener(new DataListener());
//                pollForData(smartMeterPort);

            } else {
                LOG.error("Failed to open port " + smartMeterPortName);
                logAvailablePorts();
            }
        }
    }

    private void gatherData(SerialPort comPort) {
        comPort.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, 100, 0);
        handleInputStream(comPort.getInputStream());
        comPort.closePort();
    }

    private SerialPort findSmartMeterPort() {
        SerialPort result = null;

        SerialPort[] comPorts = SerialPort.getCommPorts();
        for (SerialPort serialPort : comPorts) {
            if (smartMeterPortName.equals(serialPort.getSystemPortName())) {
                result = serialPort;
            }
        }
        return result;
    }

    private void logAvailablePorts() {
        LOG.info("--------------------------------------------------------------------------------------");
        LOG.info("-- The following ports are available:");
        List<SerialPort> availableCommPorts = Arrays.asList(SerialPort.getCommPorts());
        Collections.sort(availableCommPorts, (SerialPort p1, SerialPort p2) -> p1.getSystemPortName().compareTo(p2.getSystemPortName()));
        for (SerialPort serialPort : availableCommPorts) {
            LOG.info("-- * " + serialPort.getSystemPortName() + ": " + serialPort.getDescriptivePortName());
        }
        LOG.info("--------------------------------------------------------------------------------------");
    }

    private void pollForData(SerialPort smartMeterPort) {
        try {
            while (true) {
                waitForAvailableBytes(smartMeterPort);

                byte[] readBuffer = new byte[smartMeterPort.bytesAvailable()];
                int numRead = smartMeterPort.readBytes(readBuffer, readBuffer.length);
                String string = new String(readBuffer);

                LOG.info("Read " + numRead + " bytes: " + string);
            }
        } catch (Exception e) {
            LOG.error("Exception while reading data from smart meter", e);
        } finally {
            if (smartMeterPort != null && smartMeterPort.isOpen()) {
                smartMeterPort.closePort();
            }
        }
    }

    private void waitForAvailableBytes(SerialPort smartMeterPort) throws InterruptedException {
        while (smartMeterPort.bytesAvailable() == 0) {
            LOG.info("Waiting for bytes...");
            Thread.sleep(1000);
        }
    }

    private static class DataListener implements SerialPortPacketListener {

        private static final Logger LOG = LoggerFactory.getLogger(DataListener.class);

        @Override
        public int getListeningEvents() {
            return SerialPort.LISTENING_EVENT_DATA_RECEIVED;
        }

        @Override
        public void serialEvent(SerialPortEvent serialPortEvent) {
            LOG.info("SerialportEvent of type: " + serialPortEvent.getEventType());

            if (serialPortEvent.getEventType() != SerialPort.LISTENING_EVENT_DATA_RECEIVED) {
                return;
            }

            String data = new String(serialPortEvent.getReceivedData());
            LOG.info("Read: " + data);
        }

        @Override
        public int getPacketSize() {
            return 10;
        }
    }

    private void handleInputStream(InputStream inputStream) {
        try {
            final BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                messageBuffer.addLine(line);
            }
            reader.close();
        } catch (IOException e) {
            LOG.error("IOException", e);
        }
    }
}
