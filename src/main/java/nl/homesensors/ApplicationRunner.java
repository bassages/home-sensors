package nl.homesensors;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import nl.homesensors.sensortag.SensorTagReader;
import nl.homesensors.smartmeter.SerialSmartMeterReader;

@Component
@RequiredArgsConstructor
public class ApplicationRunner implements CommandLineRunner {

    private final SerialSmartMeterReader serialSmartMeterReader;
    private final SensorTagReader sensorTagReader;

    @Override
    public void run(String... args) throws Exception {
        serialSmartMeterReader.run();
        sensorTagReader.run();
    }
}
