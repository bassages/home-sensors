package nl.wiegman.homesensors.smartmeter;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import nl.wiegman.homesensors.smartmeter.publisher.SmartMeterMessagePublisher;

@Component
public class MessageBuffer {
    private static final Logger LOG = LoggerFactory.getLogger(MessageBuffer.class);

    private final List<String> bufferedLines = new ArrayList<>();

    private final Dsmr422Parser dsmr422Parser;

    List<SmartMeterMessagePublisher> smartMeterMessagePublishers;

    @Autowired
    public MessageBuffer(Dsmr422Parser dsmr422Parser, List<SmartMeterMessagePublisher> smartMeterMessagePublishers) {

        this.dsmr422Parser = dsmr422Parser;
        this.smartMeterMessagePublishers = smartMeterMessagePublishers;
    }

    public synchronized void addLine(String line) {

        if (bufferedLines.isEmpty() && !isFirstLineOfP1Message(line)) {
            LOG.error("Ignoring line, because it is not a valid header and no previous bufferedLines were received. Ignored line: " + line);
        } else {
            bufferedLines.add(line);

            if (isLastLineOfP1Message(line)) {
                String p1Message = bufferedLines.stream().collect(Collectors.joining("\n"));
                try {
                    SmartMeterMessage smartMeterMessage = dsmr422Parser.parse(p1Message);
                    smartMeterMessagePublishers.forEach(publisher -> publisher.publish(smartMeterMessage));
                    bufferedLines.clear();
                } catch (Dsmr422Parser.InvalidSmartMeterMessageException | Dsmr422Parser.UnsupportedVersionException e) {
                    LOG.error("Invalid smartmetermessage: " + p1Message);
                }
            }
        }
    }

    private boolean isFirstLineOfP1Message(String line) {
        return line.startsWith("/");
    }

    private boolean isLastLineOfP1Message(String line) {
        return line.startsWith("!");
    }

    public int getBufferedLinesSize() {
        return bufferedLines.size();
    }
}
