package nl.homesensors.homeserver;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.HttpResponseException;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpHeaders;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.springframework.stereotype.Component;

import java.util.Base64;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Slf4j
@Component
@RequiredArgsConstructor
public class HomeServerApi {

    private final HomeServerApiConfig homeServerApiConfig;
    private final HttpClientBuilder httpClientBuilder;

    public boolean isEnabled() {
        return isNotBlank(homeServerApiConfig.url());
    }

    public void post(final String path, final String json) {
        final String url = homeServerApiConfig.url() + path;
        log.debug("Post to url: {}. Request body: {}", url, json);

        try (final CloseableHttpClient httpClient = httpClientBuilder.build()) {
            final HttpPost request = new HttpPost(url);
            final StringEntity params = new StringEntity(json, ContentType.APPLICATION_JSON);

            request.setEntity(params);
            setAuthorizationHeader(request);

            httpClient.execute(request, response -> {
                if (response.getCode() != HttpStatus.SC_CREATED) {
                    throw new HttpResponseException(
                            response.getCode(),
                            String.format("Unexpected HTTP status: %s", response.getReasonPhrase())
                    );
                }
                return null;
            });

        } catch (final Exception e) {
            log.error(String.format("Post to url [%s] failed.", url), e);
        }
    }

    private void setAuthorizationHeader(final HttpPost request) {
        if (nonNull(homeServerApiConfig.basicAuthUser()) && nonNull(homeServerApiConfig.basicAuthPassword())) {
            final String auth = homeServerApiConfig.basicAuthUser() + ":" + homeServerApiConfig.basicAuthPassword();
            final byte[] encodedAuth = Base64.getEncoder().encode(auth.getBytes(UTF_8));
            final String authorizationHeader = "Basic " + new String(encodedAuth);
            request.setHeader(HttpHeaders.AUTHORIZATION, authorizationHeader);
        }
    }
}
