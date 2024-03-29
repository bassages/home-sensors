package nl.homesensors.smartmeter;

import lombok.extern.slf4j.Slf4j;
import nl.homesensors.smartmeter.publisher.SmartMeterMessagePublisher;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Slf4j
@Component
public class MessageBuffer {
    private final Dsmr422Parser dsmr422Parser;
    private final Collection<SmartMeterMessagePublisher> smartMeterMessagePublishers;

    private final List<String> bufferedLines = new ArrayList<>();

    public MessageBuffer(final Dsmr422Parser dsmr422Parser, final List<SmartMeterMessagePublisher> smartMeterMessagePublishers) {
        this.dsmr422Parser = dsmr422Parser;
        this.smartMeterMessagePublishers = smartMeterMessagePublishers;
        log.debug("Number of SmartMeter publishers: {}", smartMeterMessagePublishers.size());
    }

    public synchronized void addLine(final String line) {

        if (bufferedLines.isEmpty() && !isFirstLineOfP1Message(line)) {
            log.warn("Ignoring line, because it is not a valid header and no previous bufferedLines were received. Ignored line: {}", line);
            return;
        }

        bufferedLines.add(line);

        if (isLastLineOfP1Message(line)) {
            final String message = String.join("\n", bufferedLines);
            try {
                final SmartMeterMessage smartMeterMessage = dsmr422Parser.parse(message);
                smartMeterMessagePublishers.stream()
                                           .filter(SmartMeterMessagePublisher::isEnabled)
                                           .forEach(publisher -> publisher.publish(smartMeterMessage));
            } catch (final Dsmr422Parser.InvalidSmartMeterMessageException | Dsmr422Parser.UnsupportedVersionException e) {
                log.error("Ignoring invalid message: {}", message);
            }
            bufferedLines.clear();
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
