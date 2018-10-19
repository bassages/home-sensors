package nl.homesensors;

import static org.mockito.Mockito.verify;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import nl.homesensors.sensortag.SensorTagReader;
import nl.homesensors.smartmeter.SerialSmartMeterReader;

@RunWith(MockitoJUnitRunner.class)
public class ApplicationInitializerTest {

    @InjectMocks
    private ApplicationInitializer applicationInitializer;

    @Mock
    private SerialSmartMeterReader serialSmartMeterReader;
    @Mock
    private SensorTagReader sensorTagReader;

    @Test
    public void whenInitializedThenReadersRun() throws Exception {
        applicationInitializer.initialize();

        verify(serialSmartMeterReader).run();
        verify(sensorTagReader).run();
    }
}