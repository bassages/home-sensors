package nl.homesensors.smartmeter;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

@RequiredArgsConstructor
@Getter
@ConstructorBinding
@ConfigurationProperties("home-sensors.smart-meter.serial-port")
public class SmartMeterSerialPortConfiguration {
    private final String path;
    private final String baudRate;
    private final String parity;

    boolean isComplete() {
        return isNotBlank(path) && isNotBlank(baudRate) && isNotBlank(parity);
    }
}
