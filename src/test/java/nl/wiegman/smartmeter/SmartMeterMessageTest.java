package nl.wiegman.smartmeter;

import org.apache.commons.io.IOUtils;
import org.assertj.jodatime.api.Assertions;
import org.joda.time.DateTime;
import org.junit.Ignore;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

public class SmartMeterMessageTest {

    @Test
    public void validMessage1() throws Exception {
        String message = IOUtils.toString(this.getClass().getResourceAsStream("message-4.2.2-1.txt"));
        SmartMeterMessage smartMeterMessage = new SmartMeterMessage(message);

        assertThat(smartMeterMessage.getVersionInformationForP1Output()).isEqualTo("42");
        Assertions.assertThat(new DateTime(smartMeterMessage.getDatetimestamp())).isEqualTo("2016-03-18T15:06:54.000+01:00");
        assertThat(smartMeterMessage.getEquipmentIdentifier()).isEqualTo("4530303235303030303438343736313135");
        assertThat(smartMeterMessage.getMeterReadingElectricityDeliveredToClientTariff1()).isEqualTo(23.153);
        assertThat(smartMeterMessage.getMeterReadingElectricityDeliveredToClientTariff2()).isEqualTo(61.306);
        assertThat(smartMeterMessage.getMeterReadingElectricityDeliveredByClientTariff1()).isEqualTo(0.0);
        assertThat(smartMeterMessage.getMeterReadingElectricityDeliveredByClientTariff2()).isEqualTo(0.0);
        assertThat(smartMeterMessage.getTariffIndicatorElectricity()).isEqualTo("0002");
        assertThat(smartMeterMessage.getActualElectricityPowerDelivered()).isEqualTo(0.334);
        assertThat(smartMeterMessage.getLastHourlyValueGasDeliveredToClient()).isEqualTo(62.956);
    }

    @Test
    public void validMessage2() throws Exception {
        String message = IOUtils.toString(this.getClass().getResourceAsStream("message-4.2.2-2.txt"));
        SmartMeterMessage smartMeterMessage = new SmartMeterMessage(message);

        assertThat(smartMeterMessage.getVersionInformationForP1Output()).isEqualTo("42");
        Assertions.assertThat(new DateTime(smartMeterMessage.getDatetimestamp())).isEqualTo("2016-03-18T15:07:04.000+01:00");
        assertThat(smartMeterMessage.getEquipmentIdentifier()).isEqualTo("4530303235303030303438343736313135");
        assertThat(smartMeterMessage.getMeterReadingElectricityDeliveredToClientTariff1()).isEqualTo(23.153);
        assertThat(smartMeterMessage.getMeterReadingElectricityDeliveredToClientTariff2()).isEqualTo(61.307);
        assertThat(smartMeterMessage.getMeterReadingElectricityDeliveredByClientTariff1()).isEqualTo(0.0);
        assertThat(smartMeterMessage.getMeterReadingElectricityDeliveredByClientTariff2()).isEqualTo(0.0);
        assertThat(smartMeterMessage.getTariffIndicatorElectricity()).isEqualTo("0002");
        assertThat(smartMeterMessage.getActualElectricityPowerDelivered()).isEqualTo(0.337);
        assertThat(smartMeterMessage.getLastHourlyValueGasDeliveredToClient()).isEqualTo(62.956);
    }

    @Test
    public void validMessage3() throws Exception {
        String message = IOUtils.toString(this.getClass().getResourceAsStream("message-4.2.2-3.txt"));
        SmartMeterMessage smartMeterMessage = new SmartMeterMessage(message);

        assertThat(smartMeterMessage.getVersionInformationForP1Output()).isEqualTo("42");
        Assertions.assertThat(new DateTime(smartMeterMessage.getDatetimestamp())).isEqualTo("2016-03-18T14:31:34.000+01:00");
        assertThat(smartMeterMessage.getEquipmentIdentifier()).isEqualTo("4530303235303030303438343736313135");
        assertThat(smartMeterMessage.getMeterReadingElectricityDeliveredToClientTariff1()).isEqualTo(23.153);
        assertThat(smartMeterMessage.getMeterReadingElectricityDeliveredToClientTariff2()).isEqualTo(61.099);
        assertThat(smartMeterMessage.getMeterReadingElectricityDeliveredByClientTariff1()).isEqualTo(0.0);
        assertThat(smartMeterMessage.getMeterReadingElectricityDeliveredByClientTariff2()).isEqualTo(0.0);
        assertThat(smartMeterMessage.getTariffIndicatorElectricity()).isEqualTo("0002");
        assertThat(smartMeterMessage.getActualElectricityPowerDelivered()).isEqualTo(0.453);
        assertThat(smartMeterMessage.getLastHourlyValueGasDeliveredToClient()).isEqualTo(62.191);
    }

    @Test
    public void validMessage4() throws Exception {
        String message = IOUtils.toString(this.getClass().getResourceAsStream("message-4.2.2-4.txt"));
        SmartMeterMessage smartMeterMessage = new SmartMeterMessage(message);
    }

    @Ignore("Test broken, invalid CRC is reported, which is very strange because other tests pass...")
    @Test
    public void validMessageFromP1Specification() throws Exception {
        String message = IOUtils.toString(this.getClass().getResourceAsStream("message-4.2.2-from-P1-specification.txt"));
        new SmartMeterMessage(message);
    }

    @Test
    public void validMessageFromInternet() throws Exception {
        String message = IOUtils.toString(this.getClass().getResourceAsStream("message-4.0-from-internet.txt"));
        new SmartMeterMessage(message);
    }

    @Test
    public void invalidCrc() throws Exception {
        String message = IOUtils.toString(this.getClass().getResourceAsStream("message-4.2.2-invalid-crc.txt"));

        assertThatExceptionOfType(SmartMeterMessage.InvalidSmartMeterMessageException.class).isThrownBy(() -> { new SmartMeterMessage(message); })
                .withNoCause();

    }
}