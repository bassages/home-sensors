package nl.wiegman.smartmeter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FileUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;

@Component
public class SmartMeterMessagePersister {

    private static final Logger LOG = LoggerFactory.getLogger(SmartMeterMessagePersister.class);

    // TODO: inject property...
    private static final String SERVER_ENDPOINT = "http://homecontrol-bassages.rhcloud.com/homecontrol/rest/meterstanden";

    public void persist(SmartMeterMessage smartMeterMessage) {

        SmartMeterJsonMessage jsonMessage = createSmartMeterJsonMessage(smartMeterMessage);

        try {
            ObjectMapper mapper = new ObjectMapper();
            String jsonString = mapper.writeValueAsString(jsonMessage);

            try {

                sendToServer(jsonString);

            } catch (RuntimeException e) {
                LOG.warn("Failed send message to " + SERVER_ENDPOINT + ". Message=" + smartMeterMessage, e);

                try {

                    FileUtils.writeStringToFile(new File(System.currentTimeMillis() + ".txt"), jsonString);

                } catch (IOException e1) {
                    LOG.error("Failed to save file. Message=" + smartMeterMessage, e1);
                }
            }

        } catch (JsonProcessingException e) {
            LOG.error("Failed to map message to json. Message=" + smartMeterMessage, e);
        }
    }

    private void sendToServer(String jsonString) {
        LOG.info("Send json to cloud: " + jsonString);

        try (CloseableHttpClient httpClient = HttpClientBuilder.create().build()) {

            HttpPost request = new HttpPost(SERVER_ENDPOINT);
            StringEntity params = new StringEntity(jsonString, ContentType.APPLICATION_JSON);
            request.setEntity(params);

            CloseableHttpResponse response = httpClient.execute(request);

            int statusCode = response.getStatusLine().getStatusCode();

            if (statusCode != 204) {
                throw new RuntimeException("Upload to cloud failed. Statuscode = " + statusCode);
            }

        } catch (Exception ex) {
            throw new RuntimeException("Upload to cloud failed", ex);
        }
    }

    private SmartMeterJsonMessage createSmartMeterJsonMessage(SmartMeterMessage smartMeterMessage) {
        SmartMeterJsonMessage jsonMessage = new SmartMeterJsonMessage();
        jsonMessage.setDatumtijd(smartMeterMessage.getDatetimestamp().getTime());
        jsonMessage.setStroomOpgenomenVermogenInWatt(smartMeterMessage.getActualElectricityPowerDelivered().multiply(new BigDecimal(1000.0d)).intValue());
        jsonMessage.setStroomTarief1(smartMeterMessage.getMeterReadingElectricityDeliveredToClientTariff1());
        jsonMessage.setStroomTarief2(smartMeterMessage.getMeterReadingElectricityDeliveredToClientTariff2());
        jsonMessage.setGas(smartMeterMessage.getLastHourlyValueGasDeliveredToClient());
        return jsonMessage;
    }

    private static class SmartMeterJsonMessage {
        private long datumtijd;
        private int stroomOpgenomenVermogenInWatt;
        private BigDecimal stroomTarief1;
        private BigDecimal stroomTarief2;
        private BigDecimal gas;

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
    }
}
