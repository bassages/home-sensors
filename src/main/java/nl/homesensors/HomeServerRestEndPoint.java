package nl.homesensors;

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

import nl.homesensors.smartmeter.publisher.HomeServerLocalSmartMeterPublisher;

@Component
public class HomeServerRestEndPoint {

    private static final Logger LOGGER = LoggerFactory.getLogger(HomeServerLocalSmartMeterPublisher.class);

    private final String homeServerRestServiceUrl;
    private final HomeServerAuthentication homeServerAuthentication;

    public HomeServerRestEndPoint(final HomeServerAuthentication homeServerAuthentication,
                                  @Value("${home-server-local-rest-service-url:#{null}}") final String homeServerRestServiceUrl) {
        this.homeServerAuthentication = homeServerAuthentication;
        this.homeServerRestServiceUrl = homeServerRestServiceUrl;
    }

    public void post(final String path, final String jsonString) {
        final String url = homeServerRestServiceUrl + "/" + path;
        LOGGER.debug("Post to url: {}. Request body: {}", url, jsonString);

        try (final CloseableHttpClient httpClient = HttpClientBuilder.create().build()) {

            final HttpPost request = new HttpPost(url);
            final StringEntity params = new StringEntity(jsonString, ContentType.APPLICATION_JSON);
            request.setEntity(params);

            homeServerAuthentication.setAuthorizationHeader(request);

            final CloseableHttpResponse response = httpClient.execute(request);

            if (response.getStatusLine().getStatusCode() != HttpStatus.SC_CREATED) {
                throw new RuntimeException("Unexpected statusline: " + response.getStatusLine());
            }
        } catch (final Exception e) {
            LOGGER.info("Post to url [" + url + "] failed.", e);
        }
    }
}
