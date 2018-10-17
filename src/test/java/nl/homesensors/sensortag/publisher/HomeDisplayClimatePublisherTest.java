package nl.homesensors.sensortag.publisher;

import static nl.homesensors.util.TimeMachine.timeTravelTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;

import javax.inject.Provider;

import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import nl.homesensors.sensortag.Humidity;
import nl.homesensors.sensortag.SensorCode;
import nl.homesensors.sensortag.Temperature;

@RunWith(MockitoJUnitRunner.class)
public class HomeDisplayClimatePublisherTest {

    private static final String HOME_SERVER_DISPLAY_KLIMAAT_URL = "http://home-display/klimaat";

    private HomeDisplayClimatePublisher homeDisplayClimatePublisher;

    @Mock
    private Provider<HttpClientBuilder> httpClientBuilderProvider;
    @Mock
    private HttpClientBuilder httpClientBuilder;
    @Mock
    private Clock clock;

    @Mock
    private CloseableHttpClient closeableHttpClient;
    @Mock
    private CloseableHttpResponse closeableHttpResponse;
    @Mock
    private StatusLine statusLine;

    @Captor
    private ArgumentCaptor<HttpUriRequest> httpUriRequestCaptor;

    @Before
    public void setUp() {
        homeDisplayClimatePublisher = new HomeDisplayClimatePublisher(HOME_SERVER_DISPLAY_KLIMAAT_URL, httpClientBuilderProvider, clock);
        when(httpClientBuilderProvider.get()).thenReturn(httpClientBuilder);
        when(httpClientBuilder.build()).thenReturn(closeableHttpClient);
    }

    @Test
    public void givenSomeValidValuesThenPublishThenPostRequestSent() throws Exception {
        timeTravelTo(clock, LocalDate.of(2018, 1, 2).atTime(17, 9));

        when(closeableHttpClient.execute(any())).thenReturn(closeableHttpResponse);
        when(closeableHttpResponse.getStatusLine()).thenReturn(statusLine);
        when(statusLine.getStatusCode()).thenReturn(HttpStatus.SC_CREATED);

        homeDisplayClimatePublisher.publish(SensorCode.of("GARDEN"),
                                            Temperature.of(new BigDecimal("21.9")),
                                            Humidity.of(new BigDecimal("56.01")));

        verify(closeableHttpClient).execute(httpUriRequestCaptor.capture());

        assertThat(httpUriRequestCaptor.getValue()).isExactlyInstanceOf(HttpPost.class);
        final HttpPost httpPost = (HttpPost)httpUriRequestCaptor.getValue();

        assertThat(httpPost.getURI()).hasScheme("http");
        assertThat(httpPost.getURI()).hasHost("home-display");
        assertThat(httpPost.getURI()).hasPath("/klimaat");

        assertThat(httpPost.getEntity()).isNotNull();

        try (final ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            httpPost.getEntity().writeTo(baos);
            final String actual = baos.toString();
            assertThat(actual).contains("\"datumtijd\":1514909340000");
            assertThat(actual).contains("\"temperatuur\":21.9");
            assertThat(actual).contains("\"luchtvochtigheid\":56.01");
        }
    }
}