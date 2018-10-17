package nl.homesensors.sensortag;

import java.math.BigDecimal;
import java.util.Objects;

import org.apache.commons.lang3.builder.ToStringBuilder;

public final class Humidity {

    private final BigDecimal value;

    private Humidity(final BigDecimal value) {
        this.value = value;
    }

    public static Humidity of(final BigDecimal value) {
        return new Humidity(value);
    }

    public BigDecimal getValue() {
        return value;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        final Humidity humidity = (Humidity) o;
        return Objects.equals(value, humidity.value);
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
