package nl.homesensors.smartmeter;

import org.springframework.boot.context.properties.ConfigurationProperties;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

@ConfigurationProperties("home-sensors.smart-meter.serial-port")
record SmartMeterSerialPortConfiguration(
        String path,
        String baudRate,
        String parity
) {
    boolean isComplete() {
        return isNotBlank(path) && isNotBlank(baudRate) && isNotBlank(parity);
    }
}
