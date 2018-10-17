package nl.homesensors.sensortag.publisher;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;

import javax.inject.Provider;

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

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import nl.homesensors.sensortag.Humidity;
import nl.homesensors.sensortag.SensorCode;
import nl.homesensors.sensortag.Temperature;

@Service
public class HomeDisplayClimatePublisher implements ClimatePublisher {
    private static final Logger LOG = LoggerFactory.getLogger(HomeDisplayClimatePublisher.class);

    @Value("${home-display-rest-service-klimaat-url:#{null}}")
    private final String homeDisplayRestServiceKlimaatUrl;
    private final Provider<HttpClientBuilder> httpClientBuilder;
    private final Clock clock;

    public HomeDisplayClimatePublisher(@Value("${home-display-rest-service-klimaat-url:#{null}}") final String homeDisplayRestServiceKlimaatUrl,
                                       final Provider<HttpClientBuilder> httpClientBuilder,
                                       final Clock clock) {
        this.httpClientBuilder = httpClientBuilder;
        this.homeDisplayRestServiceKlimaatUrl = homeDisplayRestServiceKlimaatUrl;
        this.clock = clock;
    }

    @Async
    @Override
    public void publish(final SensorCode sensorCode, final Temperature temperature, final Humidity humidity) {
        LOG.debug("HomeDisplayClimatePublisher::publish");

        if (homeDisplayRestServiceKlimaatUrl == null) {
            return;
        }

        final String json;
        try {
            json = HomeDisplayKlimaat.of(LocalDateTime.now(clock), temperature, humidity).asJson();
        } catch (final JsonProcessingException e) {
            LOG.error("Failed to create json. temperature=" + temperature + " humidity=" + humidity, e);
            return;
        }

        try {
            postToHomeDisplay(json);
        } catch (final Exception e) {
            LOG.info("Post to {} failed: {}", homeDisplayRestServiceKlimaatUrl, e.getMessage());
        }
    }

    private void postToHomeDisplay(final String jsonString) throws Exception {
        LOG.debug("Post to home-display: {}", jsonString);

        try (final CloseableHttpClient httpClient = httpClientBuilder.get().build()) {
            final HttpPost request = createHttpPost(jsonString);

            final CloseableHttpResponse response = httpClient.execute(request);
            if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                throw new RuntimeException("Unexpected statuscode: " + response.getStatusLine());
            }
        }
    }

    private HttpPost createHttpPost(final String json) {
        final HttpPost request = new HttpPost(homeDisplayRestServiceKlimaatUrl);
        final StringEntity params = new StringEntity(json, ContentType.APPLICATION_JSON);
        request.setEntity(params);
        return request;
    }

    @SuppressWarnings("unused")
    private static class HomeDisplayKlimaat {
        private LocalDateTime datumtijd;
        private BigDecimal temperatuur;
        private BigDecimal luchtvochtigheid;

        public static HomeDisplayKlimaat of(final LocalDateTime dateTime, final Temperature temperature, final Humidity humidity) {
            final HomeDisplayKlimaat homeDisplayKlimaat = new HomeDisplayKlimaat();
            homeDisplayKlimaat.datumtijd = dateTime;
            homeDisplayKlimaat.temperatuur = temperature.getValue();
            homeDisplayKlimaat.luchtvochtigheid = humidity.getValue();
            return homeDisplayKlimaat;
        }

        private String asJson() throws JsonProcessingException {
            final ObjectMapper mapper = new ObjectMapper();
            mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
            mapper.registerModule(new JavaTimeModule());
            mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
            return mapper.writeValueAsString(this);
        }
    }
}
