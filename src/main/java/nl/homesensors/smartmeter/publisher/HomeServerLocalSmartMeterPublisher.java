package nl.homesensors.smartmeter.publisher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;

import nl.homesensors.HomeServerRestEndPoint;
import nl.homesensors.smartmeter.SmartMeterMessage;

@Component
public class HomeServerLocalSmartMeterPublisher implements SmartMeterMessagePublisher {

    private static final Logger LOGGER = LoggerFactory.getLogger(HomeServerLocalSmartMeterPublisher.class);

    private final HomeServerSmartMeterMessageFactory homeServerSmartMeterMessageFactory;
    private final HomeServerRestEndPoint homeServerRestEndPoint;

    public HomeServerLocalSmartMeterPublisher(final HomeServerSmartMeterMessageFactory homeServerSmartMeterMessageFactory,
                                              final HomeServerRestEndPoint homeServerRestEndPoint) {
        this.homeServerSmartMeterMessageFactory = homeServerSmartMeterMessageFactory;
        this.homeServerRestEndPoint = homeServerRestEndPoint;
    }

    // Publish asynchronous, because we do not want to block the main thread
    @Async
    @Override
    public void publish(final SmartMeterMessage smartMeterMessage) {
        try {
            final String path = "slimmemeter";
            final String jsonMessage = homeServerSmartMeterMessageFactory.create(smartMeterMessage);

            homeServerRestEndPoint.post(path, jsonMessage);

        } catch (final JsonProcessingException e) {
            LOGGER.error("Failed to map message to json. Message=" + smartMeterMessage, e);
        }
    }
}
