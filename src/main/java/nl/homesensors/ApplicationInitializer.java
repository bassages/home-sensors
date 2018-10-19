package nl.homesensors;

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Component;

import nl.homesensors.sensortag.SensorTagReader;
import nl.homesensors.smartmeter.SerialSmartMeterReader;

@Component
public class ApplicationInitializer {

    private final SerialSmartMeterReader serialSmartMeterReader;
    private final SensorTagReader sensorTagReader;

    public ApplicationInitializer(final SerialSmartMeterReader serialSmartMeterReader,
                                  final SensorTagReader sensorTagReader) {
        this.serialSmartMeterReader = serialSmartMeterReader;
        this.sensorTagReader = sensorTagReader;
    }

    @PostConstruct
    public void initialize() throws InterruptedException {
        serialSmartMeterReader.run();
        sensorTagReader.run();
    }
}
