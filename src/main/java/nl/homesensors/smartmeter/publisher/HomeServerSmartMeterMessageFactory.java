package nl.homesensors.smartmeter.publisher;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import nl.homesensors.smartmeter.LongPowerFailureLogItem;
import nl.homesensors.smartmeter.SmartMeterMessage;

@Component
public class HomeServerSmartMeterMessageFactory {

    String create(final SmartMeterMessage smartMeterMessage) throws JsonProcessingException {
        final HomeServerMeterstand homeServerMeterstand = mapToHomeServerMeterstand(smartMeterMessage);
        return getObjectMapper().writeValueAsString(homeServerMeterstand);
    }

    private ObjectMapper getObjectMapper() {
        final ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        return mapper;
    }

    private HomeServerMeterstand mapToHomeServerMeterstand(final SmartMeterMessage smartMeterMessage) {
        final HomeServerMeterstand homeServerMeterstand = new HomeServerMeterstand();
        homeServerMeterstand.setDatumtijd(smartMeterMessage.getTimestamp());
        homeServerMeterstand.setStroomOpgenomenVermogenInWatt(smartMeterMessage.getActualElectricityPowerDelivered().multiply(new BigDecimal(1000.0d)).intValue());
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
                homeServerMeterstand.getLangeStroomStoringen().add(langeStroomStoring);
            }
        }
        return homeServerMeterstand;
    }

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

        public LocalDateTime getDatumtijd() {
            return datumtijd;
        }

        public void setDatumtijd(LocalDateTime datumtijd) {
            this.datumtijd = datumtijd;
        }

        public int getStroomOpgenomenVermogenInWatt() {
            return stroomOpgenomenVermogenInWatt;
        }

        public void setStroomOpgenomenVermogenInWatt(int stroomOpgenomenVermogenInWatt) {
            this.stroomOpgenomenVermogenInWatt = stroomOpgenomenVermogenInWatt;
        }

        public BigDecimal getStroomTarief1() {
            return stroomTarief1;
        }

        public void setStroomTarief1(BigDecimal stroomTarief1) {
            this.stroomTarief1 = stroomTarief1;
        }

        public BigDecimal getStroomTarief2() {
            return stroomTarief2;
        }

        public void setStroomTarief2(BigDecimal stroomTarief2) {
            this.stroomTarief2 = stroomTarief2;
        }

        public BigDecimal getGas() {
            return gas;
        }

        public void setGas(BigDecimal gas) {
            this.gas = gas;
        }

        public Integer getAantalStroomStoringenInAlleFases() {
            return aantalStroomStoringenInAlleFases;
        }

        public void setAantalStroomStoringenInAlleFases(Integer aantalStroomStoringenInAlleFases) {
            this.aantalStroomStoringenInAlleFases = aantalStroomStoringenInAlleFases;
        }

        public Integer getAantalSpanningsDippenInFaseL1() {
            return aantalSpanningsDippenInFaseL1;
        }

        public void setAantalSpanningsDippenInFaseL1(Integer aantalSpanningsDippenInFaseL1) {
            this.aantalSpanningsDippenInFaseL1 = aantalSpanningsDippenInFaseL1;
        }

        public Integer getAantalSpanningsDippenInFaseL2() {
            return aantalSpanningsDippenInFaseL2;
        }

        public void setAantalSpanningsDippenInFaseL2(Integer aantalSpanningsDippenInFaseL2) {
            this.aantalSpanningsDippenInFaseL2 = aantalSpanningsDippenInFaseL2;
        }

        public String getTekstBericht() {
            return tekstBericht;
        }

        public void setTekstBericht(String tekstBericht) {
            this.tekstBericht = tekstBericht;
        }

        public Integer getAantalLangeStroomStoringenInAlleFases() {
            return aantalLangeStroomStoringenInAlleFases;
        }

        public void setAantalLangeStroomStoringenInAlleFases(Integer aantalLangeStroomStoringenInAlleFases) {
            this.aantalLangeStroomStoringenInAlleFases = aantalLangeStroomStoringenInAlleFases;
        }

        public List<LangeStroomStoring> getLangeStroomStoringen() {
            return langeStroomStoringen;
        }

        public void setLangeStroomStoringen(List<LangeStroomStoring> langeStroomStoringen) {
            this.langeStroomStoringen = langeStroomStoringen;
        }

        public void setTekstBerichtCodes(String tekstBerichtCodes) {
            this.tekstBerichtCodes = tekstBerichtCodes;
        }

        public String getTekstBerichtCodes() {
            return tekstBerichtCodes;
        }

        public String getMeterIdentificatieStroom() {
            return meterIdentificatieStroom;
        }

        public void setMeterIdentificatieStroom(String meterIdentificatieStroom) {
            this.meterIdentificatieStroom = meterIdentificatieStroom;
        }

        public String getMeterIdentificatieGas() {
            return meterIdentificatieGas;
        }

        public void setMeterIdentificatieGas(String meterIdentificatieGas) {
            this.meterIdentificatieGas = meterIdentificatieGas;
        }

        public Integer getStroomTariefIndicator() {
            return stroomTariefIndicator;
        }

        public void setStroomTariefIndicator(Integer stroomTariefIndicator) {
            this.stroomTariefIndicator = stroomTariefIndicator;
        }
    }

    public static class LangeStroomStoring {
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        private LocalDateTime datumtijdEinde;
        private Long duurVanStoringInSeconden;

        public LocalDateTime getDatumtijdEinde() {
            return datumtijdEinde;
        }

        public void setDatumtijdEinde(LocalDateTime datumtijdEinde) {
            this.datumtijdEinde = datumtijdEinde;
        }

        public Long getDuurVanStoringInSeconden() {
            return duurVanStoringInSeconden;
        }

        public void setDuurVanStoringInSeconden(Long duurVanStoringInSeconden) {
            this.duurVanStoringInSeconden = duurVanStoringInSeconden;
        }
    }
}
