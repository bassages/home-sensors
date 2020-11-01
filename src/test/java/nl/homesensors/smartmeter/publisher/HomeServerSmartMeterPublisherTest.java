package nl.homesensors.smartmeter.publisher;

import nl.homesensors.HomeServerRestEndPoint;
import nl.homesensors.smartmeter.SmartMeterMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HomeServerSmartMeterPublisherTest {

    @InjectMocks
    private HomeServerSmartMeterPublisher homeServerSmartMeterPublisher;

    @Mock
    private HomeServerRestEndPoint homeServerRestEndPoint;
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
        verify(homeServerRestEndPoint).post("slimmemeter", json);
    }
}
