package nl.wiegman.smartmeter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class MessageBuffer {
    private static final Logger LOG = LoggerFactory.getLogger(MessageBuffer.class);

    private final List<String> lines = new ArrayList<>();

    @Autowired
    private SmartMeterMessagePersister smartMeterMessagePersister;

    public synchronized void addLine(String line) {

        if (lines.isEmpty() && !line.startsWith("/")) {
            LOG.error("Ignoring line, because it is not a valid header and no previous lines were received. Ignored line: " + line);
        } else {
            lines.add(line);

            if (line.startsWith("!")) {
                try {
                    SmartMeterMessage message = new SmartMeterMessage(lines.toArray(new String[0]));
                    smartMeterMessagePersister.persist(message);
                    lines.clear();
                } catch (SmartMeterMessage.InvalidSmartMeterMessageException e) {
                    LOG.error("Invalid CRC for message: " + String.join("\n", lines));
                }
            }
        }
    }

    public int getPendingLinesSize() {
        return lines.size();
    }
}
