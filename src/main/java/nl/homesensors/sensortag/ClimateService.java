package nl.homesensors.sensortag;

import nl.homesensors.sensortag.publisher.ClimatePublisher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class ClimateService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ClimateService.class);

    private final List<ClimatePublisher> climatePublishers;

    public ClimateService(final List<ClimatePublisher> climatePublishers) {
        this.climatePublishers = climatePublishers;
    }

    void publish(final SensorCode sensorCode, final Temperature temperature, final Humidity humidity) {
        LOGGER.debug("Publishing to {} publishers", climatePublishers.size());
        climatePublishers.forEach(publisher -> publisher.publish(sensorCode, temperature, humidity));
    }
}
