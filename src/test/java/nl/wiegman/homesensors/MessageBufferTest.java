package nl.wiegman.homesensors;

import static java.nio.charset.Charset.defaultCharset;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.stream.Stream;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import nl.wiegman.homesensors.smartmeter.Dsmr422Parser;
import nl.wiegman.homesensors.smartmeter.MessageBuffer;
import nl.wiegman.homesensors.smartmeter.SmartMeterMessage;
import nl.wiegman.homesensors.smartmeter.publisher.SmartMeterMessagePublisher;

@RunWith(MockitoJUnitRunner.class)
public class MessageBufferTest {

    @Mock
    private SmartMeterMessagePublisher smartMeterMessagePublisher;
    @Mock
    private Dsmr422Parser dsmr422ParserMock;

    private MessageBuffer messageBuffer;

    @Before
    public void setup() {
        messageBuffer = new MessageBuffer(dsmr422ParserMock, singletonList(smartMeterMessagePublisher));
    }

    @Test
    public void shouldParseSingleMessage() throws Exception {
        try (Stream<String> lines = Files.lines(Paths.get(this.getClass().getResource("message-4.2.2-1.txt").toURI()), defaultCharset())) {
            lines.forEach(line -> messageBuffer.addLine(line));
        }
        assertThat(messageBuffer.getBufferedLinesSize()).isEqualTo(0);

        verify(smartMeterMessagePublisher, times(1)).publish(any(SmartMeterMessage.class));
        verify(dsmr422ParserMock, times(1)).parse(anyString());
    }

    @Test
    public void shouldParseMultipleMessages() throws Exception {
        try (Stream<String> lines = Files.lines(Paths.get(this.getClass().getResource("message-4.2.2-1.txt").toURI()), defaultCharset())) {
            lines.forEach(line -> messageBuffer.addLine(line));
        }
        assertThat(messageBuffer.getBufferedLinesSize()).isEqualTo(0);

        try (Stream<String> lines = Files.lines(Paths.get(this.getClass().getResource("message-4.2.2-2.txt").toURI()), defaultCharset())) {
            lines.forEach(line -> messageBuffer.addLine(line));
        }

        assertThat(messageBuffer.getBufferedLinesSize()).isEqualTo(0);

        verify(smartMeterMessagePublisher, times(2)).publish(any(SmartMeterMessage.class));
        verify(dsmr422ParserMock, times(2)).parse(anyString());
    }

    @Test
    public void whenNoProperHeaderHasBeenSentThenLinesAreNotAccepted() {
        messageBuffer.addLine("Hello this is not a valid header");
        assertThat(messageBuffer.getBufferedLinesSize()).isEqualTo(0);
    }

}