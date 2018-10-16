package nl.homesensors.smartmeter.publisher;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.Month;

import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;

import nl.homesensors.smartmeter.SmartMeterMessage;

public class HomeServerSmartMeterMessageFactoryTest {

    private HomeServerSmartMeterMessageFactory homeServerSmartMeterMessageFactory = new HomeServerSmartMeterMessageFactory();

    @Test
    public void whenCreateThenCreated() throws Exception {
        SmartMeterMessage smartMeterMessage = new SmartMeterMessage();
        smartMeterMessage.setTimestamp(LocalDateTime.of(2018, Month.MAY, 3, 13, 14, 15));
        smartMeterMessage.setActualElectricityPowerDelivered(new BigDecimal(0.64));

        String json = homeServerSmartMeterMessageFactory.create(smartMeterMessage);

        String expected = "{\"datumtijd\":\"2018-05-03T13:14:15\",\"stroomOpgenomenVermogenInWatt\":640,\"stroomTarief1\":null,\"stroomTarief2\":null,\"gas\":null,\"stroomTariefIndicator\":null,\"meterIdentificatieStroom\":null,\"meterIdentificatieGas\":null,\"tekstBericht\":null,\"tekstBerichtCodes\":null,\"aantalStroomStoringenInAlleFases\":null,\"aantalSpanningsDippenInFaseL1\":null,\"aantalSpanningsDippenInFaseL2\":null,\"aantalLangeStroomStoringenInAlleFases\":null,\"langeStroomStoringen\":null}\n";
        JSONAssert.assertEquals(expected, json, true);
    }
}