package nl.homesensors.smartmeter;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class SmartMeterSerialPortConfigurationTest {

    @Test
    public void givenAllParametersAreNotBlankWhenIsCompleteThenTrue() {
        final SmartMeterSerialPortConfiguration configuration = new SmartMeterSerialPortConfiguration("", "", "");
        assertThat(configuration.isComplete()).isFalse();
    }

    @Test
    public void givenAllParametersAreBlankWhenIsCompleteThenFalse() {
        final SmartMeterSerialPortConfiguration configuration = new SmartMeterSerialPortConfiguration("A", "B", "C");
        assertThat(configuration.isComplete()).isTrue();
    }
}