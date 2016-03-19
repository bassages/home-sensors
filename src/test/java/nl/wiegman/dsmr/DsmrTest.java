package nl.wiegman.dsmr;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.io.IOException;

public class DsmrTest {

    @Test
    public void validChecksum() throws IOException, Dsmr.InvalidChecksumException {
        String message = IOUtils.toString(this.getClass().getResourceAsStream("example-message-1.txt"));
        Dsmr dsmr = new Dsmr(message);
    }
}