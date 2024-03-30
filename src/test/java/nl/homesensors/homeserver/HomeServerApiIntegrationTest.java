package nl.homesensors.homeserver;

import ch.qos.logback.classic.spi.LoggingEvent;
import com.github.tomakehurst.wiremock.client.BasicCredentials;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import nl.homesensors.CaptureLogging;
import nl.homesensors.ContainsMessageAtLevel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.util.List;

import static ch.qos.logback.classic.Level.ERROR;
import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class HomeServerApiIntegrationTest {

    private static final String API_PATH = "/api/some-endpoint";
    private static final String SOME_JSON_BODY = """
                {
                    "hello": "api"
                }""";

    @RegisterExtension
    static WireMockExtension wiremock = WireMockExtension.newInstance()
            .options(wireMockConfig())
            .build();

    @Nested
    @DisplayName("With basic auth enabled")
    class WithBasicAuthEnabled {

        private static final String BASIC_USERNAME = "johndoe";
        private static final String BASIC_PASSWORD = "supersecret";

        @DynamicPropertySource
        static void registerProperties(final DynamicPropertyRegistry  registry) {
            registry.add("home-sensors.home-server.api.url", () -> wiremock.baseUrl());
            registry.add("home-sensors.home-server.api.basic-auth-user", () -> BASIC_USERNAME);
            registry.add("home-sensors.home-server.api.basic-auth-password", () -> BASIC_PASSWORD);
        }

        @Test
        @CaptureLogging(HomeServerApi.class)
        void post_shouldPostToHomeServerApi(
                @Autowired final HomeServerApi homeServerApi,
                final ArgumentCaptor<LoggingEvent> loggerEventCaptor) {

            // given
            stubFor(post(API_PATH)
                    .willReturn(created().withHeader("Connection", "close")));

            // when
            homeServerApi.post(API_PATH, SOME_JSON_BODY);

            // then
            verify(postRequestedFor(urlEqualTo(API_PATH))
                    .withRequestBody(equalTo(SOME_JSON_BODY))
                    .withBasicAuth(new BasicCredentials(BASIC_USERNAME, BASIC_PASSWORD)));

            final List<LoggingEvent> allErrorLogging = loggerEventCaptor.getAllValues()
                    .stream()
                    .filter(log -> log.getLevel().equals(ERROR))
                    .toList();
            assertThat(allErrorLogging).isEmpty();
        }

        @Test
        @CaptureLogging(HomeServerApi.class)
        void post_shouldLogWhenReturnCodeNotCreated(
                @Autowired final HomeServerApi homeServerApi,
                final ArgumentCaptor<LoggingEvent> loggerEventCaptor) {

            // given
            stubFor(post(API_PATH)
                    .willReturn(serverError().withHeader("Connection", "close")));

            // when
            homeServerApi.post(API_PATH, SOME_JSON_BODY);

            // then
            verify(postRequestedFor(urlEqualTo(API_PATH))
                    .withRequestBody(equalTo(SOME_JSON_BODY))
                    .withBasicAuth(new BasicCredentials(BASIC_USERNAME, BASIC_PASSWORD)));

            final List<LoggingEvent> loggedEvents = loggerEventCaptor.getAllValues();
            final String expectedMessage = "Post to url [http://localhost:8080/api/some-endpoint] failed: Unexpected HTTP status: 500";
            assertThat(loggedEvents).haveExactly(1, new ContainsMessageAtLevel(expectedMessage, ERROR));
        }
    }
}
