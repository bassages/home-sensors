package nl.homesensors.smartmeter;

import static ch.qos.logback.classic.Level.ERROR;
import static ch.qos.logback.classic.Level.WARN;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import java.nio.charset.Charset;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.NullInputStream;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import ch.qos.logback.classic.spi.LoggingEvent;
import nl.homesensors.LoggingRule;
import nl.homesensors.MessageContaining;

@RunWith(MockitoJUnitRunner.class)
public class SerialSmartMeterReaderTest {

    @InjectMocks
    private SerialSmartMeterReader serialSmartMeterReader;

    @Mock
    private SmartMeterSerialPortConfiguration smartMeterSerialPortConfiguration;
    @Mock
    private MessageBuffer messageBuffer;
    @Mock
    private Runtime runtime;

    @Rule
    public final LoggingRule loggingRule = new LoggingRule(SerialSmartMeterReader.class);

    @Test
    public void givenConfigurationCompleteWhenRunThenCuProcessStarted() throws Exception {
        when(smartMeterSerialPortConfiguration.isComplete()).thenReturn(true);
        when(smartMeterSerialPortConfiguration.getBaudRate()).thenReturn("100");
        when(smartMeterSerialPortConfiguration.getParity()).thenReturn("even");
        when(smartMeterSerialPortConfiguration.getPath()).thenReturn("/dev/ttyUSB0");

        final ArgumentCaptor<String> commandCaptor = ArgumentCaptor.forClass(String.class);

        final Process process = mock(Process.class);
        when(process.getInputStream()).thenReturn(new NullInputStream(0));
        when(process.getErrorStream()).thenReturn(new NullInputStream(0));

        when(runtime.exec(commandCaptor.capture())).thenReturn(process);

        serialSmartMeterReader.run();

        assertThat(commandCaptor.getValue()).isEqualTo("cu -l /dev/ttyUSB0 --speed 100 --parity=even -E q");
    }

    @Test
    public void givenConfigurationCompleteWhenRunThenOutputOfProcessWrittenToMessageBuffer() throws Exception {
        when(smartMeterSerialPortConfiguration.isComplete()).thenReturn(true);

        final Process process = mock(Process.class);
        when(process.getInputStream()).thenReturn(IOUtils.toInputStream("someLine", Charset.defaultCharset().name()));
        when(process.getErrorStream()).thenReturn(new NullInputStream(0));

        when(runtime.exec(anyString())).thenReturn(process);

        serialSmartMeterReader.run();

        verify(messageBuffer).addLine("someLine");
    }

    @Test
    public void givenErrorStreamContainsLinesWhenRunThenLinesLoggedAsError() throws Exception {
        loggingRule.setLevel(ERROR);

        when(smartMeterSerialPortConfiguration.isComplete()).thenReturn(true);

        final Process process = mock(Process.class);
        when(process.getInputStream()).thenReturn(new NullInputStream(0));
        when(process.getErrorStream()).thenReturn(IOUtils.toInputStream("error1\nerror2", Charset.defaultCharset().name()));

        when(runtime.exec(anyString())).thenReturn(process);

        serialSmartMeterReader.run();

        verifyNoInteractions(messageBuffer);
        final List<LoggingEvent> loggedEvents = loggingRule.getLoggedEventCaptor().getAllValues();
        assertThat(loggedEvents).haveExactly(1, new MessageContaining("[ERROR] error1"));
        assertThat(loggedEvents).haveExactly(1, new MessageContaining("[ERROR] error2"));
    }

    @Test
    public void givenConfigurationIncompleteWhenRunThenLoggedAndNotStarted() {
        loggingRule.setLevel(WARN);

        when(smartMeterSerialPortConfiguration.isComplete()).thenReturn(false);

        serialSmartMeterReader.run();

        final List<LoggingEvent> loggedEvents = loggingRule.getLoggedEventCaptor().getAllValues();
        assertThat(loggedEvents).haveExactly(1, new MessageContaining("[WARN] Not started SmartMeterReader, because the configuration for it is incomplete."));
    }

    @Test
    public void givenProcessEndsWithNonZeroExitValueWhenRunThenWarningLogged() throws Exception {
        loggingRule.setLevel(WARN);

        when(smartMeterSerialPortConfiguration.isComplete()).thenReturn(true);

        final Process process = mock(Process.class);
        when(process.getInputStream()).thenReturn(new NullInputStream(0));
        when(process.getErrorStream()).thenReturn(new NullInputStream(0));

        final int returnCodeIndicatingAnError = 999;
        when(process.waitFor()).thenReturn(returnCodeIndicatingAnError);

        when(runtime.exec(anyString())).thenReturn(process);

        serialSmartMeterReader.run();

        final List<LoggingEvent> loggedEvents = loggingRule.getLoggedEventCaptor().getAllValues();
        assertThat(loggedEvents).haveExactly(1, new MessageContaining("[WARN] Unexpected exit value from command. Exit value=[999]"));
    }

    @Test
    public void givenProcessInterruptedWhenRunThenErrorLogged() throws Exception {
        loggingRule.setLevel(ERROR);

        when(smartMeterSerialPortConfiguration.isComplete()).thenReturn(true);

        final Process process = mock(Process.class);
        when(process.getInputStream()).thenReturn(new NullInputStream(0));
        when(process.getErrorStream()).thenReturn(new NullInputStream(0));

        when(process.waitFor()).thenThrow(new InterruptedException("Interrupt!"));

        when(runtime.exec(anyString())).thenReturn(process);

        serialSmartMeterReader.run();

        final List<LoggingEvent> loggedEvents = loggingRule.getLoggedEventCaptor().getAllValues();
        assertThat(loggedEvents).haveExactly(1, new MessageContaining("[ERROR] An unexpected InterruptedException occurred."));
    }
}
