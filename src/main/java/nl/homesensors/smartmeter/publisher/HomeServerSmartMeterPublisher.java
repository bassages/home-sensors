package nl.homesensors.smartmeter.publisher;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.homesensors.homeserver.HomeServerApi;
import nl.homesensors.smartmeter.SmartMeterMessage;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import static java.lang.String.format;
@Slf4j
@Component
@RequiredArgsConstructor
public class HomeServerSmartMeterPublisher implements SmartMeterMessagePublisher {
    private static final String API_PATH = "slimmemeter";

    private final HomeServerSmartMeterMessageFactory homeServerSmartMeterMessageFactory;
    private final HomeServerApi homeServerApi;

    // Publish asynchronous,
    // because we do not want to block the main thread which should always be able read sensor values
    @Async
    @Override
    public void publish(final SmartMeterMessage smartMeterMessage) {
        try {
            homeServerApi.post(API_PATH, homeServerSmartMeterMessageFactory.create(smartMeterMessage));
        } catch (final JsonProcessingException e) {
            log.error(format("Failed to map message to json. Message=%s", smartMeterMessage), e);
        }
    }

    @Override
    public boolean isEnabled() {
        return homeServerApi.isEnabled();
    }
}
