package nl.homesensors.sensortag.publisher;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import nl.homesensors.HomeServerRestEndPoint;
import nl.homesensors.sensortag.Humidity;
import nl.homesensors.sensortag.SensorCode;
import nl.homesensors.sensortag.Temperature;

@Component
public class HomeServerLocalClimatePublisher implements ClimatePublisher {
    private static final Logger LOG = LoggerFactory.getLogger(HomeServerLocalClimatePublisher.class);

    private final HomeServerRestEndPoint homeServerRestEndPoint;

    public HomeServerLocalClimatePublisher(final HomeServerRestEndPoint homeServerRestEndPoint) {
        this.homeServerRestEndPoint = homeServerRestEndPoint;
    }

    // Publish asynchronous, because we do not want to block the main thread
    @Async
    @Override
    public void publish(final SensorCode sensorCode, final Temperature temperature, final Humidity humidity) {
        LOG.debug("HomeServerLocalClimatePublisher::publish");

        try {
            final String jsonMessage = createJsonMessage(temperature, humidity);
            final String path = String.format("klimaat/sensors/%s", sensorCode.getValue());

            homeServerRestEndPoint.post(path, jsonMessage);

        } catch (final JsonProcessingException e) {
            LOG.error("Failed to create json message. temperatuur=" + temperature + " luchtvochtigheid=" + humidity, e);
        }
    }

    private String createJsonMessage(final Temperature temperature, final Humidity humidity) throws JsonProcessingException {
        final HomeServerKlimaat homeServerKlimaat = new HomeServerKlimaat();
        homeServerKlimaat.setDatumtijd(LocalDateTime.now());
        homeServerKlimaat.setTemperatuur(temperature.getValue());
        homeServerKlimaat.setLuchtvochtigheid(humidity.getValue());

        final ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        return objectMapper.writeValueAsString(homeServerKlimaat);
    }

    private static class HomeServerKlimaat {
        private LocalDateTime datumtijd;
        private BigDecimal temperatuur;
        private BigDecimal luchtvochtigheid;

        public LocalDateTime getDatumtijd() {
            return datumtijd;
        }

        public void setDatumtijd(LocalDateTime datumtijd) {
            this.datumtijd = datumtijd;
        }

        public BigDecimal getTemperatuur() {
            return temperatuur;
        }

        public void setTemperatuur(BigDecimal temperatuur) {
            this.temperatuur = temperatuur;
        }

        public BigDecimal getLuchtvochtigheid() {
            return luchtvochtigheid;
        }

        public void setLuchtvochtigheid(BigDecimal luchtvochtigheid) {
            this.luchtvochtigheid = luchtvochtigheid;
        }
    }
}
