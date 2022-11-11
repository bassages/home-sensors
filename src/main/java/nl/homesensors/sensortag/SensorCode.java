package nl.homesensors.sensortag;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@RequiredArgsConstructor
@ToString
@EqualsAndHashCode
public final class SensorCode {

    @Getter
    private final String value;

    public static SensorCode of(final String value) {
        return new SensorCode(value);
    }
}
