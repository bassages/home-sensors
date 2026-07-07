package nl.homesensors.smartmeter.publisher;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.Builder;
import lombok.Data;
import nl.homesensors.smartmeter.SmartMeterMessage;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Component;

@Component
class HomeServerSmartMeterMessageFactory {

    String create(final SmartMeterMessage smartMeterMessage) throws JsonProcessingException {
        final HomeServerMeterstand homeServerMeterstand = mapToHomeServerMeterstand(smartMeterMessage);
        return getObjectMapper().writeValueAsString(homeServerMeterstand);
    }

    private ObjectMapper getObjectMapper() {
        final ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        return mapper;
    }

    private HomeServerMeterstand mapToHomeServerMeterstand(final SmartMeterMessage smartMeterMessage) {
        return HomeServerMeterstand.builder()
                .datumtijd(smartMeterMessage.getTimestamp())
                .stroomOpgenomenVermogenInWatt(smartMeterMessage.getActualElectricityPowerDelivered().multiply(BigDecimal.valueOf(1000)).intValue())
                .stroomTariefIndicator(smartMeterMessage.getTariffIndicatorElectricity())
                .stroomTarief1(smartMeterMessage.getMeterReadingElectricityDeliveredToClientTariff1())
                .stroomTarief2(smartMeterMessage.getMeterReadingElectricityDeliveredToClientTariff2())
                .gas(smartMeterMessage.getLastHourlyValueOfTemperatureConvertedGasDeliveredToClient())
                .voltageL1(toRoundedInteger(smartMeterMessage.getVoltageL1()))
                .voltageL2(toRoundedInteger(smartMeterMessage.getVoltageL2()))
                .voltageL3(toRoundedInteger(smartMeterMessage.getVoltageL3()))
                .aantalSpanningsDippenInFaseL1(smartMeterMessage.getNumberOfVoltageSagsInPhaseL1())
                .aantalSpanningsDippenInFaseL2(smartMeterMessage.getNumberOfVoltageSagsInPhaseL2())
                .aantalSpanningsDippenInFaseL3(smartMeterMessage.getNumberOfVoltageSagsInPhaseL3())
                .aantalStroomStoringenInAlleFases(smartMeterMessage.getNumberOfPowerFailuresInAnyPhase())
                .aantalLangeStroomStoringenInAlleFases(smartMeterMessage.getNumberOfLongPowerFailuresInAnyPhase())
                .tekstBericht(smartMeterMessage.getTextMessage())
                .tekstBerichtCodes(smartMeterMessage.getTextMessageCodes())
                .meterIdentificatieStroom(smartMeterMessage.getEquipmentIdentifierElectricity())
                .meterIdentificatieGas(smartMeterMessage.getEquipmentIdentifierGas())
                .langeStroomStoringen(
                        smartMeterMessage.getLongPowerFailureLog().stream()
                                .map(item -> LangeStroomStoring.builder()
                                        .datumtijdEinde(item.getTimestampOfEndOfFailure())
                                        .duurVanStoringInSeconden(item.getFailureDurationInSeconds())
                                        .build())
                                .toList()
                )
                .build();
    }

    private Integer toRoundedInteger(final BigDecimal value) {
        if (value == null) {
            return null;
        }
        return value.setScale(0, RoundingMode.HALF_UP).intValue();
    }

    @Builder
    @Data
    @SuppressWarnings("unused")
    private static class HomeServerMeterstand {
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        private LocalDateTime datumtijd;

        private String meterIdentificatieStroom;
        private String meterIdentificatieGas;

        private int stroomOpgenomenVermogenInWatt;
        private Integer stroomTariefIndicator;

        private BigDecimal stroomTarief1;
        private BigDecimal stroomTarief2;
        private BigDecimal gas;

        private String tekstBericht;
        private String tekstBerichtCodes;

        private Integer aantalStroomStoringenInAlleFases;
        private Integer aantalSpanningsDippenInFaseL1;
        private Integer aantalSpanningsDippenInFaseL2;
        private Integer aantalSpanningsDippenInFaseL3;

        private Integer voltageL1;
        private Integer voltageL2;
        private Integer voltageL3;

        private Integer aantalLangeStroomStoringenInAlleFases;
        private List<LangeStroomStoring> langeStroomStoringen;
    }

    @Builder
    @SuppressWarnings("unused")
    static class LangeStroomStoring {
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        private LocalDateTime datumtijdEinde;
        private Long duurVanStoringInSeconden;
    }
}
