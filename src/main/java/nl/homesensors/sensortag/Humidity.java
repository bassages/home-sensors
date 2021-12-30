package nl.homesensors.sensortag;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor
public final class Humidity {

    @Getter
    private final BigDecimal value;

    @EqualsAndHashCode.Include
    private BigDecimal value() {
        return value == null ? null : value.stripTrailingZeros();
    }
}
