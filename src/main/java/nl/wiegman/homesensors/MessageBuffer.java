package nl.wiegman.homesensors;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MessageBuffer {
    private static final Logger LOG = LoggerFactory.getLogger(MessageBuffer.class);

    private final List<String> bufferedLines = new ArrayList<>();

    @Autowired
    private SmartMeterMessageParser smartMeterMessageParser;

    @Autowired
    private HomeServerSmartMeterPublisher homeServerSmartMeterPublisher;

    public synchronized void addLine(String line) {

        if (bufferedLines.isEmpty() && !isFirstLineOfP1Message(line)) {
            LOG.error("Ignoring line, because it is not a valid header and no previous bufferedLines were received. Ignored line: " + line);
        } else {
            bufferedLines.add(line);

            if (isLastLineOfP1Message(line)) {
                String p1Message = bufferedLines.stream().collect(Collectors.joining("\n"));
                try {
                    SmartMeterMessage smartMeterMessage = smartMeterMessageParser.parse(p1Message);
                    homeServerSmartMeterPublisher.publish(smartMeterMessage);
                    bufferedLines.clear();
                } catch (SmartMeterMessageParser.InvalidSmartMeterMessageException e) {
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
