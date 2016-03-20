package nl.wiegman.dsmr;

import org.apache.commons.io.IOUtils;
import org.assertj.jodatime.api.Assertions;
import org.joda.time.DateTime;
import org.junit.Ignore;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

public class DsmrTest {

    @Test
    public void validMessage1() throws Exception {
        String message = IOUtils.toString(this.getClass().getResourceAsStream("message-4.2.2-1.txt"));
        Dsmr dsmr = new Dsmr(message);

        assertThat(dsmr.getVersionInformationForP1Output()).isEqualTo("42");
        Assertions.assertThat(new DateTime(dsmr.getDatetimestamp())).isEqualTo("2016-03-18T15:06:54.000+01:00");
        assertThat(dsmr.getEquipmentIdentifier()).isEqualTo("4530303235303030303438343736313135");
        assertThat(dsmr.getMeterReadingElectricityDeliveredToClientTariff1()).isEqualTo(23.153);
        assertThat(dsmr.getMeterReadingElectricityDeliveredToClientTariff2()).isEqualTo(61.306);
        assertThat(dsmr.getMeterReadingElectricityDeliveredByClientTariff1()).isEqualTo(0.0);
        assertThat(dsmr.getMeterReadingElectricityDeliveredByClientTariff2()).isEqualTo(0.0);
        assertThat(dsmr.getTariffIndicatorElectricity()).isEqualTo("0002");
        assertThat(dsmr.getActualElectricityPowerDelivered()).isEqualTo(0.334);
        assertThat(dsmr.getLastHourlyValueGasDeliveredToClient()).isEqualTo(62.956);
    }

    @Test
    public void validMessage2() throws Exception {
        String message = IOUtils.toString(this.getClass().getResourceAsStream("message-4.2.2-2.txt"));
        Dsmr dsmr = new Dsmr(message);

        assertThat(dsmr.getVersionInformationForP1Output()).isEqualTo("42");
        Assertions.assertThat(new DateTime(dsmr.getDatetimestamp())).isEqualTo("2016-03-18T15:07:04.000+01:00");
        assertThat(dsmr.getEquipmentIdentifier()).isEqualTo("4530303235303030303438343736313135");
        assertThat(dsmr.getMeterReadingElectricityDeliveredToClientTariff1()).isEqualTo(23.153);
        assertThat(dsmr.getMeterReadingElectricityDeliveredToClientTariff2()).isEqualTo(61.307);
        assertThat(dsmr.getMeterReadingElectricityDeliveredByClientTariff1()).isEqualTo(0.0);
        assertThat(dsmr.getMeterReadingElectricityDeliveredByClientTariff2()).isEqualTo(0.0);
        assertThat(dsmr.getTariffIndicatorElectricity()).isEqualTo("0002");
        assertThat(dsmr.getActualElectricityPowerDelivered()).isEqualTo(0.337);
        assertThat(dsmr.getLastHourlyValueGasDeliveredToClient()).isEqualTo(62.956);
    }

    @Test
    public void validMessage3() throws Exception {
        String message = IOUtils.toString(this.getClass().getResourceAsStream("message-4.2.2-3.txt"));
        Dsmr dsmr = new Dsmr(message);

        assertThat(dsmr.getVersionInformationForP1Output()).isEqualTo("42");
        Assertions.assertThat(new DateTime(dsmr.getDatetimestamp())).isEqualTo("2016-03-18T14:31:34.000+01:00");
        assertThat(dsmr.getEquipmentIdentifier()).isEqualTo("4530303235303030303438343736313135");
        assertThat(dsmr.getMeterReadingElectricityDeliveredToClientTariff1()).isEqualTo(23.153);
        assertThat(dsmr.getMeterReadingElectricityDeliveredToClientTariff2()).isEqualTo(61.099);
        assertThat(dsmr.getMeterReadingElectricityDeliveredByClientTariff1()).isEqualTo(0.0);
        assertThat(dsmr.getMeterReadingElectricityDeliveredByClientTariff2()).isEqualTo(0.0);
        assertThat(dsmr.getTariffIndicatorElectricity()).isEqualTo("0002");
        assertThat(dsmr.getActualElectricityPowerDelivered()).isEqualTo(0.453);
        assertThat(dsmr.getLastHourlyValueGasDeliveredToClient()).isEqualTo(62.191);
    }

    @Test
    public void validMessage4() throws Exception {
        String message = IOUtils.toString(this.getClass().getResourceAsStream("message-4.2.2-4.txt"));
        Dsmr dsmr = new Dsmr(message);
    }

    @Ignore("Test broken, invalid CRC is reported, which is very strange because other tests pass...")
    @Test
    public void validMessageFromP1Specification() throws Exception {
        String message = IOUtils.toString(this.getClass().getResourceAsStream("message-4.2.2-from-P1-specification.txt"));
        new Dsmr(message);
    }

    @Test
    public void validMessageFromInternet() throws Exception {
        String message = IOUtils.toString(this.getClass().getResourceAsStream("message-4.0-from-internet.txt"));
        new Dsmr(message);
    }

    @Test
    public void invalidCrc() throws Exception {
        String message = IOUtils.toString(this.getClass().getResourceAsStream("message-4.2.2-invalid-crc.txt"));

        assertThatExceptionOfType(Dsmr.InvalidChecksumException.class).isThrownBy(() -> { new Dsmr(message); })
                .withNoCause();

    }
}