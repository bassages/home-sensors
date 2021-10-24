package nl.homesensors.sensortag;

import java.util.Objects;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.builder.ToStringBuilder;

@RequiredArgsConstructor
public final class SensorCode {

    @Getter
    private final String value;

    public static SensorCode of(final String value) {
        return new SensorCode(value);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final SensorCode that = (SensorCode) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("value", value)
                                        .toString();
    }
}
