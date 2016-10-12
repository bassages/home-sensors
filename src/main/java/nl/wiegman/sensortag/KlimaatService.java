package nl.wiegman.sensortag;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class KlimaatService {

    private static final Logger LOG = LoggerFactory.getLogger(KlimaatService.class);

    @Value("${home-server-rest-service-klimaat-url}")
    private String homeServerRestServiceKlimaatUrl;
    @Value("${home-server-rest-service-klimaat-basic-auth-user:#{null}}")
    private String homeServerRestServiceKlimaatBasicAuthUser;
    @Value("${home-server-rest-service-klimaat-basic-auth-password:#{null}}")
    private String homeServerRestServiceKlimaatBasicAuthPassword;

    public void persist(BigDecimal temperatuur, BigDecimal luchtvochtigheid) {

        try {
            String jsonMessage = createSmartMeterJsonMessage(temperatuur, luchtvochtigheid);

            try {
                postToHomeServer(jsonMessage);

            } catch (Exception e) {
                LOG.warn("Post to " + homeServerRestServiceKlimaatUrl + " failed.", e);
            }

        } catch (JsonProcessingException e) {
            LOG.error("Failed to map temperatuur to json. Temperatuur=" + temperatuur, e);
        }
    }

    private void postToHomeServer(String jsonString) throws Exception {
        LOG.debug("Post to home-server: " + jsonString);

        try (CloseableHttpClient httpClient = HttpClientBuilder.create().build()) {

            HttpPost request = new HttpPost(homeServerRestServiceKlimaatUrl);
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
        if (homeServerRestServiceKlimaatBasicAuthUser != null
                && homeServerRestServiceKlimaatBasicAuthPassword != null) {

            String auth = homeServerRestServiceKlimaatBasicAuthUser + ":" + homeServerRestServiceKlimaatBasicAuthPassword;
            byte[] encodedAuth = Base64.encodeBase64(auth.getBytes(StandardCharsets.UTF_8));
            String authorizationHeader = "Basic " + new String(encodedAuth);
            request.setHeader(HttpHeaders.AUTHORIZATION, authorizationHeader);
        }
    }

    private String createSmartMeterJsonMessage(BigDecimal temperatuur, BigDecimal luchtvochtigheid) throws JsonProcessingException {
        HomeServerKlimaat homeServerKlimaat = new HomeServerKlimaat();
        homeServerKlimaat.setDatumtijd(new Date().getTime());
        homeServerKlimaat.setTemperatuur(temperatuur);
        homeServerKlimaat.setLuchtvochtigheid(luchtvochtigheid);
        return new ObjectMapper().writeValueAsString(homeServerKlimaat);
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
