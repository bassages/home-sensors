package nl.homesensors.smartmeter.publisher;

import nl.homesensors.smartmeter.SmartMeterMessage;

public interface SmartMeterMessagePublisher {

    boolean isEnabled();

    void publish(SmartMeterMessage smartMeterMessage);
}
