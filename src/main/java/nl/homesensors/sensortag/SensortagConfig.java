package nl.homesensors.sensortag;

import org.springframework.boot.context.properties.ConfigurationProperties;

import static java.util.Objects.isNull;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

@ConfigurationProperties(prefix = "home-sensors.sensortag")
record SensortagConfig(
        Integer probetimeSeconds,
        String climateSensorCode,
        String bluetoothAddress
) {
    public boolean isComplete() {
        return isNotBlank(bluetoothAddress) && !isNull(probetimeSeconds) && isNotBlank(climateSensorCode);
    }
}
