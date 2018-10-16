package nl.homesensors.sensortag.publisher;

import nl.homesensors.sensortag.Humidity;
import nl.homesensors.sensortag.SensorCode;
import nl.homesensors.sensortag.Temperature;

public interface ClimatePublisher {

    void publish(SensorCode sensorCode, Temperature temperature, Humidity humidity);
}
