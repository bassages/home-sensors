package nl.homesensors.sensortag.publisher;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.homesensors.homeserver.HomeServerApi;
import nl.homesensors.sensortag.Humidity;
import nl.homesensors.sensortag.SensorCode;
import nl.homesensors.sensortag.Temperature;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;

import static java.lang.String.format;

@Slf4j
@Component
@RequiredArgsConstructor
public class HomeServerClimatePublisher implements ClimatePublisher {
    private final HomeServerApi homeServerApi;
    private final Clock clock;

    // Publish asynchronous, because we do not want to block the main thread
    @Async
    @Override
    public void publish(final SensorCode sensorCode, final Temperature temperature, final Humidity humidity) {
        log.debug("HomeServerClimatePublisher::publish");

        try {
            final String jsonMessage = HomeServerKlimaat.of(LocalDateTime.now(clock), temperature, humidity).asJson();
            final String path = format("/klimaat/sensors/%s", sensorCode.getValue());

            homeServerApi.post(path, jsonMessage);

        } catch (final JsonProcessingException e) {
            final String message = format("Failed to create json. temperature=%s, humidity=%s", temperature, humidity);
            log.error(message, e);
        }
    }

    @SuppressWarnings("unused")
    private static class HomeServerKlimaat {
        private LocalDateTime datumtijd;
        private BigDecimal temperatuur;
        private BigDecimal luchtvochtigheid;

        public static HomeServerKlimaat of(final LocalDateTime dateTime, final Temperature temperature, final Humidity humidity) {
            final HomeServerKlimaat homeServerKlimaat = new HomeServerKlimaat();
            homeServerKlimaat.datumtijd = dateTime;
            homeServerKlimaat.temperatuur = temperature.getValue();
            homeServerKlimaat.luchtvochtigheid = humidity.getValue();
            return homeServerKlimaat;
        }

        private String asJson() throws JsonProcessingException {
            final ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());
            mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
            mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
            return mapper.writeValueAsString(this);
        }
    }
}
