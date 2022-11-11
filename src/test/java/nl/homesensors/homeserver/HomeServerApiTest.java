package nl.homesensors.homeserver;

import ch.qos.logback.classic.spi.LoggingEvent;
import nl.homesensors.CaptureLogging;
import nl.homesensors.ContainsMessageAtLevel;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.inject.Provider;
import java.io.ByteArrayOutputStream;
import java.util.List;

import static ch.qos.logback.classic.Level.INFO;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HomeServerApiTest {

    private static final String HOME_SERVER_REST_API_URL = "http://home-server/api";

    @InjectMocks
    private HomeServerApi homeServerApi;

    @Mock
    private Provider<HttpClientBuilder> httpClientBuilderProvider;
    @Mock
    private HttpClientBuilder httpClientBuilder;
    @Mock
    private HomeServerApiConfig homeServerApiConfig;

    @Mock
    private CloseableHttpClient closeableHttpClient;
    @Mock
    private CloseableHttpResponse closeableHttpResponse;
    @Mock
    private StatusLine statusLine;

    @Captor
    private ArgumentCaptor<HttpUriRequest> httpUriRequestCaptor;

    @BeforeEach
    public void setUp() {
        when(httpClientBuilderProvider.get()).thenReturn(httpClientBuilder);
        when(httpClientBuilder.build()).thenReturn(closeableHttpClient);
        when(homeServerApiConfig.getUrl()).thenReturn(HOME_SERVER_REST_API_URL);
    }

    @Test
    void givenJsonToPostToPathWhenPostThenPosted() throws Exception {
        when(homeServerApiConfig.getBasicAuthUser()).thenReturn("user_1234");
        when(homeServerApiConfig.getBasicAuthPassword()).thenReturn("secret_password");

        when(closeableHttpClient.execute(any())).thenReturn(closeableHttpResponse);
        when(closeableHttpResponse.getStatusLine()).thenReturn(statusLine);
        when(statusLine.getStatusCode()).thenReturn(HttpStatus.SC_CREATED);

        final String json = "{\"ji-ja-json\":100}";
        final String path = "pi-pa-path";
        homeServerApi.post(path, json);

        verify(closeableHttpClient).execute(httpUriRequestCaptor.capture());

        assertThat(httpUriRequestCaptor.getValue()).isExactlyInstanceOf(HttpPost.class);
        final HttpPost httpPost = (HttpPost)httpUriRequestCaptor.getValue();

        assertThat(httpPost.getURI()).hasScheme("http");
        assertThat(httpPost.getURI()).hasHost("home-server");
        assertThat(httpPost.getURI()).hasPath("/api/" + path);
        assertThat(httpPost.getEntity()).isNotNull();
        assertThat(httpPost.getFirstHeader("Authorization")).isNotNull();

        try (final ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            httpPost.getEntity().writeTo(baos);
            assertThat(baos).hasToString(json);
        }
    }

    @Test
    void givenNoUsernamePassworsWhenPostThenPostedWithoutAuthenticationHeader() throws Exception {
        when(closeableHttpClient.execute(any())).thenReturn(closeableHttpResponse);
        when(closeableHttpResponse.getStatusLine()).thenReturn(statusLine);
        when(statusLine.getStatusCode()).thenReturn(HttpStatus.SC_CREATED);

        final String json = "{\"ji-ja-json\":100}";
        final String path = "pi-pa-path";
        homeServerApi.post(path, json);

        verify(closeableHttpClient).execute(httpUriRequestCaptor.capture());

        assertThat(httpUriRequestCaptor.getValue()).isExactlyInstanceOf(HttpPost.class);
        final HttpPost httpPost = (HttpPost)httpUriRequestCaptor.getValue();

        assertThat(httpPost.getFirstHeader("Authorization")).isNull();
    }

    @Test
    @CaptureLogging(HomeServerApi.class)
    void givenEndpointDoesNotReturnStatusCode201WhenPostThenExceptionLogged(
            final ArgumentCaptor<LoggingEvent> loggerEventCaptor) throws Exception {

        when(closeableHttpClient.execute(any())).thenReturn(closeableHttpResponse);
        when(closeableHttpResponse.getStatusLine()).thenReturn(statusLine);

        when(statusLine.getStatusCode()).thenReturn(HttpStatus.SC_BAD_REQUEST);

        homeServerApi.post("somePath", "{\"a\":1}");

        final List<LoggingEvent> loggedEvents = loggerEventCaptor.getAllValues();
        final String expectedMessage = "Post to url [http://home-server/api/somePath] failed.";
        assertThat(loggedEvents).haveExactly(1, new ContainsMessageAtLevel(expectedMessage, INFO));
    }
}
