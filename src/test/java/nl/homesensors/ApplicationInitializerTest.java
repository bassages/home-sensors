package nl.homesensors;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.verify;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import nl.homesensors.sensortag.SensorTagReader;
import nl.homesensors.smartmeter.SmartMeterReaderNative;

@RunWith(MockitoJUnitRunner.class)
public class ApplicationInitializerTest {

    @InjectMocks
    private ApplicationInitializer applicationInitializer;

    @Mock
    private SmartMeterReaderNative smartMeterReaderNative;
    @Mock
    private SensorTagReader sensorTagReader;

    @Test
    public void whenInitializedThenReadersRun() throws Exception {
        applicationInitializer.initialize();

        verify(smartMeterReaderNative).run();
        verify(sensorTagReader).run();
    }
}