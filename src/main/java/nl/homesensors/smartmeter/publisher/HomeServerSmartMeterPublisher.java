package nl.homesensors.smartmeter.publisher;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.homesensors.HomeServerRestEndPoint;
import nl.homesensors.smartmeter.SmartMeterMessage;
import org.apache.commons.lang3.builder.RecursiveToStringStyle;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import static java.lang.String.format;

@Slf4j
@Component
@RequiredArgsConstructor
public class HomeServerSmartMeterPublisher implements SmartMeterMessagePublisher {
    private static final String API_PATH = "slimmemeter";

    private final HomeServerSmartMeterMessageFactory homeServerSmartMeterMessageFactory;
    private final HomeServerRestEndPoint homeServerRestEndPoint;

    // Publish asynchronous,
    // because we do not want to block the main thread which should always be able read sensor values
    @Async
    @Override
    public void publish(final SmartMeterMessage smartMeterMessage) {
        try {
            homeServerRestEndPoint.post(API_PATH, homeServerSmartMeterMessageFactory.create(smartMeterMessage));
        } catch (final JsonProcessingException e) {
            final String smartMeterMessageAsString = ReflectionToStringBuilder.toString(smartMeterMessage, new RecursiveToStringStyle());
            log.error(format("Failed to map message to json. Message=%s", smartMeterMessageAsString), e);
        }
    }

    @Override
    public boolean isEnabled() {
        return homeServerRestEndPoint.isEnabled();
    }
}
