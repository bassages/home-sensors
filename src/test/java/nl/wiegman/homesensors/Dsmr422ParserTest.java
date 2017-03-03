package nl.wiegman.homesensors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;

import nl.wiegman.homesensors.smartmeter.SmartMeterMessage;
import nl.wiegman.homesensors.smartmeter.Dsmr422Parser;

public class Dsmr422ParserTest {

    private Dsmr422Parser dsmr422Parser;

    @Before
    public void setup() {
        this.dsmr422Parser = new Dsmr422Parser();
    }

    @Test
    public void shouldParseValidMessage1() throws Exception {
        String message = IOUtils.toString(this.getClass().getResourceAsStream("message-4.2.2-1.txt"), StandardCharsets.UTF_8);
        SmartMeterMessage smartMeterMessage = dsmr422Parser.parse(message);

        assertThat(smartMeterMessage).isNotNull();
        assertThat(smartMeterMessage.getHeader()).isEqualTo("KFM5KAIFA-METER");
        assertThat(smartMeterMessage.getVersionInformationForP1Output()).isEqualTo("42");
        assertThat(smartMeterMessage.getTimestamp()).hasYear(2017).hasMonth(2).hasDayOfMonth(24).hasHourOfDay(19).hasMinute(31).hasSecond(18);
        assertThat(smartMeterMessage.getTimestampDstIndicator()).isEqualTo(SmartMeterMessage.DstIndicator.WINTER);
        assertThat(smartMeterMessage.getEquipmentIdentifierElectricity()).isEqualTo("4530303235303030303738363130313136");
        assertThat(smartMeterMessage.getMeterReadingElectricityDeliveredToClientTariff1()).isEqualTo(new BigDecimal("1.628"));
        assertThat(smartMeterMessage.getMeterReadingElectricityDeliveredToClientTariff2()).isEqualTo(new BigDecimal("5.573"));
        assertThat(smartMeterMessage.getMeterReadingElectricityDeliveredByClientTariff1()).isEqualTo(new BigDecimal("1.301"));
        assertThat(smartMeterMessage.getMeterReadingElectricityDeliveredByClientTariff2()).isEqualTo(new BigDecimal("2.050"));
        assertThat(smartMeterMessage.getTariffIndicatorElectricity()).isEqualTo("0002");
        assertThat(smartMeterMessage.getActualElectricityPowerDelivered()).isEqualTo(new BigDecimal("0.042"));
        assertThat(smartMeterMessage.getActualElectricityPowerRecieved()).isEqualTo(new BigDecimal("0.000"));
        assertThat(smartMeterMessage.getNumberOfPowerFailuresInAnyPhase()).isEqualTo(1);
        assertThat(smartMeterMessage.getNumberOfLongPowerFailuresInAnyPhase()).isEqualTo(1);
        assertThat(smartMeterMessage.getNumberOfVoltageSagsInPhaseL1()).isEqualTo(10);
        assertThat(smartMeterMessage.getNumberOfVoltageSagsInPhaseL2()).isEqualTo(2);
        assertThat(smartMeterMessage.getTextMessageCodes()).isEqualTo(null);
        assertThat(smartMeterMessage.getTextMessage()).isEqualTo(null);
        assertThat(smartMeterMessage.getInstantaneousCurrentL1()).isEqualTo(0);
        assertThat(smartMeterMessage.getInstantaneousActivePowerL1()).isEqualTo(new BigDecimal("0.042"));
        assertThat(smartMeterMessage.getInstantaneousActivePowerL2()).isEqualTo(new BigDecimal("0.000"));
        assertThat(smartMeterMessage.getEquipmentIdentifierGas()).isEqualTo("4730303235303033353032393639333137");
        assertThat(smartMeterMessage.getLastHourlyValueOfTemperatureConvertedGasDeliveredToClient()).isEqualTo(new BigDecimal("13.027"));
        assertThat(smartMeterMessage.getLastHourlyValueOfTemperatureConvertedGasDeliveredToClientCaptureTimestamp()).hasYear(2017).hasMonth(2).hasDayOfMonth(24).hasHourOfDay(19).hasMinute(00).hasSecond(00);
        assertThat(smartMeterMessage.getLastHourlyValueOfTemperatureConvertedGasDeliveredToClientCaptureTimestampDstIndicator()).isEqualTo(SmartMeterMessage.DstIndicator.WINTER);
        assertThat(smartMeterMessage.getLongPowerFailureLog()).hasSize(1);
        assertThat(smartMeterMessage.getLongPowerFailureLog().get(0).getTimestampOfEndOfFailure()).hasYear(2016).hasMonth(8).hasDayOfMonth(15).hasHourOfDay(13).hasMinute(51).hasSecond(47);
        assertThat(smartMeterMessage.getLongPowerFailureLog().get(0).getTimestampOfEndOfFailureDstIndicator()).isEqualTo(SmartMeterMessage.DstIndicator.SUMMER);
        assertThat(smartMeterMessage.getLongPowerFailureLog().get(0).getFailureDurationInSeconds()).isEqualTo(647L);
    }

    @Test
    public void shouldParseValidMessage2() throws Exception {
        String message = IOUtils.toString(this.getClass().getResourceAsStream("message-4.2.2-2.txt"), StandardCharsets.UTF_8);
        SmartMeterMessage smartMeterMessage = dsmr422Parser.parse(message);
        assertThat(smartMeterMessage.getLongPowerFailureLog()).hasSize(2);
    }

    @Test
    public void shouldParseValidMessage3() throws Exception {
        String message = IOUtils.toString(this.getClass().getResourceAsStream("message-4.2.2-3.txt"), StandardCharsets.UTF_8);
        dsmr422Parser.parse(message);
    }

    @Test
    public void shouldParseValidMessage4() throws Exception {
        String message = IOUtils.toString(this.getClass().getResourceAsStream("message-4.2.2-4.txt"), StandardCharsets.UTF_8);
        dsmr422Parser.parse(message);
    }

    @Test
    public void shouldParseValidMessageFoundOnInternet() throws Exception {
        String message = IOUtils.toString(this.getClass().getResourceAsStream("message-4.0-from-internet.txt"), StandardCharsets.UTF_8);
        dsmr422Parser.parse(message);
    }

    @Test
    public void shouldParseValidMessageFromSpecifiation() throws Exception {
        String message = IOUtils.toString(this.getClass().getResourceAsStream("message-4.2.2-from-P1-specification.txt"), StandardCharsets.UTF_8);
        SmartMeterMessage smartMeterMessage = dsmr422Parser.parse(message);

        assertThat(smartMeterMessage.getTextMessageCodes()).isEqualTo("01 61 81");
        assertThat(smartMeterMessage.getTextMessage()).isEqualTo("0123456789:;<=>?0123456789:;<=>?0123456789:;<=>?0123456789:;<=>?0123456789:;<=>?");
    }

    @Test
    public void invalidCrc() throws Exception {
        String message = IOUtils.toString(this.getClass().getResourceAsStream("message-4.2.2-invalid-crc.txt"), StandardCharsets.UTF_8);

        assertThatExceptionOfType(Dsmr422Parser.InvalidSmartMeterMessageException.class).isThrownBy(() ->
                dsmr422Parser.parse(message)
        ).withMessageStartingWith("CRC checksum failed");
    }
}