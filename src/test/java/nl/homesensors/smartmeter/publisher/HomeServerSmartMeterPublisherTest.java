package nl.homesensors.smartmeter.publisher;

import ch.qos.logback.classic.spi.LoggingEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import nl.homesensors.CaptureLogging;
import nl.homesensors.ContainsMessageAtLevel;
import nl.homesensors.homeserver.HomeServerApi;
import nl.homesensors.smartmeter.LongPowerFailureLogItem;
import nl.homesensors.smartmeter.SmartMeterMessage;
import nl.homesensors.smartmeter.SmartMeterMessage.DstIndicator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.Month;

import static ch.qos.logback.classic.Level.ERROR;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HomeServerSmartMeterPublisherTest {

    @InjectMocks
    private HomeServerSmartMeterPublisher homeServerSmartMeterPublisher;

    @Mock
    private HomeServerApi homeServerApi;
    @Mock
    private HomeServerSmartMeterMessageFactory homeServerSmartMeterMessageFactory;

    @Test
    void whenPublishThenMessageCreatedAndPostedToSlimmeMeterApiOfHomeServer() throws Exception {
        // given
        final SmartMeterMessage message = mock(SmartMeterMessage.class);
        final String json = "VerySmartMessage";

        when(homeServerSmartMeterMessageFactory.create(message)).thenReturn(json);

        // when
        homeServerSmartMeterPublisher.publish(message);

        // then
        verify(homeServerApi).post("slimmemeter", json);
    }

    @Test
    @CaptureLogging(HomeServerSmartMeterPublisher.class)
    void givenMessageFactoryThrowsJsonProcessingExceptionWhenPublishThenErrorIsLogged(
            final ArgumentCaptor<LoggingEvent> loggerEventCaptor) throws Exception {
        // given
        final SmartMeterMessage message = new SmartMeterMessage();
        final LongPowerFailureLogItem logItem = new LongPowerFailureLogItem();
        logItem.setFailureDurationInSeconds(60);
        logItem.setTimestampOfEndOfFailure(LocalDate.of(2020, Month.JANUARY, 8).atStartOfDay());
        logItem.setTimestampOfEndOfFailureDstIndicator(DstIndicator.SUMMER);
        message.addLongPowerFailureLogItem(logItem);

        final JsonProcessingException jsonProcessingException = mock(JsonProcessingException.class);
        when(homeServerSmartMeterMessageFactory.create(message)).thenThrow(jsonProcessingException);

        // when
        homeServerSmartMeterPublisher.publish(message);

        // then
        assertThat(loggerEventCaptor.getAllValues())
                .haveExactly(1, new ContainsMessageAtLevel("Failed to map message to json. Message=", ERROR));

    }

    @Test
    void whenIsEnabledThenDeterminedByCallingHomeServerRestEndPoint() {
        when(homeServerApi.isEnabled()).thenReturn(true);

        // when
        final boolean enabled = homeServerSmartMeterPublisher.isEnabled();

        // then
        assertThat(enabled).isTrue();
        verify(homeServerApi).isEnabled();
    }

}
