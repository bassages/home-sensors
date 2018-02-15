package nl.wiegman.homesensors.smartmeter.publisher;

import java.nio.charset.StandardCharsets;

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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;

import nl.wiegman.homesensors.smartmeter.SmartMeterMessage;

@Component
public class HomeServerLocalSmartMeterPublisher implements SmartMeterMessagePublisher {

    private static final Logger LOGGER = LoggerFactory.getLogger(HomeServerLocalSmartMeterPublisher.class);

    @Value("${home-server-local-rest-service-url:#{null}}")
    private String homeServerRestServiceUrl;

    @Value("${home-server-local-rest-service-basic-auth-user:#{null}}")
    private String homeServerRestServiceBasicAuthUser;
    @Value("${home-server-local-rest-service-basic-auth-password:#{null}}")
    private String homeServerRestServiceBasicAuthPassword;

    private final HomeServerSmartMeterMessageFactory homeServerSmartMeterMessageFactory;

    @Autowired
    public HomeServerLocalSmartMeterPublisher(HomeServerSmartMeterMessageFactory homeServerSmartMeterMessageFactory) {
        this.homeServerSmartMeterMessageFactory = homeServerSmartMeterMessageFactory;
    }

    // Publish asynchronous, because we do not want to block the main thread
    @Async
    @Override
    public void publish(SmartMeterMessage smartMeterMessage) {
        try {
            String url = homeServerRestServiceUrl + "/slimmemeter";
            String jsonMessage = homeServerSmartMeterMessageFactory.create(smartMeterMessage);

            try {
                postJson(url, jsonMessage);
            } catch (Exception e) {
                LOGGER.info("Post to url [" + url + "] failed.", e);
            }

        } catch (JsonProcessingException e) {
            LOGGER.error("Failed to map message to json. Message=" + smartMeterMessage, e);
        }
    }

    private void postJson(String url, String jsonString) throws Exception {
        LOGGER.debug("Post to url: {}. Request body: {}", url, jsonString);

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
}
