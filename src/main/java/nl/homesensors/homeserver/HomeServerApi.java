package nl.homesensors.homeserver;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Base64;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.time.temporal.ChronoUnit.SECONDS;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Slf4j
@Component
@RequiredArgsConstructor
public class HomeServerApi {

    private final HomeServerApiConfig homeServerApiConfig;
    private final HttpClient.Builder httpClientBuilder;

    public boolean isEnabled() {
        return isNotBlank(homeServerApiConfig.url());
    }

    public void post(final String path, final String json) {
        final String url = homeServerApiConfig.url() + path;
        log.debug("Post to url: {}. Request body: {}", url, json);

        try (final HttpClient httpClient = httpClientBuilder
                .connectTimeout(Duration.of(20, SECONDS))
                .build()) {

            final HttpRequest.Builder postRequestBuilder = HttpRequest.newBuilder()
                    .uri(new URI(url))
                    .headers("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .timeout(Duration.of(20, SECONDS));
            setAuthorizationHeader(postRequestBuilder);
            final HttpRequest httpRequest = postRequestBuilder.build();

            final HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 201) {
                log.error(String.format("Post to url [%s] failed: Unexpected HTTP status: %s".formatted(url, response.statusCode())));
            }

        } catch (final URISyntaxException | IOException | InterruptedException e) {
            log.error(String.format("Post to url [%s] failed.", url), e);
        }
    }

    private void setAuthorizationHeader(final HttpRequest.Builder postRequestBuilder) {
        if (nonNull(homeServerApiConfig.basicAuthUser()) && nonNull(homeServerApiConfig.basicAuthPassword())) {
            final String auth = homeServerApiConfig.basicAuthUser() + ":" + homeServerApiConfig.basicAuthPassword();
            final byte[] encodedAuth = Base64.getEncoder().encode(auth.getBytes(UTF_8));
            final String authorizationHeader = "Basic " + new String(encodedAuth);
            postRequestBuilder.header("Authorization", authorizationHeader);
        }
    }
}
