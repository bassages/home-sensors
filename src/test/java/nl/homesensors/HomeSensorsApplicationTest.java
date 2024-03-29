package nl.homesensors;

import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class HomeSensorsApplicationTest {

    private HomeSensorsApplication homeSensorsApplication;

    @BeforeEach
    void setUp() {
        homeSensorsApplication = new HomeSensorsApplication();
    }

    @Test
    void whenGetRuntimeThenReturned() {
        final Runtime runtime = homeSensorsApplication.getRuntime();
        assertThat(runtime).isSameAs(Runtime.getRuntime());
    }

    @Test
    void whenGetClockThenSystemDefaultZoneClockReturned() {
        final Clock clock = homeSensorsApplication.getClock();
        assertThat(clock).isEqualTo(Clock.systemDefaultZone());
    }

    @Test
    void whenGetHttpClientBuilderThenReturned() {
        final HttpClientBuilder httpClientBuilder = homeSensorsApplication.getHttpClientBuilder();
        assertThat(httpClientBuilder).isNotNull();
    }
}
