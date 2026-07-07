package nl.homesensors;

import nl.homesensors.smartmeter.DsmrParser;
import nl.homesensors.smartmeter.SmartMeterMessage;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class DsmrParserTest {

    private DsmrParser dsmrParser;

    @BeforeEach
    void setup() {
        this.dsmrParser = new DsmrParser();
    }

    private String readMessage(final String resourceName) throws IOException {
        return IOUtils.toString(Objects.requireNonNull(this.getClass().getResourceAsStream(resourceName)), StandardCharsets.UTF_8);
    }

    @Test
    void shouldParseValidMessage1() throws Exception {
        final String message = readMessage("message-4.2.2-1.txt");
        final SmartMeterMessage smartMeterMessage = dsmrParser.parse(message);

        assertThat(smartMeterMessage).isNotNull();
        assertThat(smartMeterMessage.getHeader()).isEqualTo("KFM5KAIFA-METER");
        assertThat(smartMeterMessage.getVersionInformationForP1Output()).isEqualTo("42");
        assertThat(smartMeterMessage.getTimestamp()).isEqualTo(LocalDateTime.of(2017, 2, 24, 19, 31,18));
        assertThat(smartMeterMessage.getTimestampDstIndicator()).isEqualTo(SmartMeterMessage.DstIndicator.WINTER);
        assertThat(smartMeterMessage.getEquipmentIdentifierElectricity()).isEqualTo("4530303235303030303738363130313136");
        assertThat(smartMeterMessage.getMeterReadingElectricityDeliveredToClientTariff1()).isEqualTo(new BigDecimal("1.628"));
        assertThat(smartMeterMessage.getMeterReadingElectricityDeliveredToClientTariff2()).isEqualTo(new BigDecimal("5.573"));
        assertThat(smartMeterMessage.getMeterReadingElectricityDeliveredByClientTariff1()).isEqualTo(new BigDecimal("1.301"));
        assertThat(smartMeterMessage.getMeterReadingElectricityDeliveredByClientTariff2()).isEqualTo(new BigDecimal("2.050"));
        assertThat(smartMeterMessage.getTariffIndicatorElectricity()).isEqualTo(2);
        assertThat(smartMeterMessage.getActualElectricityPowerDelivered()).isEqualTo(new BigDecimal("0.042"));
        assertThat(smartMeterMessage.getActualElectricityPowerReceived()).isEqualTo(new BigDecimal("0.000"));
        assertThat(smartMeterMessage.getNumberOfPowerFailuresInAnyPhase()).isEqualTo(1);
        assertThat(smartMeterMessage.getNumberOfLongPowerFailuresInAnyPhase()).isEqualTo(1);
        assertThat(smartMeterMessage.getNumberOfVoltageSagsInPhaseL1()).isEqualTo(10);
        assertThat(smartMeterMessage.getNumberOfVoltageSagsInPhaseL2()).isEqualTo(2);
        assertThat(smartMeterMessage.getNumberOfVoltageSagsInPhaseL3()).isNull();
        assertThat(smartMeterMessage.getTextMessageCodes()).isNull();
        assertThat(smartMeterMessage.getTextMessage()).isNull();
        assertThat(smartMeterMessage.getInstantaneousCurrentL1()).isZero();
        assertThat(smartMeterMessage.getEquipmentIdentifierGas()).isEqualTo("4730303235303033353032393639333137");
        assertThat(smartMeterMessage.getLastHourlyValueOfTemperatureConvertedGasDeliveredToClient()).isEqualTo(new BigDecimal("13.027"));
        assertThat(smartMeterMessage.getLastHourlyValueOfTemperatureConvertedGasDeliveredToClientCaptureTimestamp()).isEqualTo(LocalDateTime.of(2017, 2, 24, 19, 0,0));
        assertThat(smartMeterMessage.getLastHourlyValueOfTemperatureConvertedGasDeliveredToClientCaptureTimestampDstIndicator()).isEqualTo(SmartMeterMessage.DstIndicator.WINTER);
        assertThat(smartMeterMessage.getLongPowerFailureLog()).hasSize(1);
        assertThat(smartMeterMessage.getLongPowerFailureLog().getFirst().getTimestampOfEndOfFailure()).isEqualTo(LocalDateTime.of(2016, 8, 15, 13, 51, 47));
        assertThat(smartMeterMessage.getLongPowerFailureLog().getFirst().getTimestampOfEndOfFailureDstIndicator()).isEqualTo(SmartMeterMessage.DstIndicator.SUMMER);
        assertThat(smartMeterMessage.getLongPowerFailureLog().getFirst().getFailureDurationInSeconds()).isEqualTo(647L);
    }

    @Test
    void shouldParseValidMessage2() throws Exception {
        final String message = readMessage("message-4.2.2-2.txt");

        final SmartMeterMessage smartMeterMessage = dsmrParser.parse(message);

        assertThat(smartMeterMessage.getLongPowerFailureLog()).hasSize(2);
    }

    @Test
    void shouldParseValidMessage3() throws Exception {
        final String message = readMessage("message-4.2.2-3.txt");

        final SmartMeterMessage smartMeterMessage = dsmrParser.parse(message);

        assertThat(smartMeterMessage.getActualElectricityPowerDelivered()).isEqualTo(new BigDecimal("0.453"));
    }

    @Test
    void shouldParseValidMessage4() throws Exception {
        final String message = readMessage("message-4.2.2-4.txt");

        final SmartMeterMessage smartMeterMessage = dsmrParser.parse(message);

        assertThat(smartMeterMessage.getActualElectricityPowerDelivered()).isEqualTo(new BigDecimal("0.454"));
    }

    @Test
    void shouldParseVoltagePerPhaseFromDsmr5Message() throws Exception {
        final String message = normalizeTelegramForChecksum(readMessage("message-5.0-1.txt"));

        final SmartMeterMessage smartMeterMessage = dsmrParser.parse(message);

        assertThat(smartMeterMessage.getVersionInformationForP1Output()).isEqualTo("50");
        assertThat(smartMeterMessage.getVoltageL1()).isEqualTo(new BigDecimal("223.0"));
        assertThat(smartMeterMessage.getVoltageL2()).isEqualTo(new BigDecimal("225.0"));
        assertThat(smartMeterMessage.getVoltageL3()).isEqualTo(new BigDecimal("223.0"));
        assertThat(smartMeterMessage.getInstantaneousCurrentL1()).isZero();
        assertThat(smartMeterMessage.getInstantaneousCurrentL2()).isZero();
        assertThat(smartMeterMessage.getInstantaneousCurrentL3()).isZero();
        assertThat(smartMeterMessage.getInstantaneousPowerDeliveredL1()).isEqualTo(new BigDecimal("0.062"));
        assertThat(smartMeterMessage.getInstantaneousPowerDeliveredL2()).isEqualTo(new BigDecimal("0.000"));
        assertThat(smartMeterMessage.getInstantaneousPowerDeliveredL3()).isEqualTo(new BigDecimal("0.010"));
        assertThat(smartMeterMessage.getInstantaneousPowerReceivedL1()).isEqualTo(new BigDecimal("0.000"));
        assertThat(smartMeterMessage.getInstantaneousPowerReceivedL2()).isEqualTo(new BigDecimal("0.000"));
        assertThat(smartMeterMessage.getInstantaneousPowerReceivedL3()).isEqualTo(new BigDecimal("0.000"));
    }

    private String normalizeTelegramForChecksum(final String message) {
        return String.join("\r\n", message.split("\\R"));
    }

    @Test
    void shouldFailParseValidMessageFoundOnInternetWithWrongVersion() throws Exception {
        final String message = readMessage("message-4.0-from-internet.txt");
        assertThatExceptionOfType(DsmrParser.UnsupportedVersionException.class).isThrownBy(() ->
            dsmrParser.parse(message)
        ).withMessageStartingWith("Unsupported DSMR version");
    }

    @Test
    void shouldParseValidMessageFromSpecifiation() throws Exception {
        final String message = readMessage("message-4.2.2-from-P1-specification.txt");
        final SmartMeterMessage smartMeterMessage = dsmrParser.parse(message);

        assertThat(smartMeterMessage.getTextMessageCodes()).isEqualTo("01 61 81");
        assertThat(smartMeterMessage.getTextMessage()).isEqualTo("0123456789:;<=>?0123456789:;<=>?0123456789:;<=>?0123456789:;<=>?0123456789:;<=>?");
    }

    @Test
    void invalidCrc() throws Exception {
        final String message = readMessage("message-4.2.2-invalid-crc.txt");

        assertThatExceptionOfType(DsmrParser.InvalidSmartMeterMessageException.class).isThrownBy(() ->
                dsmrParser.parse(message)
        ).withMessageStartingWith("CRC checksum failed");
    }
}
