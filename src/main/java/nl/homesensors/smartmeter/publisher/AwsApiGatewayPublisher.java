package nl.homesensors.smartmeter.publisher;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;

import nl.homesensors.AwsApiGateway;
import nl.homesensors.smartmeter.SmartMeterMessage;

@Component
public class AwsApiGatewayPublisher implements SmartMeterMessagePublisher {

    private static final Logger LOGGER = LoggerFactory.getLogger(AwsApiGatewayPublisher.class);

    private static final String API_PATH = "smartmeter";

    private final AwsApiGatewayMessageFactory messageFactory;
    private final AwsApiGateway awsApiGateway;

    public AwsApiGatewayPublisher(final AwsApiGatewayMessageFactory messageFactory,
                                  final AwsApiGateway awsApiGateway) {
        this.messageFactory = messageFactory;
        this.awsApiGateway = awsApiGateway;
    }

    public boolean isEnabled() {
        return awsApiGateway.isEnabled();
    }

    // Publish asynchronous, because we do not want to block the main thread
    @Async
    @Override
    public void publish(final SmartMeterMessage smartMeterMessage) {
        LOGGER.debug("AwsApiGatewayPublisher::publish");

        try {
            awsApiGateway.post(API_PATH, messageFactory.create(smartMeterMessage));
        } catch (final JsonProcessingException e) {
            LOGGER.error("Failed to map message to json. Message=" + smartMeterMessage, e);
        }
    }
}
