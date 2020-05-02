package nl.homesensors;

import static ch.qos.logback.classic.Level.INFO;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayOutputStream;
import java.util.List;

import javax.inject.Provider;

import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import ch.qos.logback.classic.spi.LoggingEvent;

@RunWith(MockitoJUnitRunner.class)
public class HomeServerRestEndPointTest {

    private static final String HOME_SERVER_REST_API_URL = "http://home-server/api";

    private HomeServerRestEndPoint homeServerRestEndPoint;

    @Mock
    private Provider<HttpClientBuilder> httpClientBuilderProvider;
    @Mock
    private HttpClientBuilder httpClientBuilder;
    @Mock
    private HomeServerAuthentication homeServerAuthentication;

    @Mock
    private CloseableHttpClient closeableHttpClient;
    @Mock
    private CloseableHttpResponse closeableHttpResponse;
    @Mock
    private StatusLine statusLine;

    @Captor
    private ArgumentCaptor<HttpUriRequest> httpUriRequestCaptor;

    @Rule
    public final LoggingRule loggingRule = new LoggingRule(HomeServerRestEndPoint.class);

    @Before
    public void setUp() {
        homeServerRestEndPoint = new HomeServerRestEndPoint(homeServerAuthentication, HOME_SERVER_REST_API_URL, httpClientBuilderProvider);
        when(httpClientBuilderProvider.get()).thenReturn(httpClientBuilder);
        when(httpClientBuilder.build()).thenReturn(closeableHttpClient);
    }

    @Test
    public void givenJsonToPostToPathWhenPostThenPosted() throws Exception {
        when(closeableHttpClient.execute(any())).thenReturn(closeableHttpResponse);
        when(closeableHttpResponse.getStatusLine()).thenReturn(statusLine);
        when(statusLine.getStatusCode()).thenReturn(HttpStatus.SC_CREATED);

        final String json = "{\"ji-ja-json\":100}";
        final String path = "pi-pa-path";
        homeServerRestEndPoint.post(path, json);

        verify(closeableHttpClient).execute(httpUriRequestCaptor.capture());

        assertThat(httpUriRequestCaptor.getValue()).isExactlyInstanceOf(HttpPost.class);
        final HttpPost httpPost = (HttpPost)httpUriRequestCaptor.getValue();

        assertThat(httpPost.getURI()).hasScheme("http");
        assertThat(httpPost.getURI()).hasHost("home-server");
        assertThat(httpPost.getURI()).hasPath("/api/" + path);

        assertThat(httpPost.getEntity()).isNotNull();

        try (final ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            httpPost.getEntity().writeTo(baos);
            assertThat(baos.toString()).isEqualTo(json);
        }

        verify(homeServerAuthentication).setAuthorizationHeader(httpPost);
    }

    @Test
    public void givenEndpointDoesNotReturnStatusCode201WhenPostThenExceptionLogged() throws Exception {
        when(closeableHttpClient.execute(any())).thenReturn(closeableHttpResponse);
        when(closeableHttpResponse.getStatusLine()).thenReturn(statusLine);

        when(statusLine.getStatusCode()).thenReturn(HttpStatus.SC_BAD_REQUEST);

        loggingRule.setLevel(INFO);

        homeServerRestEndPoint.post("somePath", "{\"a\":1}");

        final List<LoggingEvent> loggedEvents = loggingRule.getLoggedEventCaptor().getAllValues();
        assertThat(loggedEvents).haveExactly(1, new MessageContaining("[INFO] Post to url [http://home-server/api/somePath] failed."));
    }
}
