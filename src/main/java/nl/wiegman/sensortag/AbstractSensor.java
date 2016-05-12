package nl.wiegman.sensortag;

import net.sf.expectit.Expect;
import net.sf.expectit.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static net.sf.expectit.matcher.Matchers.regexp;

public abstract class AbstractSensor {

    private static final Logger LOG = LoggerFactory.getLogger(SensorTagReader.class);

    abstract void enableNotifications(Expect expect) throws IOException;

    abstract void enable(Expect expect) throws IOException;

    abstract void disable(Expect expect) throws IOException;

    String expectSuccesfulMatch(Expect expect, String regexp) throws IOException, SensortagException {
        String value;
        Result result = expect.withTimeout(25, TimeUnit.SECONDS).expect(regexp(regexp));
        if (result.isSuccessful()) {
            value = result.group(1);
        } else {
            throw new SensortagException("No match found for regexp [" + regexp + "] in [" + result.getInput() + "]");
        }
        return value;
    }

    void discardNotifications(Expect expect, String regexp) throws IOException {
        while(expect.withTimeout(20, TimeUnit.SECONDS).expect(regexp(regexp)).isSuccessful()) {
            LOG.debug("Discarding notification handle");
        }
    }

}
