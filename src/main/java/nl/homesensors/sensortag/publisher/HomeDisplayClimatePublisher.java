package nl.homesensors.sensortag.publisher;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;

import nl.homesensors.sensortag.Humidity;
import nl.homesensors.sensortag.SensorCode;
import nl.homesensors.sensortag.Temperature;

@Service
public class HomeDisplayClimatePublisher implements ClimatePublisher {
    private static final Logger LOG = LoggerFactory.getLogger(HomeDisplayClimatePublisher.class);

    @Value("${home-display-rest-service-klimaat-url:#{null}}")
    private String homeDisplayRestServiceKlimaatUrl;

    @Async
    @Override
    public void publish(final SensorCode sensorCode, final Temperature temperature, final Humidity humidity) {
        LOG.debug("HomeDisplayClimatePublisher::publish");

        try {
            final String jsonMessage = createJsonMessage(temperature, humidity);

            if (homeDisplayRestServiceKlimaatUrl != null) {
                try {
                    postToHomeDisplay(jsonMessage);
                } catch (final Exception e) {
                    LOG.info("Post to {} failed: {}", homeDisplayRestServiceKlimaatUrl, e.getMessage());
                }
            }

        } catch (final JsonProcessingException e) {
            LOG.error("Failed to create json message. temperature=" + temperature + " humidity=" + humidity, e);
        }
    }

    private void postToHomeDisplay(final String jsonString) throws Exception {
        LOG.debug("Post to home-display: {}", jsonString);

        try (final CloseableHttpClient httpClient = HttpClientBuilder.create().build()) {

            final HttpPost request = new HttpPost(homeDisplayRestServiceKlimaatUrl);
            final StringEntity params = new StringEntity(jsonString, ContentType.APPLICATION_JSON);
            request.setEntity(params);

            final CloseableHttpResponse response = httpClient.execute(request);

            if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                throw new RuntimeException("Unexpected statuscode: " + response.getStatusLine());
            }
        }
    }

    private static String createJsonMessage(final Temperature temperature, final Humidity humidity) throws JsonProcessingException {
        final HomeDisplayKlimaat homeDisplayKlimaat = new HomeDisplayKlimaat();
        homeDisplayKlimaat.setDatumtijd(new Date().getTime());
        homeDisplayKlimaat.setTemperatuur(temperature.getValue());
        homeDisplayKlimaat.setLuchtvochtigheid(humidity.getValue());
        return new ObjectMapper().writeValueAsString(homeDisplayKlimaat);
    }

    private static class HomeDisplayKlimaat {
        private long datumtijd;
        private BigDecimal temperatuur;
        private BigDecimal luchtvochtigheid;

        public long getDatumtijd() {
            return datumtijd;
        }

        public void setDatumtijd(long datumtijd) {
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
