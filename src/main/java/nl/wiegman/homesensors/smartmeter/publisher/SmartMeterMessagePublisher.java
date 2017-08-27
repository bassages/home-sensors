package nl.wiegman.homesensors.smartmeter.publisher;

import nl.wiegman.homesensors.smartmeter.SmartMeterMessage;

public interface SmartMeterMessagePublisher {

    void publish(SmartMeterMessage smartMeterMessage);
}
