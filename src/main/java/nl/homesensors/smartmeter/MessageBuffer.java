package nl.homesensors.smartmeter;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import nl.homesensors.smartmeter.publisher.SmartMeterMessagePublisher;

@Component
public class MessageBuffer {
    private static final Logger LOG = LoggerFactory.getLogger(MessageBuffer.class);

    private final Dsmr422Parser dsmr422Parser;
    private final List<SmartMeterMessagePublisher> smartMeterMessagePublishers;

    private final List<String> bufferedLines = new ArrayList<>();

    public MessageBuffer(final Dsmr422Parser dsmr422Parser, final List<SmartMeterMessagePublisher> smartMeterMessagePublishers) {
        this.dsmr422Parser = dsmr422Parser;
        this.smartMeterMessagePublishers = smartMeterMessagePublishers;
    }

    public synchronized void addLine(final String line) {

        if (bufferedLines.isEmpty() && !isFirstLineOfP1Message(line)) {
            LOG.error("Ignoring line, because it is not a valid header and no previous bufferedLines were received. Ignored line: {}", line);
        } else {
            bufferedLines.add(line);

            if (isLastLineOfP1Message(line)) {
                final String p1Message = String.join("\n", bufferedLines);
                try {
                    final SmartMeterMessage smartMeterMessage = dsmr422Parser.parse(p1Message);
                    smartMeterMessagePublishers.forEach(publisher -> publisher.publish(smartMeterMessage));
                    bufferedLines.clear();
                } catch (final Dsmr422Parser.InvalidSmartMeterMessageException | Dsmr422Parser.UnsupportedVersionException e) {
                    LOG.error("Invalid smartmetermessage: {}", p1Message);
                }
            }
        }
    }

    private boolean isFirstLineOfP1Message(final String line) {
        return line.startsWith("/");
    }

    private boolean isLastLineOfP1Message(final String line) {
        return line.startsWith("!");
    }

    public int getBufferedLinesSize() {
        return bufferedLines.size();
    }
}
