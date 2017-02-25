package nl.wiegman.homesensors;

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
    @Mock
    private SmartMeterMessageParser smartMeterMessageParserMock;

    @InjectMocks
    private MessageBuffer messageBuffer;

    @Test
    public void shouldParseSingleMessage() throws Exception {
        try (Stream<String> lines = Files.lines(Paths.get(this.getClass().getResource("message-4.2.2-1.txt").toURI()), Charset.defaultCharset())) {
            lines.forEach(line -> messageBuffer.addLine(line));
        }
        assertThat(messageBuffer.getBufferedLinesSize()).isEqualTo(0);

        verify(homeServerSmartMeterPublisherMock, times(1)).publish((SmartMeterMessage) Matchers.anyObject());
        verify(smartMeterMessageParserMock, times(1)).parse((String)Matchers.anyObject());
    }

    @Test
    public void shouldParseMultipleMessages() throws Exception {
        try (Stream<String> lines = Files.lines(Paths.get(this.getClass().getResource("message-4.2.2-1.txt").toURI()), Charset.defaultCharset())) {
            lines.forEach(line -> messageBuffer.addLine(line));
        }
        assertThat(messageBuffer.getBufferedLinesSize()).isEqualTo(0);

        try (Stream<String> lines = Files.lines(Paths.get(this.getClass().getResource("message-4.2.2-2.txt").toURI()), Charset.defaultCharset())) {
            lines.forEach(line -> messageBuffer.addLine(line));
        }

        assertThat(messageBuffer.getBufferedLinesSize()).isEqualTo(0);

        verify(homeServerSmartMeterPublisherMock, times(2)).publish((SmartMeterMessage) Matchers.anyObject());
        verify(smartMeterMessageParserMock, times(2)).parse((String)Matchers.anyObject());
    }

    @Test
    public void linesNotAcceptedWhenNoProperHeaderHasBeenSent() throws Exception {
        messageBuffer.addLine("Hello this is not a valid header");
        assertThat(messageBuffer.getBufferedLinesSize()).isEqualTo(0);
    }

}