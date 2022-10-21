package nl.homesensors.sensortag;

import lombok.extern.slf4j.Slf4j;
import net.sf.expectit.Expect;
import net.sf.expectit.Result;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static net.sf.expectit.matcher.Matchers.regexp;

// SonarQube: "Abstract classes without fields should be converted to interfaces".
// Ignore because @Slf4j does not work on an interface.
@SuppressWarnings("java:S1610")
@Slf4j
abstract class AbstractSensor {

    abstract void enableNotifications(Expect expect) throws IOException;

    abstract void enable(Expect expect) throws IOException;

    abstract void disable(Expect expect) throws IOException;

    String expectSuccessfulMatch(final Expect expect, final String regexp) throws IOException, SensortagException {
        final String value;
        final Result result = expect.withTimeout(25, TimeUnit.SECONDS).expect(regexp(regexp));
        if (result.isSuccessful()) {
            value = result.group(1);
        } else {
            throw new SensortagException("No match found for regexp [" + regexp + "] in [" + result.getInput() + "]");
        }
        return value;
    }

    void discardNotifications(final Expect expect, final String regexp) throws IOException {
        while(expect.withTimeout(20, TimeUnit.SECONDS).expect(regexp(regexp)).isSuccessful()) {
            log.debug("Discarding notification handle");
        }
    }
}
