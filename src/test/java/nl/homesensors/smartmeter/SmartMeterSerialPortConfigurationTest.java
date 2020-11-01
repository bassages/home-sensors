package nl.homesensors.smartmeter;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class SmartMeterSerialPortConfigurationTest {

    @Test
    void givenAllParametersAreNotBlankWhenIsCompleteThenTrue() {
        // given
        final SmartMeterSerialPortConfiguration configuration = new SmartMeterSerialPortConfiguration("", "", "");

        // when
        final boolean complete = configuration.isComplete();

        // then
        assertThat(complete).isFalse();
    }

    @Test
    void givenAllParametersAreBlankWhenIsCompleteThenFalse() {
        // given
        final SmartMeterSerialPortConfiguration configuration = new SmartMeterSerialPortConfiguration("A", "B", "C");

        // when
        final boolean complete = configuration.isComplete();

        // then
        assertThat(complete).isTrue();
    }
}
