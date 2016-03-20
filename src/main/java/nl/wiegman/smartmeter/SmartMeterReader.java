package nl.wiegman.smartmeter;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortEvent;
import com.fazecast.jSerialComm.SerialPortPacketListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

@Component
public class SmartMeterReader {

    private final Logger logger = LoggerFactory.getLogger(SmartMeterReader.class);

    private static final String SMART_METER_PORT_NAME = "cu.usbserial-AI02DX8V";
//    private static final String SMART_METER_PORT_NAME = "cu.Bluetooth-Incoming-Port";

    @PostConstruct
    public void start() {
        SerialPort smartMeterPort = findSmartMeterPort();

        if (smartMeterPort == null) {
            logger.error("Failed to connect to port " + SMART_METER_PORT_NAME);
            logAvailablePorts();
        } else {
            smartMeterPort.setComPortParameters(115200, 7, SerialPort.ONE_STOP_BIT, SerialPort.EVEN_PARITY);
            smartMeterPort.setFlowControl(SerialPort.FLOW_CONTROL_DISABLED);

            boolean isPortOpened = smartMeterPort.openPort();

            if (isPortOpened) {
                logger.info("Connected to port " + smartMeterPort.getDescriptivePortName() + " on " + smartMeterPort.getSystemPortName());

                // Uncomment one of the following:
//                smartMeterPort.addDataListener(new DataListener());
                pollForData(smartMeterPort);

            } else {
                logger.error("Failed to open port " + SMART_METER_PORT_NAME);
                logAvailablePorts();
            }
        }
    }

    private SerialPort findSmartMeterPort() {
        SerialPort result = null;

        SerialPort[] comPorts = SerialPort.getCommPorts();
        for (SerialPort serialPort : comPorts) {
            if (SMART_METER_PORT_NAME.equals(serialPort.getSystemPortName())) {
                result = serialPort;
            }
        }
        return result;
    }

    private void logAvailablePorts() {
        logger.info("---------------------------------------------------------------------");
        logger.info("The following ports are available:");
        for (SerialPort serialPort : SerialPort.getCommPorts()) {
            logger.info(serialPort.getSystemPortName() + ": " + serialPort.getDescriptivePortName());
        }
        logger.info("---------------------------------------------------------------------");
    }

    private void pollForData(SerialPort smartMeterPort) {
        try {
            while (true) {
                waitForAvailableBytes(smartMeterPort);

                byte[] readBuffer = new byte[smartMeterPort.bytesAvailable()];
                int numRead = smartMeterPort.readBytes(readBuffer, readBuffer.length);
                logger.info("Read " + numRead + " bytes.");

                String s = new String(readBuffer);
                logger.info("Read: " + s);
            }
        } catch (Exception e) {
            logger.error("Exception while reading data from smart meter", e);
        } finally {
            if (smartMeterPort != null && smartMeterPort.isOpen()) {
                smartMeterPort.closePort();
            }
        }
    }

    private void waitForAvailableBytes(SerialPort smartMeterPort) throws InterruptedException {
        while (smartMeterPort.bytesAvailable() == 0) {
            logger.info("Waiting for bytes...");
            Thread.sleep(1000);
        }
    }

    private static class DataListener implements SerialPortPacketListener {

        private final Logger logger = LoggerFactory.getLogger(DataListener.class);

        @Override
        public int getListeningEvents() {
            return SerialPort.LISTENING_EVENT_DATA_RECEIVED;
        }

        @Override
        public void serialEvent(SerialPortEvent serialPortEvent) {
            if (serialPortEvent.getEventType() != SerialPort.LISTENING_EVENT_DATA_RECEIVED) {
                return;
            }
            byte[] newData = serialPortEvent.getReceivedData();
            String s = new String(newData);
            logger.info("Read: " + s);
        }

        @Override
        public int getPacketSize() {
            return 100;
        }
    }

    private void nativeConnection() {

        try {

            Process process = Runtime.getRuntime().exec("sudo cu -l /dev/" + SMART_METER_PORT_NAME + " --speed 115200 --parity=even");

            final Thread ioThread = new Thread() {
                @Override
                public void run() {
                    try {
                        final BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                        String line = null;
                        while ((line = reader.readLine()) != null) {
                            logger.info(line);
                        }
                        reader.close();
                    } catch (IOException e) {
                        logger.error("", e);
                    }
                }
            };
            ioThread.start();

            process.waitFor();
        } catch (IOException | InterruptedException e) {
            logger.error("", e);
        }
    }
}
