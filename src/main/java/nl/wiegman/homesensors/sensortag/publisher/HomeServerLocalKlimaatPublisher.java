package nl.wiegman.homesensors.sensortag.publisher;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpHeaders;
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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class HomeServerLocalKlimaatPublisher implements KlimaatPublisher {
    private static final Logger LOG = LoggerFactory.getLogger(HomeServerLocalKlimaatPublisher.class);

    @Value("${home-server-local-rest-service-url:#{null}}")
    private String homeServerRestServiceUrl;

    @Value("${home-server-local-rest-service-basic-auth-user:#{null}}")
    private String homeServerRestServiceBasicAuthUser;
    @Value("${home-server-local-rest-service-basic-auth-password:#{null}}")
    private String homeServerRestServiceBasicAuthPassword;

    // Publish asynchronous, because we do not want to block the main thread
    @Async
    @Override
    public void publish(String klimaatSensorCode, BigDecimal temperatuur, BigDecimal luchtvochtigheid) {
        LOG.debug("HomeServerLocalKlimaatPublisher::publish");

        if (homeServerRestServiceUrl != null) {
            try {
                String jsonMessage = createKlimaatJsonMessage(temperatuur, luchtvochtigheid);
                String url = String.format(homeServerRestServiceUrl + "/klimaat/sensors/%s", klimaatSensorCode);

                try {
                    postJson(url, jsonMessage);
                } catch (Exception e) {
                    LOG.info("Post to url [" + url + "] failed.", e);
                }

            } catch (JsonProcessingException e) {
                LOG.error("Failed to create json message. temperatuur=" + temperatuur + " luchtvochtigheid=" + luchtvochtigheid, e);
            }
        }
    }

    private String createKlimaatJsonMessage(BigDecimal temperatuur, BigDecimal luchtvochtigheid) throws JsonProcessingException {
        HomeServerKlimaat homeServerKlimaat = new HomeServerKlimaat();
        homeServerKlimaat.setDatumtijd(new Date().getTime());
        homeServerKlimaat.setTemperatuur(temperatuur);
        homeServerKlimaat.setLuchtvochtigheid(luchtvochtigheid);
        return new ObjectMapper().writeValueAsString(homeServerKlimaat);
    }

    private void postJson( String url, String jsonString) throws Exception {
        LOG.debug("Post to url: {}. Request body: {}", url, jsonString);

        try (CloseableHttpClient httpClient = HttpClientBuilder.create().build()) {

            HttpPost request = new HttpPost(url);
            StringEntity params = new StringEntity(jsonString, ContentType.APPLICATION_JSON);
            request.setEntity(params);

            setAuthorizationHeader(request);

            CloseableHttpResponse response = httpClient.execute(request);

            if (response.getStatusLine().getStatusCode() != HttpStatus.SC_CREATED) {
                throw new RuntimeException("Unexpected statusline: " + response.getStatusLine());
            }
        }
    }

    private void setAuthorizationHeader(HttpPost request) {
        if (homeServerRestServiceBasicAuthUser != null
                && homeServerRestServiceBasicAuthPassword != null) {
            String auth = homeServerRestServiceBasicAuthUser + ":" + homeServerRestServiceBasicAuthPassword;
            byte[] encodedAuth = Base64.encodeBase64(auth.getBytes(StandardCharsets.UTF_8));
            String authorizationHeader = "Basic " + new String(encodedAuth);
            request.setHeader(HttpHeaders.AUTHORIZATION, authorizationHeader);
        }
    }

    private static class HomeServerKlimaat {
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
