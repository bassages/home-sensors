package nl.wiegman.sensortag;

import net.sf.expectit.Expect;
import net.sf.expectit.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static net.sf.expectit.matcher.Matchers.regexp;

public abstract class AbstractSensortagSensor {

    private static final Logger LOG = LoggerFactory.getLogger(SensorTagReader.class);

    abstract void enableNotifications(Expect expect) throws IOException;

    abstract void enable(Expect expect) throws IOException;

    abstract void disable(Expect expect) throws IOException;

    abstract String getNotificationPattern();

    String expectSuccesfulMatch(Expect expect, String regexp) throws IOException, SensortagException {
        String value;
        Result result = expect.withTimeout(15, TimeUnit.SECONDS).expect(regexp(regexp));
        if (result.isSuccessful()) {
            value = result.group(1);
        } else {
            throw new SensortagException("No match found. " + result.getInput());
        }
        return value;
    }

    public void discardNotifications(Expect expect) throws IOException {
        while(expect.withTimeout(10, TimeUnit.SECONDS).expect(regexp(getNotificationPattern())).isSuccessful()) {
            LOG.debug("Discarding notification for regexp [" + getNotificationPattern() + "]");
        }
    }

}
