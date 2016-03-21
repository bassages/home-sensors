package nl.wiegman.smartmeter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

@Component
public class MessageBuffer {
    private final Logger logger = LoggerFactory.getLogger(MessageBuffer.class);

    private final List<String> lines = new ArrayList<>();

    public synchronized void addLine(String line) {
        lines.add(line);

        if (line.startsWith("!")) {
            try {
                Dsmr dsmr = new Dsmr(lines.toArray(new String[0]));
                logger.info("received a valid DSRM: " + dsmr.toString());
                lines.clear();
            } catch (Dsmr.InvalidChecksumException e) {
                logger.error("Invalid CRC for message: " + String.join("\n", lines));
            }
        }
    }

    public int getPendingLinesSize() {
        return lines.size();
    }
}
