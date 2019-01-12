package nl.homesensors;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

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
import org.springframework.util.Assert;

@Component
public class AwsApiGateway {

    private static final Logger LOGGER = LoggerFactory.getLogger(AwsApiGateway.class);

    private final String apiRootUrl;
    private final Provider<HttpClientBuilder> httpClientBuilderProvider;

    public AwsApiGateway(@Value("${aws-lambda-api.url:#{null}}") final String apiRootUrl,
                         final Provider<HttpClientBuilder> httpClientBuilderProvider) {
        this.apiRootUrl = apiRootUrl;
        this.httpClientBuilderProvider = httpClientBuilderProvider;
    }

    public boolean isEnabled() {
        return isNotBlank(apiRootUrl);
    }

    public void post(final String path, final String json) {
        final String url = apiRootUrl + "/" + path;
        LOGGER.debug("Post to url: {}. Request body: {}", url, json);

        try (final CloseableHttpClient httpClient = httpClientBuilderProvider.get().build()) {
            final HttpPost request = new HttpPost(url);
            final StringEntity params = new StringEntity(json, ContentType.APPLICATION_JSON);
            request.setEntity(params);

            final CloseableHttpResponse response = httpClient.execute(request);

            Assert.isTrue(response.getStatusLine().getStatusCode() == HttpStatus.SC_OK,
                          "Unexpected statusline: " + response.getStatusLine());

        } catch (final Exception e) {
            LOGGER.info("Post to url [" + url + "] failed.", e);
        }
    }
}
