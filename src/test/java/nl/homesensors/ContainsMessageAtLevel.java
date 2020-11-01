package nl.homesensors;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.LoggingEvent;
import org.assertj.core.api.Condition;

public class ContainsMessageAtLevel extends Condition<LoggingEvent> {
    private final Level expectedLevel;
    private final String expectedMessage;

    public ContainsMessageAtLevel(String expectedMessage, final Level expectedLevel) {
        super("Contains \"" + expectedMessage + "\" at level " + expectedLevel.toString());
        this.expectedMessage = expectedMessage;
        this.expectedLevel = expectedLevel;
    }

    @Override
    public boolean matches(final LoggingEvent loggingEvent) {
        return loggingEvent.getFormattedMessage().contains(expectedMessage) && loggingEvent.getLevel() == expectedLevel;
    }
}
