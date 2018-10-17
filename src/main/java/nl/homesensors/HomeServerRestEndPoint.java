package nl.homesensors;

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
import org.springframework.stereotype.Component;

@Component
public class HomeServerRestEndPoint {

    private static final Logger LOGGER = LoggerFactory.getLogger(HomeServerRestEndPoint.class);

    private final String homeServerRestApiUrl;
    private final HomeServerAuthentication homeServerAuthentication;
    private final Provider<HttpClientBuilder> httpClientBuilderProvider;

    public HomeServerRestEndPoint(final HomeServerAuthentication homeServerAuthentication,
                                  @Value("${home-server-local-rest-service-url:#{null}}") final String homeServerRestApiUrl,
                                  final Provider<HttpClientBuilder> httpClientBuilderProvider) {
        this.homeServerAuthentication = homeServerAuthentication;
        this.homeServerRestApiUrl = homeServerRestApiUrl;
        this.httpClientBuilderProvider = httpClientBuilderProvider;
    }

    public void post(final String path, final String json) {
        final String url = homeServerRestApiUrl + "/" + path;
        LOGGER.debug("Post to url: {}. Request body: {}", url, json);

        try (final CloseableHttpClient httpClient = httpClientBuilderProvider.get().build()) {
            final HttpPost request = new HttpPost(url);
            final StringEntity params = new StringEntity(json, ContentType.APPLICATION_JSON);
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
