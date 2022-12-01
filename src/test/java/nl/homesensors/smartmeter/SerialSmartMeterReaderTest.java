package nl.homesensors.smartmeter;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.LoggingEvent;
import nl.homesensors.CaptureLogging;
import nl.homesensors.ContainsMessageAtLevel;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.NullInputStream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.charset.Charset;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SerialSmartMeterReaderTest {

    @InjectMocks
    private SerialSmartMeterReader serialSmartMeterReader;

    @Mock
    private SmartMeterSerialPortConfiguration smartMeterSerialPortConfiguration;
    @Mock
    private MessageBuffer messageBuffer;
    @Mock
    private Runtime runtime;

    @Test
    void givenConfigurationCompleteWhenRunThenCuProcessStarted() throws Exception {
        when(smartMeterSerialPortConfiguration.isComplete()).thenReturn(true);
        when(smartMeterSerialPortConfiguration.baudRate()).thenReturn("100");
        when(smartMeterSerialPortConfiguration.parity()).thenReturn("even");
        when(smartMeterSerialPortConfiguration.path()).thenReturn("/dev/ttyUSB0");

        final ArgumentCaptor<String> commandCaptor = ArgumentCaptor.forClass(String.class);

        final Process process = mock(Process.class);
        when(process.getInputStream()).thenReturn(new NullInputStream(0));
        when(process.getErrorStream()).thenReturn(new NullInputStream(0));

        when(runtime.exec(commandCaptor.capture())).thenReturn(process);

        serialSmartMeterReader.run();

        assertThat(commandCaptor.getValue()).isEqualTo("cu -l /dev/ttyUSB0 --speed 100 --parity=even -E q");
    }

    @Test
    void givenConfigurationCompleteWhenRunThenOutputOfProcessWrittenToMessageBuffer() throws Exception {
        when(smartMeterSerialPortConfiguration.isComplete()).thenReturn(true);

        final Process process = mock(Process.class);
        when(process.getInputStream()).thenReturn(IOUtils.toInputStream("someLine", Charset.defaultCharset().name()));
        when(process.getErrorStream()).thenReturn(new NullInputStream(0));

        when(runtime.exec(anyString())).thenReturn(process);

        serialSmartMeterReader.run();

        verify(messageBuffer).addLine("someLine");
    }

    @Test
    @CaptureLogging(SerialSmartMeterReader.class)
    void givenErrorStreamContainsLinesWhenRunThenLinesLoggedAsError(
            final ArgumentCaptor<LoggingEvent> loggerEventCaptor) throws Exception {

        when(smartMeterSerialPortConfiguration.isComplete()).thenReturn(true);

        final Process process = mock(Process.class);
        when(process.getInputStream()).thenReturn(new NullInputStream(0));
        when(process.getErrorStream()).thenReturn(IOUtils.toInputStream("error1\nerror2", Charset.defaultCharset().name()));

        when(runtime.exec(anyString())).thenReturn(process);

        serialSmartMeterReader.run();

        verifyNoInteractions(messageBuffer);
        final List<LoggingEvent> loggedEvents = loggerEventCaptor.getAllValues();
        assertThat(loggedEvents)
                .haveExactly(1, new ContainsMessageAtLevel("error1", Level.ERROR))
                .haveExactly(1, new ContainsMessageAtLevel("error2", Level.ERROR));
    }

    @Test
    @CaptureLogging(SerialSmartMeterReader.class)
    void givenConfigurationIncompleteWhenRunThenLoggedAndNotStarted(
            final ArgumentCaptor<LoggingEvent> loggerEventCaptor) {

        when(smartMeterSerialPortConfiguration.isComplete()).thenReturn(false);

        serialSmartMeterReader.run();

        final List<LoggingEvent> loggedEvents = loggerEventCaptor.getAllValues();
        assertThat(loggedEvents).haveExactly(1, new ContainsMessageAtLevel("Not started SmartMeterReader, because the configuration for it is incomplete.", Level.WARN));
    }

    @Test
    @CaptureLogging(SerialSmartMeterReader.class)
    void givenProcessEndsWithNonZeroExitValueWhenRunThenWarningLogged(
            final ArgumentCaptor<LoggingEvent> loggerEventCaptor) throws Exception {

        when(smartMeterSerialPortConfiguration.isComplete()).thenReturn(true);

        final Process process = mock(Process.class);
        when(process.getInputStream()).thenReturn(new NullInputStream(0));
        when(process.getErrorStream()).thenReturn(new NullInputStream(0));

        final int returnCodeIndicatingAnError = 999;
        when(process.waitFor()).thenReturn(returnCodeIndicatingAnError);

        when(runtime.exec(anyString())).thenReturn(process);

        serialSmartMeterReader.run();

        final List<LoggingEvent> loggedEvents = loggerEventCaptor.getAllValues();
        assertThat(loggedEvents).haveExactly(1, new ContainsMessageAtLevel("Unexpected exit value from command. Exit value=[999]", Level.WARN));
    }

    @Test
    @CaptureLogging(SerialSmartMeterReader.class)
    void givenProcessInterruptedWhenRunThenErrorLogged(
            final ArgumentCaptor<LoggingEvent> loggerEventCaptor) throws Exception {

        // given
        when(smartMeterSerialPortConfiguration.isComplete()).thenReturn(true);

        final Process process = mock(Process.class);
        when(process.getInputStream()).thenReturn(new NullInputStream(0));
        when(process.getErrorStream()).thenReturn(new NullInputStream(0));

        when(process.waitFor()).thenThrow(new InterruptedException("Interrupt!"));

        when(runtime.exec(anyString())).thenReturn(process);

        // when
        serialSmartMeterReader.run();

        // then
        final List<LoggingEvent> loggedEvents = loggerEventCaptor.getAllValues();
        assertThat(loggedEvents).haveExactly(1, new ContainsMessageAtLevel("An unexpected InterruptedException occurred.", Level.ERROR));
    }
}
