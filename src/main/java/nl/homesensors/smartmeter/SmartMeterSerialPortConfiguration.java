package nl.homesensors.smartmeter;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
class SmartMeterSerialPortConfiguration {

    private final String portPath;
    private final String baudRate;
    private final String parity;

    public SmartMeterSerialPortConfiguration(@Value("${smart-meter-serial-port-path:#{null}}")
                                             final String portPath,
                                             @Value("${smart-meter-serial-port-baudrate:#{null}}")
                                             final String baudRate,
                                             @Value("${smart-meter-serial-port-parity:#{null}}")
                                             final String parity) {
        this.portPath = portPath;
        this.baudRate = baudRate;
        this.parity = parity;
    }

    String getPath() {
        return portPath;
    }

    String getBaudRate() {
        return baudRate;
    }

    String getParity() {
        return parity;
    }

    boolean isComplete() {
        return isNotBlank(portPath) && isNotBlank(baudRate) && isNotBlank(parity);
    }
}
