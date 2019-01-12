package nl.homesensors.smartmeter.publisher;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import nl.homesensors.smartmeter.SmartMeterMessage;

@Component
class AwsApiGatewayMessageFactory {

    String create(final SmartMeterMessage smartMeterMessage) throws JsonProcessingException {
        final SmartmeterMessage smartmeterMessage = mapToHomeServerMeterstand(smartMeterMessage);
        return getObjectMapper().writeValueAsString(smartmeterMessage);
    }

    private ObjectMapper getObjectMapper() {
        final ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        return mapper;
    }

    private SmartmeterMessage mapToHomeServerMeterstand(final SmartMeterMessage smartMeterMessage) {
        final SmartmeterMessage smartmeterMessage = new SmartmeterMessage();
        smartmeterMessage.setDatumtijd(smartMeterMessage.getTimestamp());
        smartmeterMessage.setStroomOpgenomenVermogenInWatt(smartMeterMessage.getActualElectricityPowerDelivered().multiply(BigDecimal.valueOf(1000)).intValue());
        smartmeterMessage.setStroomTariefIndicator(smartMeterMessage.getTariffIndicatorElectricity());
        return smartmeterMessage;
    }

    @SuppressWarnings("unused")
    private static class SmartmeterMessage {
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        private LocalDateTime datumtijd;
        private int stroomOpgenomenVermogenInWatt;
        private Integer stroomTariefIndicator;

        void setDatumtijd(final LocalDateTime datumtijd) {
            this.datumtijd = datumtijd;
        }

        void setStroomOpgenomenVermogenInWatt(final int stroomOpgenomenVermogenInWatt) {
            this.stroomOpgenomenVermogenInWatt = stroomOpgenomenVermogenInWatt;
        }

        void setStroomTariefIndicator(final Integer stroomTariefIndicator) {
            this.stroomTariefIndicator = stroomTariefIndicator;
        }
    }
}
