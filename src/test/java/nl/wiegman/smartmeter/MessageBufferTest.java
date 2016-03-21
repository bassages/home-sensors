package nl.wiegman.smartmeter;

import org.junit.Test;

import java.net.URI;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Stream;
import static org.assertj.core.api.Assertions.*;

public class MessageBufferTest {

    private MessageBuffer messageBuffer = new MessageBuffer();

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
    }
}