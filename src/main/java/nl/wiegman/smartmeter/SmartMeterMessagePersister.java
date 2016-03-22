package nl.wiegman.smartmeter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class SmartMeterMessagePersister {

    private static final Logger LOG = LoggerFactory.getLogger(SmartMeterMessagePersister.class);

    public void persist(SmartMeterMessage smartMeterMessage) {

        SmartMeterJsonMessage jsonMessage = createSmartMeterJsonMessage(smartMeterMessage);

        try {
            ObjectMapper mapper = new ObjectMapper();
            String jsonString = mapper.writeValueAsString(jsonMessage);
            LOG.info("Send json to cloud: " + jsonString);

        } catch (JsonProcessingException e) {
            LOG.error("Failed to map message to json. Message=" + smartMeterMessage, e);
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
