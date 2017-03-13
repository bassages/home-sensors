package nl.wiegman.homesensors.smartmeter;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class HomeServerSmartMeterPublisher {

    private static final Logger LOG = LoggerFactory.getLogger(HomeServerSmartMeterPublisher.class);

    @Value("${home-server-rest-service-url:#{null}}")
    private String homeServerRestServiceUrl;

    @Value("${home-server-rest-service-basic-auth-user:#{null}}")
    private String homeServerRestServiceBasicAuthUser;
    @Value("${home-server-rest-service-basic-auth-password:#{null}}")
    private String homeServerRestServiceBasicAuthPassword;

    public void publish(SmartMeterMessage smartMeterMessage) {
        try {
            String url = homeServerRestServiceUrl + "/slimmemeter";
            String jsonMessage = createSmartMeterJsonMessage(smartMeterMessage);

            try {
                postJson(url, jsonMessage);
            } catch (Exception e) {
                LOG.warn("Post to url [" + url + "] failed.", e);
            }

        } catch (JsonProcessingException e) {
            LOG.error("Failed to map message to json. Message=" + smartMeterMessage, e);
        }
    }

    private void postJson(String url, String jsonString) throws Exception {
        LOG.debug("Post to url: {}. Request body: {}", url, jsonString);

        try (CloseableHttpClient httpClient = HttpClientBuilder.create().build()) {

            HttpPost request = new HttpPost(url);
            StringEntity params = new StringEntity(jsonString, ContentType.APPLICATION_JSON);
            request.setEntity(params);

            setAuthorizationHeader(request);

            CloseableHttpResponse response = httpClient.execute(request);

            if (response.getStatusLine().getStatusCode() != HttpStatus.SC_CREATED) {
                throw new RuntimeException("Unexpected statusline: " + response.getStatusLine());
            }
        }
    }

    private void setAuthorizationHeader(HttpPost request) {
        if (homeServerRestServiceBasicAuthUser != null
                && homeServerRestServiceBasicAuthPassword != null) {
            String auth = homeServerRestServiceBasicAuthUser + ":" + homeServerRestServiceBasicAuthPassword;
            byte[] encodedAuth = Base64.encodeBase64(auth.getBytes(StandardCharsets.UTF_8));
            String authorizationHeader = "Basic " + new String(encodedAuth);
            request.setHeader(HttpHeaders.AUTHORIZATION, authorizationHeader);
        }
    }

    private String createSmartMeterJsonMessage(SmartMeterMessage smartMeterMessage) throws JsonProcessingException {
        HomeServerMeterstand homeServerMeterstand = mapToHomeServerMeterstand(smartMeterMessage);
        return new ObjectMapper().writeValueAsString(homeServerMeterstand);
    }

    private HomeServerMeterstand mapToHomeServerMeterstand(SmartMeterMessage smartMeterMessage) {
        HomeServerMeterstand homeServerMeterstand = new HomeServerMeterstand();
        homeServerMeterstand.setDatumtijd(smartMeterMessage.getTimestamp().getTime());
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
            for (LongPowerFailureLogItem item : smartMeterMessage.getLongPowerFailureLog()) {
                LangeStroomStoring langeStroomStoring = new LangeStroomStoring();
                langeStroomStoring.setDatumtijdEinde(item.getTimestampOfEndOfFailure());
                langeStroomStoring.setDuurVanStoringInSeconden(item.getFailureDurationInSeconds());
                homeServerMeterstand.getLangeStroomStoringen().add(langeStroomStoring);
            }
        }
        return homeServerMeterstand;
    }

    private static class HomeServerMeterstand {
        private long datumtijd;
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

        public long getDatumtijd() {
            return datumtijd;
        }

        public void setDatumtijd(long datumtijd) {
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
        private Date datumtijdEinde;
        private Long duurVanStoringInSeconden;

        public Date getDatumtijdEinde() {
            return datumtijdEinde;
        }

        public void setDatumtijdEinde(Date datumtijdEinde) {
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
