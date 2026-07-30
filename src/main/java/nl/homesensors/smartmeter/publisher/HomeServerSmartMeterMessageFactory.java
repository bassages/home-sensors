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
                .stroomOpgenomenVermogenInWatt(kwToWatt(smartMeterMessage.getActualElectricityPowerDeliveredKw()))
                .stroomTariefIndicator(smartMeterMessage.getTariffIndicatorElectricity())
                .stroomTarief1(smartMeterMessage.getMeterReadingElectricityDeliveredToClientTariff1())
                .stroomTarief2(smartMeterMessage.getMeterReadingElectricityDeliveredToClientTariff2())
                .gas(smartMeterMessage.getLastHourlyValueOfTemperatureConvertedGasDeliveredToClient())
                .voltageL1(toRoundedInteger(smartMeterMessage.getVoltageL1()))
                .voltageL2(toRoundedInteger(smartMeterMessage.getVoltageL2()))
                .voltageL3(toRoundedInteger(smartMeterMessage.getVoltageL3()))
                .instantaneousCurrentL1Ampere(smartMeterMessage.getInstantaneousCurrentL1Ampere())
                .instantaneousCurrentL2Ampere(smartMeterMessage.getInstantaneousCurrentL2Ampere())
                .instantaneousCurrentL3Ampere(smartMeterMessage.getInstantaneousCurrentL3Ampere())
                .directGeleverdVermogenL1InWatt(kwToWatt(smartMeterMessage.getInstantaneousPowerDeliveredL1Kw()))
                .directGeleverdVermogenL2InWatt(kwToWatt(smartMeterMessage.getInstantaneousPowerDeliveredL2Kw()))
                .directGeleverdVermogenL3InWatt(kwToWatt(smartMeterMessage.getInstantaneousPowerDeliveredL3Kw()))
                .directTeruggeleverdVermogenL1InWatt(kwToWatt(smartMeterMessage.getInstantaneousPowerReceivedL1Kw()))
                .directTeruggeleverdVermogenL3InWatt(kwToWatt(smartMeterMessage.getInstantaneousPowerReceivedL3Kw()))
                .directTeruggeleverdVermogenL2InWatt(kwToWatt(smartMeterMessage.getInstantaneousPowerReceivedL2Kw()))
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

    private Integer kwToWatt(final BigDecimal kw) {
        if (kw == null) {
            return null;
        }
        return kw.multiply(BigDecimal.valueOf(1000)).intValue();
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

        private Integer instantaneousCurrentL1Ampere;
        private Integer instantaneousCurrentL2Ampere;
        private Integer instantaneousCurrentL3Ampere;

        private Integer directGeleverdVermogenL1InWatt;
        private Integer directGeleverdVermogenL2InWatt;
        private Integer directGeleverdVermogenL3InWatt;

        private Integer directTeruggeleverdVermogenL1InWatt;
        private Integer directTeruggeleverdVermogenL2InWatt;
        private Integer directTeruggeleverdVermogenL3InWatt;

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
