package nl.homesensors.smartmeter.publisher;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import nl.homesensors.HomeServerRestEndPoint;
import nl.homesensors.smartmeter.SmartMeterMessage;

@RunWith(MockitoJUnitRunner.class)
public class HomeServerSmartMeterPublisherTest {

    @InjectMocks
    private HomeServerSmartMeterPublisher homeServerSmartMeterPublisher;

    @Mock
    private HomeServerRestEndPoint homeServerRestEndPoint;
    @Mock
    private HomeServerSmartMeterMessageFactory homeServerSmartMeterMessageFactory;

    @Test
    public void whenPublishThenMessageCreatedAndPostedToSlimmeMeterApiOfHomeServer() throws Exception {
        final SmartMeterMessage message = mock(SmartMeterMessage.class);
        final String json = "VerySmartMessage";

        when(homeServerSmartMeterMessageFactory.create(message)).thenReturn(json);

        homeServerSmartMeterPublisher.publish(message);

        verify(homeServerRestEndPoint).post("slimmemeter", json);
    }
}