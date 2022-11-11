package nl.homesensors.sensortag;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

import static java.util.Objects.isNull;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

@RequiredArgsConstructor
@Getter
@ConstructorBinding
@ConfigurationProperties(prefix = "home-sensors.sensortag")
public class SensortagConfig {

    private final Integer probetimeSeconds;
    private final String climateSensorCode;
    private final String bluetoothAddress;

    public boolean isComplete() {
        return isNotBlank(bluetoothAddress) && !isNull(probetimeSeconds) && isNotBlank(climateSensorCode);
    }
}
