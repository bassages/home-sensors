package nl.homesensors;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import javax.inject.Provider;

import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Slf4j
@Component
public class HomeServerRestEndPoint {

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

    public boolean isEnabled() {
        return isNotBlank(homeServerRestApiUrl);
    }

    public void post(final String path, final String json) {
        final String url = homeServerRestApiUrl + "/" + path;
        log.debug("Post to url: {}. Request body: {}", url, json);

        try (final CloseableHttpClient httpClient = httpClientBuilderProvider.get().build()) {
            final HttpPost request = new HttpPost(url);
            final StringEntity params = new StringEntity(json, ContentType.APPLICATION_JSON);
            request.setEntity(params);

            homeServerAuthentication.setAuthorizationHeader(request);

            final CloseableHttpResponse response = httpClient.execute(request);

            Assert.isTrue(response.getStatusLine().getStatusCode() == HttpStatus.SC_CREATED,
                          format("Unexpected statusline: %s", response.getStatusLine()));

        } catch (final Exception e) {
            log.info(format("Post to url [%s] failed.", url), e);
        }
    }
}
