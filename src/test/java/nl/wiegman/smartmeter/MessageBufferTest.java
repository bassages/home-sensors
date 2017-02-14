package nl.wiegman.smartmeter;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class MessageBufferTest {

    @Mock
    private HomeServerSmartMeterPublisher homeServerSmartMeterPublisherMock;

    @InjectMocks
    private MessageBuffer messageBuffer;

    @Test
    public void happyFlow() throws Exception {
        try (Stream<String> lines = Files.lines(Paths.get(this.getClass().getResource("message-4.2.2-1.txt").toURI()), Charset.defaultCharset())) {
            lines.forEach(line -> messageBuffer.addLine(line));
        }
        assertThat(messageBuffer.getPendingLinesSize()).isEqualTo(0);

        try (Stream<String> lines = Files.lines(Paths.get(this.getClass().getResource("message-4.2.2-2.txt").toURI()), Charset.defaultCharset())) {
            lines.forEach(line -> messageBuffer.addLine(line));
        }

        assertThat(messageBuffer.getPendingLinesSize()).isEqualTo(0);

        verify(homeServerSmartMeterPublisherMock, times(2)).publish((SmartMeterMessage) Matchers.anyObject());
    }

    @Test
    public void linesNotAcceptedWhenNoProperHeaderHasBeenSent() throws Exception {
        messageBuffer.addLine("Hello this is not a valid header");
        assertThat(messageBuffer.getPendingLinesSize()).isEqualTo(0);
    }

}