package nl.homesensors.smartmeter.publisher;

import nl.homesensors.smartmeter.SmartMeterMessage;

public interface SmartMeterMessagePublisher {

    void publish(SmartMeterMessage smartMeterMessage);
}
