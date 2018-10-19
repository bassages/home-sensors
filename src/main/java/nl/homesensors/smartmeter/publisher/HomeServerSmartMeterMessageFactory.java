package nl.homesensors.smartmeter.publisher;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import nl.homesensors.smartmeter.LongPowerFailureLogItem;
import nl.homesensors.smartmeter.SmartMeterMessage;

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
        final HomeServerMeterstand homeServerMeterstand = new HomeServerMeterstand();
        homeServerMeterstand.setDatumtijd(smartMeterMessage.getTimestamp());
        homeServerMeterstand.setStroomOpgenomenVermogenInWatt(smartMeterMessage.getActualElectricityPowerDelivered().multiply(BigDecimal.valueOf(1000)).intValue());
        homeServerMeterstand.setStroomTariefIndicator(smartMeterMessage.getTariffIndicatorElectricity());
        homeServerMeterstand.setStroomTarief1(smartMeterMessage.getMeterReadingElectricityDeliveredToClientTariff1());
        homeServerMeterstand.setStroomTarief2(smartMeterMessage.getMeterReadingElectricityDeliveredToClientTariff2());
        homeServerMeterstand.setGas(smartMeterMessage.getLastHourlyValueOfTemperatureConvertedGasDeliveredToClient());

        homeServerMeterstand.setAantalSpanningsDippenInFaseL1(smartMeterMessage.getNumberOfVoltageSagsInPhaseL1());
        homeServerMeterstand.setAantalSpanningsDippenInFaseL2(smartMeterMessage.getNumberOfVoltageSagsInPhaseL2());
        homeServerMeterstand.setAantalStroomStoringenInAlleFases(smartMeterMessage.getNumberOfPowerFailuresInAnyPhase());
        homeServerMeterstand.setAantalLangeStroomStoringenInAlleFases(smartMeterMessage.getNumberOfLongPowerFailuresInAnyPhase());

        homeServerMeterstand.setTekstBericht(smartMeterMessage.getTextMessage());
        homeServerMeterstand.setTekstBerichtCodes(smartMeterMessage.getTextMessageCodes());

        homeServerMeterstand.setMeterIdentificatieStroom(smartMeterMessage.getEquipmentIdentifierElectricity());
        homeServerMeterstand.setMeterIdentificatieGas(smartMeterMessage.getEquipmentIdentifierGas());

        if (!CollectionUtils.isEmpty(smartMeterMessage.getLongPowerFailureLog())) {
            homeServerMeterstand.setLangeStroomStoringen(new ArrayList<>());
            for (final LongPowerFailureLogItem item : smartMeterMessage.getLongPowerFailureLog()) {
                final LangeStroomStoring langeStroomStoring = new LangeStroomStoring();
                langeStroomStoring.setDatumtijdEinde(item.getTimestampOfEndOfFailure());
                langeStroomStoring.setDuurVanStoringInSeconden(item.getFailureDurationInSeconds());
                homeServerMeterstand.addLangeStroomStoring(langeStroomStoring);
            }
        }
        return homeServerMeterstand;
    }

    @SuppressWarnings("unused")
    private static class HomeServerMeterstand {
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        private LocalDateTime datumtijd;
        private int stroomOpgenomenVermogenInWatt;
        private BigDecimal stroomTarief1;
        private BigDecimal stroomTarief2;
        private BigDecimal gas;
        private Integer stroomTariefIndicator;

        private String meterIdentificatieStroom;
        private String meterIdentificatieGas;

        private String tekstBericht;
        private String tekstBerichtCodes;

        private Integer aantalStroomStoringenInAlleFases;
        private Integer aantalSpanningsDippenInFaseL1;
        private Integer aantalSpanningsDippenInFaseL2;

        private Integer aantalLangeStroomStoringenInAlleFases;
        private List<LangeStroomStoring> langeStroomStoringen;

        void setDatumtijd(final LocalDateTime datumtijd) {
            this.datumtijd = datumtijd;
        }

        void setStroomOpgenomenVermogenInWatt(final int stroomOpgenomenVermogenInWatt) {
            this.stroomOpgenomenVermogenInWatt = stroomOpgenomenVermogenInWatt;
        }

        void setStroomTarief1(final BigDecimal stroomTarief1) {
            this.stroomTarief1 = stroomTarief1;
        }

        void setStroomTarief2(final BigDecimal stroomTarief2) {
            this.stroomTarief2 = stroomTarief2;
        }

        void setGas(final BigDecimal gas) {
            this.gas = gas;
        }

        void setAantalStroomStoringenInAlleFases(final Integer aantalStroomStoringenInAlleFases) {
            this.aantalStroomStoringenInAlleFases = aantalStroomStoringenInAlleFases;
        }

        void setAantalSpanningsDippenInFaseL1(final Integer aantalSpanningsDippenInFaseL1) {
            this.aantalSpanningsDippenInFaseL1 = aantalSpanningsDippenInFaseL1;
        }

        void setAantalSpanningsDippenInFaseL2(final Integer aantalSpanningsDippenInFaseL2) {
            this.aantalSpanningsDippenInFaseL2 = aantalSpanningsDippenInFaseL2;
        }

        void setTekstBericht(final String tekstBericht) {
            this.tekstBericht = tekstBericht;
        }

        void setAantalLangeStroomStoringenInAlleFases(final Integer aantalLangeStroomStoringenInAlleFases) {
            this.aantalLangeStroomStoringenInAlleFases = aantalLangeStroomStoringenInAlleFases;
        }

        void setLangeStroomStoringen(final List<LangeStroomStoring> langeStroomStoringen) {
            this.langeStroomStoringen = langeStroomStoringen;
        }

        void setTekstBerichtCodes(final String tekstBerichtCodes) {
            this.tekstBerichtCodes = tekstBerichtCodes;
        }

        void setMeterIdentificatieStroom(final String meterIdentificatieStroom) {
            this.meterIdentificatieStroom = meterIdentificatieStroom;
        }

        void setMeterIdentificatieGas(final String meterIdentificatieGas) {
            this.meterIdentificatieGas = meterIdentificatieGas;
        }

        void setStroomTariefIndicator(final Integer stroomTariefIndicator) {
            this.stroomTariefIndicator = stroomTariefIndicator;
        }

        void addLangeStroomStoring(final LangeStroomStoring langeStroomStoring) {
            this.langeStroomStoringen.add(langeStroomStoring);
        }
    }

    @SuppressWarnings("unused")
    static class LangeStroomStoring {
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        private LocalDateTime datumtijdEinde;
        private Long duurVanStoringInSeconden;

        void setDatumtijdEinde(final LocalDateTime datumtijdEinde) {
            this.datumtijdEinde = datumtijdEinde;
        }

        void setDuurVanStoringInSeconden(final Long duurVanStoringInSeconden) {
            this.duurVanStoringInSeconden = duurVanStoringInSeconden;
        }
    }
}
