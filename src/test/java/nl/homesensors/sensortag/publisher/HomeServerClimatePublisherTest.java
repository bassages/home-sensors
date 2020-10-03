package nl.homesensors.sensortag.publisher;

import static java.time.Month.JANUARY;
import static nl.homesensors.util.TimeMachine.timeTravelTo;
import static org.mockito.Mockito.verify;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import nl.homesensors.HomeServerRestEndPoint;
import nl.homesensors.sensortag.Humidity;
import nl.homesensors.sensortag.SensorCode;
import nl.homesensors.sensortag.Temperature;

@RunWith(MockitoJUnitRunner.class)
public class HomeServerClimatePublisherTest {

    @InjectMocks
    private HomeServerClimatePublisher homeServerClimatePublisher;

    @Mock
    private HomeServerRestEndPoint homeServerRestEndPoint;
    @Mock
    private Clock clock;

    @Test
    public void whenPublishThenMessageCreatedAndPostedToSlimmeMeterApiOfHomeServer() {
        timeTravelTo(clock, LocalDate.of(2018, JANUARY, 2).atTime(17, 9));

        homeServerClimatePublisher.publish(SensorCode.of("GARDEN"), Temperature.of(new BigDecimal("10.1")), Humidity.of(new BigDecimal("61.7")));

        final String expectedBody = """
                {"datumtijd":"2018-01-02T17:09:00","temperatuur":10.1,"luchtvochtigheid":61.7}""";
        verify(homeServerRestEndPoint).post("klimaat/sensors/GARDEN", expectedBody);
    }
}
