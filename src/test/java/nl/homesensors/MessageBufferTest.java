package nl.homesensors;

import ch.qos.logback.classic.spi.LoggingEvent;
import nl.homesensors.smartmeter.Dsmr422Parser;
import nl.homesensors.smartmeter.MessageBuffer;
import nl.homesensors.smartmeter.SmartMeterMessage;
import nl.homesensors.smartmeter.publisher.SmartMeterMessagePublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;

import static ch.qos.logback.classic.Level.ERROR;
import static ch.qos.logback.classic.Level.WARN;
import static java.nio.charset.Charset.defaultCharset;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MessageBufferTest {

    @Mock
    private SmartMeterMessagePublisher smartMeterMessagePublisher;
    @Mock
    private Dsmr422Parser dsmr422Parser;

    @Mock
    private SmartMeterMessage smartMeterMessage;

    private MessageBuffer messageBuffer;

    @BeforeEach
    void setup() {
        messageBuffer = new MessageBuffer(dsmr422Parser, singletonList(smartMeterMessagePublisher));
    }

    @Test
    void shouldParseSingleMessage() throws Exception {
        when(smartMeterMessagePublisher.isEnabled()).thenReturn(true);
        when(dsmr422Parser.parse(anyString())).thenReturn(smartMeterMessage);

        try (final Stream<String> lines = Files.lines(Paths.get(this.getClass().getResource("message-4.2.2-1.txt").toURI()), defaultCharset())) {
            lines.forEach(line -> messageBuffer.addLine(line));
        }
        assertThat(messageBuffer.getBufferedLinesSize()).isZero();

        verify(smartMeterMessagePublisher, times(1)).publish(smartMeterMessage);
    }

    @Test
    void shouldParseMultipleMessages() throws Exception {
        // given
        when(smartMeterMessagePublisher.isEnabled()).thenReturn(true);
        when(dsmr422Parser.parse(anyString())).thenReturn(smartMeterMessage);

        try (final Stream<String> lines = Files.lines(Paths.get(this.getClass().getResource("message-4.2.2-1.txt").toURI()), defaultCharset())) {
            lines.forEach(line -> messageBuffer.addLine(line));
        }
        assertThat(messageBuffer.getBufferedLinesSize()).isZero();

        // when
        try (final Stream<String> lines = Files.lines(Paths.get(this.getClass().getResource("message-4.2.2-2.txt").toURI()), defaultCharset())) {
            lines.forEach(line -> messageBuffer.addLine(line));
        }

        // then
        assertThat(messageBuffer.getBufferedLinesSize()).isZero();

        verify(smartMeterMessagePublisher, times(2)).publish(any(SmartMeterMessage.class));
        verify(dsmr422Parser, times(2)).parse(anyString());
    }

    @Test
    @CaptureLogging(MessageBuffer.class)
    void whenMessageIsInvalidThenItsLoggedAndIgnored(
            final ArgumentCaptor<LoggingEvent> loggerEventCaptor) throws Exception {
        // when
        when(dsmr422Parser.parse(anyString())).thenThrow(new Dsmr422Parser.InvalidSmartMeterMessageException("Noooooo!"));

        messageBuffer.addLine("/bullSh#tLine1"); // Because line starts with "/", it's considered to be the first line of a message
        messageBuffer.addLine("bullSh#tLine2");
        messageBuffer.addLine("!bullSh#tLine3"); // Because line starts with "!", it's considered to be the last line of a message

        final List<LoggingEvent> loggedEvents = loggerEventCaptor.getAllValues();
        final String expectedMessage = "Ignoring invalid message: /bull";
        assertThat(loggedEvents).haveExactly(1, new ContainsMessageAtLevel(expectedMessage, ERROR));

        // then
        assertThat(messageBuffer.getBufferedLinesSize()).isZero();
    }

    @Test
    @CaptureLogging(MessageBuffer.class)
    void whenNoProperHeaderHasBeenSentThenLinesAreNotAccepted(final ArgumentCaptor<LoggingEvent> loggerEventCaptor) {
        // when
        messageBuffer.addLine("Hello this is not a valid header");

        // then
        final List<LoggingEvent> loggedEvents = loggerEventCaptor.getAllValues();
        final String expectedMessage = "Ignoring line, because it is not a valid header and no previous bufferedLines were received. Ignored line: Hello this is not a valid header";
        assertThat(loggedEvents).haveExactly(1, new ContainsMessageAtLevel(expectedMessage, WARN));

        assertThat(messageBuffer.getBufferedLinesSize()).isZero();
    }
}
