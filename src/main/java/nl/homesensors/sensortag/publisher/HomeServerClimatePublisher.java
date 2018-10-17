package nl.homesensors.sensortag.publisher;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import nl.homesensors.HomeServerRestEndPoint;
import nl.homesensors.sensortag.Humidity;
import nl.homesensors.sensortag.SensorCode;
import nl.homesensors.sensortag.Temperature;

@Component
public class HomeServerClimatePublisher implements ClimatePublisher {
    private static final Logger LOG = LoggerFactory.getLogger(HomeServerClimatePublisher.class);

    private final HomeServerRestEndPoint homeServerRestEndPoint;
    private final Clock clock;

    public HomeServerClimatePublisher(final HomeServerRestEndPoint homeServerRestEndPoint, final Clock clock) {
        this.homeServerRestEndPoint = homeServerRestEndPoint;
        this.clock = clock;
    }

    // Publish asynchronous, because we do not want to block the main thread
    @Async
    @Override
    public void publish(final SensorCode sensorCode, final Temperature temperature, final Humidity humidity) {
        LOG.debug("HomeServerClimatePublisher::publish");

        try {
            final String jsonMessage = HomeServerKlimaat.of(LocalDateTime.now(clock), temperature, humidity).asJson();
            final String path = String.format("klimaat/sensors/%s", sensorCode.getValue());

            homeServerRestEndPoint.post(path, jsonMessage);

        } catch (final JsonProcessingException e) {
            LOG.error("Failed to create json. temperatuur=" + temperature + " luchtvochtigheid=" + humidity, e);
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
            final ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule());
            objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
            objectMapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
            return objectMapper.writeValueAsString(this);
        }
    }
}
