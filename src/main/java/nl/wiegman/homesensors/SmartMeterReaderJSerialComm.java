//package nl.wiegman.smartmeter;
//
//import com.fazecast.jSerialComm.SerialPort;
//import com.fazecast.jSerialComm.SerialPortEvent;
//import com.fazecast.jSerialComm.SerialPortPacketListener;
//import org.apache.commons.io.IOUtils;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Component;
//
//import java.io.IOException;
//import java.io.InputStream;
//import java.util.Arrays;
//import java.util.Collections;
//import java.util.List;
//
//@Component
//public class SmartMeterReaderJSerialComm {
//
//    private static final Logger LOG = LoggerFactory.getLogger(SmartMeterReaderJSerialComm.class);
//
//    @Value("${smart-meter-port-name}")
//    private String smartMeterPortName;
//
//    @Autowired
//    private MessageBuffer messageBuffer;
//
////    @PostConstruct
//    public void start() {
//        SerialPort smartMeterPort = findSmartMeterPort();
//
//        if (smartMeterPort == null) {
//            LOG.error("Failed to connect to port " + smartMeterPortName);
//            logAvailablePorts();
//        } else {
//            smartMeterPort.setBaudRate(115200);
//            smartMeterPort.setParity(SerialPort.EVEN_PARITY);
//            smartMeterPort.setNumStopBits(SerialPort.ONE_STOP_BIT);
//            smartMeterPort.setNumDataBits(7);
//            smartMeterPort.setFlowControl(SerialPort.FLOW_CONTROL_DISABLED);
//
//            try {
//                boolean isPortOpened = smartMeterPort.openPort();
//
//                if (isPortOpened) {
//                    LOG.info("Connected to port " + smartMeterPort.getDescriptivePortName() + " on " + smartMeterPort.getSystemPortName());
//
//                    // Uncomment one of the following:
//                    gatherData(smartMeterPort);
////                    smartMeterPort.addDataListener(new DataListener());
////                pollForData(smartMeterPort);
//
//                } else {
//                    LOG.error("Failed to open port " + smartMeterPortName);
//                    logAvailablePorts();
//                }
//
//            } finally {
////                if (smartMeterPort.isOpen()) {
////                    smartMeterPort.closePort();
////                }
//            }
//        }
//    }
//
//    private void gatherData(SerialPort comPort) {
//        InputStream in = comPort.getInputStream();
//        try
//        {
//            for (int j = 0; j < 1000; ++j)
//                System.out.print((char)in.read());
//            in.close();
//        } catch (Exception e) { e.printStackTrace(); }
//        comPort.closePort();
//    }
//
//    private SerialPort findSmartMeterPort() {
//        SerialPort result = null;
//
//        SerialPort[] comPorts = SerialPort.getCommPorts();
//        for (SerialPort serialPort : comPorts) {
//            if (smartMeterPortName.equals(serialPort.getSystemPortName())) {
//                result = serialPort;
//            }
//        }
//        return result;
//    }
//
//    private void logAvailablePorts() {
//        LOG.info("--------------------------------------------------------------------------------------");
//        LOG.info("-- The following ports are available:");
//        List<SerialPort> availableCommPorts = Arrays.asList(SerialPort.getCommPorts());
//        Collections.sort(availableCommPorts, (SerialPort p1, SerialPort p2) -> p1.getSystemPortName().compareTo(p2.getSystemPortName()));
//        for (SerialPort serialPort : availableCommPorts) {
//            LOG.info("-- * " + serialPort.getSystemPortName() + ": " + serialPort.getDescriptivePortName());
//        }
//        LOG.info("--------------------------------------------------------------------------------------");
//    }
//
////    private void pollForData(SerialPort smartMeterPort) {
////        try {
////            while (true) {
////                waitForAvailableBytes(smartMeterPort);
////
////                byte[] readBuffer = new byte[smartMeterPort.bytesAvailable()];
////                String string = new String(readBuffer);
////
////                LOG.info(string);
////            }
////        } catch (Exception e) {
////            LOG.error("Oops, and unexpected error occurred.", e);
////        } finally {
////            if (smartMeterPort != null && smartMeterPort.isOpen()) {
////                smartMeterPort.closePort();
////            }
////        }
////    }
////
////    private void waitForAvailableBytes(SerialPort smartMeterPort) throws InterruptedException {
////        while (smartMeterPort.bytesAvailable() == 0) {
//////            LOG.info("Waiting for bytes...");
////            Thread.sleep(500);
////        }
////    }
//
//    private static class DataListener implements SerialPortPacketListener {
//
//        private static final Logger LOG = LoggerFactory.getLogger(DataListener.class);
//
//        @Override
//        public int getListeningEvents() {
//            return SerialPort.LISTENING_EVENT_DATA_RECEIVED;
//        }
//
//        @Override
//        public void serialEvent(SerialPortEvent serialPortEvent) {
//
//            if (serialPortEvent.getEventType() != SerialPort.LISTENING_EVENT_DATA_RECEIVED) {
//                LOG.warn("Ignoring serialportevent of type: " + serialPortEvent.getEventType());
//                return;
//            }
//
//            String data = new String(serialPortEvent.getReceivedData());
//            LOG.info(data);
//        }
//
//        @Override
//        public int getPacketSize() {
//            return 100;
//        }
//    }
//
//    private void handleInputStream(InputStream inputStream) {
//        try {
//            IOUtils.copy(inputStream, System.out);
//        } catch (IOException e) {
//            LOG.error("Oops, and unexpected error occurred.", e);
//        }
//    }
//}
