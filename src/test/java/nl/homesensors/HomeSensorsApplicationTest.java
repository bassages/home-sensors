package nl.homesensors;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Clock;

import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class HomeSensorsApplicationTest {

    private HomeSensorsApplication homeSensorsApplication;

    @Before
    public void setUp() {
        homeSensorsApplication = new HomeSensorsApplication();
    }

    @Test
    public void whenGetRuntimeThenReturned() {
        final Runtime runtime = homeSensorsApplication.getRuntime();
        assertThat(runtime).isSameAs(Runtime.getRuntime());
    }

    @Test
    public void whenGetClockThenSystemDefaultZoneClockReturned() {
        final Clock clock = homeSensorsApplication.getClock();
        assertThat(clock).isEqualTo(Clock.systemDefaultZone());
    }

    @Test
    public void whenGetHttpClientBuilderThenReturned() {
        final HttpClientBuilder httpClientBuilder = homeSensorsApplication.getHttpClientBuilder();
        assertThat(httpClientBuilder).isNotNull();
    }
}