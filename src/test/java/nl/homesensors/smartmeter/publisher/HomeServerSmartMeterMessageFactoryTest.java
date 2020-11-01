package nl.homesensors.smartmeter.publisher;

import nl.homesensors.smartmeter.LongPowerFailureLogItem;
import nl.homesensors.smartmeter.SmartMeterMessage;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static java.time.Month.MAY;

class HomeServerSmartMeterMessageFactoryTest {

    private final HomeServerSmartMeterMessageFactory homeServerSmartMeterMessageFactory = new HomeServerSmartMeterMessageFactory();

    @Test
    void givenSomeSmartMeterMessageWhenCreateThenCreated() throws Exception {
        final SmartMeterMessage smartMeterMessage = new SmartMeterMessage();
        smartMeterMessage.setTimestamp(LocalDateTime.of(2018, MAY, 3, 13, 14, 15));
        smartMeterMessage.setActualElectricityPowerDelivered(new BigDecimal("0.64"));

        final LongPowerFailureLogItem longPowerFailureLogItem = new LongPowerFailureLogItem();
        longPowerFailureLogItem.setFailureDurationInSeconds(12);
        longPowerFailureLogItem.setTimestampOfEndOfFailureDstIndicator(SmartMeterMessage.DstIndicator.SUMMER);
        longPowerFailureLogItem.setTimestampOfEndOfFailure(LocalDate.of(2016, MAY, 23).atTime(13, 12, 9));
        smartMeterMessage.addLongPowerFailureLogItem(longPowerFailureLogItem);

        final String actual = homeServerSmartMeterMessageFactory.create(smartMeterMessage);

        final String expected = """
                {
                    "datumtijd":"2018-05-03T13:14:15",
                    "stroomOpgenomenVermogenInWatt":640,
                    "stroomTarief1":null,
                    "stroomTarief2":null,
                    "gas":null,
                    "stroomTariefIndicator":null,
                    "meterIdentificatieStroom":null,
                    "meterIdentificatieGas":null,
                    "tekstBericht":null,
                    "tekstBerichtCodes":null,
                    "aantalStroomStoringenInAlleFases":null,
                    "aantalSpanningsDippenInFaseL1":null,
                    "aantalSpanningsDippenInFaseL2":null,
                    "aantalLangeStroomStoringenInAlleFases":null,
                    "langeStroomStoringen":[
                        {
                            "datumtijdEinde":"2016-05-23T13:12:09",
                            "duurVanStoringInSeconden":12
                        }
                    ]
                }
                """;
        JSONAssert.assertEquals(expected, actual, true);
    }
}
