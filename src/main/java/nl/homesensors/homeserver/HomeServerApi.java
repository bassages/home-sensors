package nl.homesensors.homeserver;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import javax.inject.Provider;

import static java.lang.String.format;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Slf4j
@Component
@RequiredArgsConstructor
public class HomeServerApi {

    private final HomeServerApiConfig homeServerApiConfig;
    private final Provider<HttpClientBuilder> httpClientBuilderProvider;

    public boolean isEnabled() {
        return isNotBlank(homeServerApiConfig.getUrl());
    }

    public void post(final String path, final String json) {
        final String url = homeServerApiConfig.getUrl() + "/" + path;
        log.debug("Post to url: {}. Request body: {}", url, json);

        try (final CloseableHttpClient httpClient = httpClientBuilderProvider.get().build()) {
            final HttpPost request = new HttpPost(url);
            final StringEntity params = new StringEntity(json, ContentType.APPLICATION_JSON);
            request.setEntity(params);
            setAuthorizationHeader(request);
            final CloseableHttpResponse response = httpClient.execute(request);

            Assert.isTrue(response.getStatusLine().getStatusCode() == HttpStatus.SC_CREATED,
                    format("Unexpected statusline: %s", response.getStatusLine()));

        } catch (final Exception e) {
            log.info(format("Post to url [%s] failed.", url), e);
        }
    }

    private void setAuthorizationHeader(final HttpPost request) {
        if (nonNull(homeServerApiConfig.getBasicAuthUser()) && nonNull(homeServerApiConfig.getBasicAuthPassword())) {
            final String auth = homeServerApiConfig.getBasicAuthUser() + ":" + homeServerApiConfig.getBasicAuthPassword();
            final byte[] encodedAuth = Base64.encodeBase64(auth.getBytes(UTF_8));
            final String authorizationHeader = "Basic " + new String(encodedAuth);
            request.setHeader(HttpHeaders.AUTHORIZATION, authorizationHeader);
        }
    }
}
