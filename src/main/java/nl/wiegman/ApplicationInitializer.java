package nl.wiegman;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import nl.wiegman.homesensors.SmartMeterReaderNative;
import nl.wiegman.sensortag.SensorTagReader;

@Component
public class ApplicationInitializer {

    @Autowired
    private SmartMeterReaderNative smartMeterReaderNative;
    @Autowired
    private SensorTagReader sensorTagReader;

    @PostConstruct
    public void initialize() throws Exception {
        smartMeterReaderNative.run();
        sensorTagReader.run();
    }

}
