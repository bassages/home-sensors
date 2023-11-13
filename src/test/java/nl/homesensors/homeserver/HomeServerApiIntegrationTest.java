package nl.homesensors.homeserver;

import ch.qos.logback.classic.spi.LoggingEvent;
import com.github.tomakehurst.wiremock.common.ConsoleNotifier;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import nl.homesensors.CaptureLogging;
import nl.homesensors.ContainsMessageAtLevel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;

import static ch.qos.logback.classic.Level.ERROR;
import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ExtendWith(SpringExtension.class)
public class HomeServerApiIntegrationTest {

    private static final String API_PATH = "/api/some-endpoint";

    @RegisterExtension
    static WireMockExtension wiremock = WireMockExtension.newInstance()
            .options(wireMockConfig()
                    .notifier(new ConsoleNotifier(true))).build();

    @DynamicPropertySource
    static void registerProperties(final DynamicPropertyRegistry  registry) {
        registry.add("home-sensors.home-server.api.url", () -> wiremock.baseUrl());
        registry.add("home-sensors.home-server.api.basic-auth-user", () -> "johndoe");
        registry.add("home-sensors.home-server.api.basic-auth-password", () -> "supersecret");
    }

    @Autowired
    HomeServerApi homeServerApi;

    @CaptureLogging(HomeServerApi.class)
    @Test
    void post_shouldPostToHomeServerApi(final ArgumentCaptor<LoggingEvent> loggerEventCaptor) {
        // given
        stubFor(post(API_PATH)
                .willReturn(created()));

        final String json = """
                {
                    "hello": "api"
                }
                """;
        // when
        homeServerApi.post(API_PATH, json);

        // then
        verify(postRequestedFor(urlEqualTo(API_PATH)).withRequestBody(equalTo(json)));
        final List<LoggingEvent> allErrorLogging = loggerEventCaptor.getAllValues()
                .stream()
                .filter(log -> log.getLevel().equals(ERROR))
                .toList();
        assertThat(allErrorLogging).isEmpty();
    }

    @CaptureLogging(HomeServerApi.class)
    @Test
    void post_shouldLogWhenReturnCodeNotCreated(final ArgumentCaptor<LoggingEvent> loggerEventCaptor) {
        // given
        stubFor(post(API_PATH)
                .willReturn(serverError()));

        final String json = """
                {
                    "body-does-not-matter": "for-this-test"
                }
                """;

        // when
        homeServerApi.post(API_PATH, json);

        // then
        verify(postRequestedFor(urlEqualTo(API_PATH)).withRequestBody(equalTo(json)));

        final List<LoggingEvent> loggedEvents = loggerEventCaptor.getAllValues();
        final String expectedMessage = "Post to url [http://localhost:8080/api/some-endpoint] failed.";
        assertThat(loggedEvents).haveExactly(1, new ContainsMessageAtLevel(expectedMessage, ERROR));
    }
}
