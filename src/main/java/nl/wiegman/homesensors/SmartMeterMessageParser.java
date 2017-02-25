package nl.wiegman.homesensors;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class SmartMeterMessageParser {

    private static final Logger LOG = LoggerFactory.getLogger(SmartMeterMessageParser.class);

    private static final String DSMR_TIMESTAMP_FORMAT = "yyMMddHHmmss";

    private static final String GROUP_NAME = "attributeValue";

    public enum AttributeValue {
        HEADER("^/(?<" + GROUP_NAME + ">.+?)\\R{2}.+"),
        VERSION_INFORMATION_FOR_P1_OUTPUT(".+?1-3:0.2.8\\((?<" + GROUP_NAME + ">\\w{2})\\).+"),
        DATETIMESTAMP_OF_THE_P1_MESSAGE(".+?0-0:1.0.0\\((?<" + GROUP_NAME + ">\\d{" + DSMR_TIMESTAMP_FORMAT.length() + "}).+"),
        DATETIMESTAMP_OF_THE_P1_MESSAGE_DST_INDICATOR(".+?0-0:1.0.0.+?(?<" + GROUP_NAME + ">(S|W))\\).+"),
        EQUIPMENT_IDENTIFIER_ELECTRICITY(".+?0-0:96.1.1\\((?<" + GROUP_NAME + ">\\w+)\\).+"),
        METER_READING_ELECTRICITY_DELIVERED_TO_CLIENT_TARIFF_1(".+?1-0:1.8.1\\((?<" + GROUP_NAME + ">\\d{6}\\.\\d{3})\\*kWh\\).+"),
        METER_READING_ELECTRICITY_DELIVERED_TO_CLIENT_TARIFF_2(".+?1-0:1.8.2\\((?<" + GROUP_NAME + ">\\d{6}\\.\\d{3})\\*kWh\\).+"),
        METER_READING_ELECTRICITY_DELIVERED_BY_CLIENT_TARIFF_1(".+?1-0:2.8.1\\((?<" + GROUP_NAME + ">\\d{6}\\.\\d{3})\\*kWh\\).+"),
        METER_READING_ELECTRICITY_DELIVERED_BY_CLIENT_TARIFF_2(".+?1-0:2.8.2\\((?<" + GROUP_NAME + ">\\d{6}\\.\\d{3})\\*kWh\\).+"),
        TARIFF_INDICATOR_ELECTRICITY(".+?0-0:96.14.0\\((?<" + GROUP_NAME + ">\\d{4})\\).+"),
        ACTUAL_ELECTRICITY_POWER_DELIVERED(".+?1-0:1.7.0\\((?<" + GROUP_NAME + ">\\d{2}\\.\\d{3})\\*kW\\).+"),
        ACTUAL_ELECTRICITY_POWER_RECIEVED(".+?1-0:2.7.0\\((?<" + GROUP_NAME + ">\\d{2}\\.\\d{3})\\*kW\\).+"),
        NUMBER_OF_POWER_FAILURES_IN_ANY_PHASE(".+?0-0:96.7.21\\((?<"+ GROUP_NAME +">\\d{5})\\).+"),
        NUMBER_OF_LONG_POWER_FAILURES_IN_ANY_PHASE(".+?0-0:96.7.9\\((?<"+ GROUP_NAME +">\\d{5})\\).+"),
        LONG_POWER_FAILURE_EVENT_LOG(".+?1-0:99.97.0(?<" + GROUP_NAME + ">.+?).+"),
        NUMBER_OF_VOLTAGE_SAGS_IN_PHASE_L1(".+?1-0:32.32.0\\((?<"+ GROUP_NAME +">\\d{5})\\).+"),
        NUMBER_OF_VOLTAGE_SAGS_IN_PHASE_L2(".+?1-0:32.36.0\\((?<"+ GROUP_NAME +">\\d{5})\\).+"),
        TEXT_MESSAGE_CODES(".+?0-0:96.13.1\\((?<"+ GROUP_NAME +">.*?)\\).+"),
        TEXT_MESSAGE(".+?0-0:96.13.0\\((?<"+ GROUP_NAME +">.*?)\\).+"),
        INSTANTANEOUS_CURRENT_L1_IN_A_RESOLUTION(".+?1-0:31.7.0\\((?<"+ GROUP_NAME +">\\d{3})\\*A\\).+"),
        INSTANTANEOUS_ACTIVE_POWER_L1(".+?1-0:21.7.0\\((?<" + GROUP_NAME + ">\\d{2}\\.\\d{3})\\*kW\\).+"),
        INSTANTANEOUS_ACTIVE_POWER_L2(".+?1-0:22.7.0\\((?<" + GROUP_NAME + ">\\d{2}\\.\\d{3})\\*kW\\).+"),
        DEVICE_TYPE(".+?0-1:24.1.0\\((?<"+ GROUP_NAME +">\\d{3})\\).+"),
        EQUIPMENT_IDENTIFIER_GAS(".+?0-1:96.1.0\\((?<"+ GROUP_NAME +">.+?)\\).+"),
        LAST_HOURLY_VALUE_OF_TEMPERATURE_CONVERTED_GAS_DELIVERED_TO_CLIENT_CAPTURE_TIMESTAMP(".+0-1:24.2.1\\((?<"+ GROUP_NAME + ">\\d{" + DSMR_TIMESTAMP_FORMAT.length() + "}).+"),
        LAST_HOURLY_VALUE_OF_TEMPERATURE_CONVERTED_GAS_DELIVERED_TO_CLIENT_CAPTURE_TIMESTAMP_DST_INDICATOR(".+?0-1:24.2.1\\(\\d{" + DSMR_TIMESTAMP_FORMAT.length() + "}(?<" + GROUP_NAME + ">(S|W))\\).+"),
        LAST_HOURLY_VALUE_OF_TEMPERATURE_CONVERTED_GAS_DELIVERED_TO_CLIENT_IN_M3(".+?0-1:24.2.1\\(\\d{" + DSMR_TIMESTAMP_FORMAT.length() + "}(S|W)\\)\\((?<" + GROUP_NAME + ">.+?)\\*m3\\).+"),
        ;

        private Pattern pattern;

        AttributeValue(String pattern) {
            this.pattern = Pattern.compile(pattern, Pattern.DOTALL);
        }

        public String extractFromMessage(String message) {
            Matcher matcher = pattern.matcher(message);
            if (matcher.matches()) {
                return matcher.group(GROUP_NAME);
            } else {
                return null;
            }
        }
    }

    public SmartMeterMessage parse(String message) throws InvalidSmartMeterMessageException {
        String[] linesInMessage = message.split("\n|\r\n");
        verifyChecksum(linesInMessage);

        SmartMeterMessage smartMeterMessage = new SmartMeterMessage();
        smartMeterMessage.setHeader(AttributeValue.HEADER.extractFromMessage(message));
        smartMeterMessage.setVersionInformationForP1Output(AttributeValue.VERSION_INFORMATION_FOR_P1_OUTPUT.extractFromMessage(message));
        smartMeterMessage.setTimestamp(toDate(AttributeValue.DATETIMESTAMP_OF_THE_P1_MESSAGE.extractFromMessage(message)));
        smartMeterMessage.setTimestampDstIndicator(toDstindicator(AttributeValue.DATETIMESTAMP_OF_THE_P1_MESSAGE_DST_INDICATOR.extractFromMessage(message)));
        smartMeterMessage.setEquipmentIdentifierElectricity(AttributeValue.EQUIPMENT_IDENTIFIER_ELECTRICITY.extractFromMessage(message));
        smartMeterMessage.setMeterReadingElectricityDeliveredToClientTariff1(new BigDecimal(AttributeValue.METER_READING_ELECTRICITY_DELIVERED_TO_CLIENT_TARIFF_1.extractFromMessage(message)));
        smartMeterMessage.setMeterReadingElectricityDeliveredToClientTariff2(new BigDecimal(AttributeValue.METER_READING_ELECTRICITY_DELIVERED_TO_CLIENT_TARIFF_2.extractFromMessage(message)));
        smartMeterMessage.setMeterReadingElectricityDeliveredByClientTariff1(new BigDecimal(AttributeValue.METER_READING_ELECTRICITY_DELIVERED_BY_CLIENT_TARIFF_1.extractFromMessage(message)));
        smartMeterMessage.setMeterReadingElectricityDeliveredByClientTariff2(new BigDecimal(AttributeValue.METER_READING_ELECTRICITY_DELIVERED_BY_CLIENT_TARIFF_2.extractFromMessage(message)));
        smartMeterMessage.setTariffIndicatorElectricity(AttributeValue.TARIFF_INDICATOR_ELECTRICITY.extractFromMessage(message));
        smartMeterMessage.setActualElectricityPowerDelivered(new BigDecimal(AttributeValue.ACTUAL_ELECTRICITY_POWER_DELIVERED.extractFromMessage(message)));
        smartMeterMessage.setActualElectricityPowerRecieved(new BigDecimal(AttributeValue.ACTUAL_ELECTRICITY_POWER_RECIEVED.extractFromMessage(message)));
        smartMeterMessage.setNumberOfPowerFailuresInAnyPhase(Integer.parseInt(AttributeValue.NUMBER_OF_POWER_FAILURES_IN_ANY_PHASE.extractFromMessage(message)));
        smartMeterMessage.setNumberOfLongPowerFailuresInAnyPhase(Integer.parseInt(AttributeValue.NUMBER_OF_LONG_POWER_FAILURES_IN_ANY_PHASE.extractFromMessage(message)));
        smartMeterMessage.setNumberOfVoltageSagsInPhaseL1(Integer.parseInt(AttributeValue.NUMBER_OF_VOLTAGE_SAGS_IN_PHASE_L1.extractFromMessage(message)));
        smartMeterMessage.setNumberOfVoltageSagsInPhaseL2(Integer.parseInt(AttributeValue.NUMBER_OF_VOLTAGE_SAGS_IN_PHASE_L2.extractFromMessage(message)));
        smartMeterMessage.setTextMessageCodes(StringUtils.defaultIfEmpty(AttributeValue.TEXT_MESSAGE_CODES.extractFromMessage(message), null));
        smartMeterMessage.setTextMessage(StringUtils.defaultIfEmpty(AttributeValue.TEXT_MESSAGE.extractFromMessage(message), null));
        smartMeterMessage.setInstantaneousCurrentL1(Integer.parseInt(AttributeValue.INSTANTANEOUS_CURRENT_L1_IN_A_RESOLUTION.extractFromMessage(message)));
        smartMeterMessage.setInstantaneousActivePowerL1(new BigDecimal(AttributeValue.INSTANTANEOUS_ACTIVE_POWER_L1.extractFromMessage(message)));
        smartMeterMessage.setInstantaneousActivePowerL2(new BigDecimal(AttributeValue.INSTANTANEOUS_ACTIVE_POWER_L2.extractFromMessage(message)));
        smartMeterMessage.setDeviceType(AttributeValue.DEVICE_TYPE.extractFromMessage(message));
        smartMeterMessage.setEquipmentIdentifierGas(AttributeValue.EQUIPMENT_IDENTIFIER_GAS.extractFromMessage(message));
        smartMeterMessage.setLastHourlyValueOfTemperatureConvertedGasDeliveredToClient(new BigDecimal(AttributeValue.LAST_HOURLY_VALUE_OF_TEMPERATURE_CONVERTED_GAS_DELIVERED_TO_CLIENT_IN_M3.extractFromMessage(message)));
        smartMeterMessage.setLastHourlyValueOfTemperatureConvertedGasDeliveredToClientCaptureTimestamp(toDate(AttributeValue.LAST_HOURLY_VALUE_OF_TEMPERATURE_CONVERTED_GAS_DELIVERED_TO_CLIENT_CAPTURE_TIMESTAMP.extractFromMessage(message)));
        smartMeterMessage.setLastHourlyValueOfTemperatureConvertedGasDeliveredToClientCaptureTimestampDstIndicator(toDstindicator(AttributeValue.LAST_HOURLY_VALUE_OF_TEMPERATURE_CONVERTED_GAS_DELIVERED_TO_CLIENT_CAPTURE_TIMESTAMP_DST_INDICATOR.extractFromMessage(message)));
        return smartMeterMessage;
    }

    private SmartMeterMessage.DstIndicator toDstindicator(String dstIndicatorFromMessage) throws InvalidSmartMeterMessageException {
        switch (dstIndicatorFromMessage) {
            case "W":
                return SmartMeterMessage.DstIndicator.WINTER;
            case "S":
                return SmartMeterMessage.DstIndicator.SUMMER;
            default:
                throw new InvalidSmartMeterMessageException("Invalid value for dst indicator: " + dstIndicatorFromMessage);
        }
    }

    private Date toDate(String dateFromMessage) throws InvalidSmartMeterMessageException {
        try {
            return new SimpleDateFormat(DSMR_TIMESTAMP_FORMAT).parse(dateFromMessage);
        } catch (ParseException e) {
            throw new InvalidSmartMeterMessageException("Invalid date: " + dateFromMessage);
        }
    }

    private void verifyChecksum(String[] linesInMessage) throws InvalidSmartMeterMessageException {
        String messageForCalculatingCrc = String.join("\r\n", ArrayUtils.subarray(linesInMessage, 0, linesInMessage.length - 1)) + "\r\n!";
        int calculatedCrc16 = Crc16.calculate(messageForCalculatingCrc);

        String actual = Integer.toHexString(getCrc(linesInMessage));
        String expected = Integer.toHexString(calculatedCrc16);

        LOG.debug("Actual CRC / Expected CRC : " + actual + " / " + expected);

        if (getCrc(linesInMessage) != calculatedCrc16) {
            throw new InvalidSmartMeterMessageException("CRC checksum failed. Expected " + expected + " but was " + actual);
        }
    }

    private int getCrc(String[] linesInMessage) {
        return Integer.parseInt(linesInMessage[linesInMessage.length - 1].substring(1, 5), 16);
    }

    public static class InvalidSmartMeterMessageException extends Exception {
        public InvalidSmartMeterMessageException(String message) {
            super(message);
        }
    }
}
