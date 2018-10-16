package nl.homesensors;

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Component;

import nl.homesensors.sensortag.SensorTagReader;
import nl.homesensors.smartmeter.SmartMeterReaderNative;

@Component
public class ApplicationInitializer {

    private final SmartMeterReaderNative smartMeterReaderNative;
    private final SensorTagReader sensorTagReader;

    public ApplicationInitializer(final SmartMeterReaderNative smartMeterReaderNative,
                                  final SensorTagReader sensorTagReader) {
        this.smartMeterReaderNative = smartMeterReaderNative;
        this.sensorTagReader = sensorTagReader;
    }

    @PostConstruct
    public void initialize() throws Exception {
        smartMeterReaderNative.run();
        sensorTagReader.run();
    }

}
