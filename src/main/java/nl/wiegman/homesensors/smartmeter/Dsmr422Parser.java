package nl.wiegman.homesensors.smartmeter;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class Dsmr422Parser {

    private static final Logger LOG = LoggerFactory.getLogger(Dsmr422Parser.class);

    private static final String SUPPORTED_DSMR_VERSION = "42";
    private static final String DSMR_TIMESTAMP_FORMAT = "yyMMddHHmmss";
    private static final String GROUP_NAME = "attributeValue";

    private static Pattern HEADER = compile("^/(?<" + GROUP_NAME + ">.+?)\\R{2}");
    private static Pattern VERSION_INFORMATION_FOR_P1_OUTPUT = compile("1-3:0.2.8\\((?<" + GROUP_NAME + ">\\w{2})\\)");
    private static Pattern DATETIMESTAMP_OF_THE_P1_MESSAGE = compile("0-0:1.0.0\\((?<" + GROUP_NAME + ">\\d{" + DSMR_TIMESTAMP_FORMAT.length() + "})");
    private static Pattern DATETIMESTAMP_OF_THE_P1_MESSAGE_DST_INDICATOR= compile("0-0:1.0.0.+?(?<" + GROUP_NAME + ">(S|W))\\)");
    private static Pattern EQUIPMENT_IDENTIFIER_ELECTRICITY = compile("0-0:96.1.1\\((?<" + GROUP_NAME + ">\\w+)\\)");
    private static Pattern METER_READING_ELECTRICITY_DELIVERED_TO_CLIENT_TARIFF_1 = compile("1-0:1.8.1\\((?<" + GROUP_NAME + ">\\d{6}\\.\\d{3})\\*kWh\\)");
    private static Pattern METER_READING_ELECTRICITY_DELIVERED_TO_CLIENT_TARIFF_2 = compile("1-0:1.8.2\\((?<" + GROUP_NAME + ">\\d{6}\\.\\d{3})\\*kWh\\)");
    private static Pattern METER_READING_ELECTRICITY_DELIVERED_BY_CLIENT_TARIFF_1 = compile("1-0:2.8.1\\((?<" + GROUP_NAME + ">\\d{6}\\.\\d{3})\\*kWh\\)");
    private static Pattern METER_READING_ELECTRICITY_DELIVERED_BY_CLIENT_TARIFF_2 = compile("1-0:2.8.2\\((?<" + GROUP_NAME + ">\\d{6}\\.\\d{3})\\*kWh\\)");
    private static Pattern TARIFF_INDICATOR_ELECTRICITY = compile("0-0:96.14.0\\((?<" + GROUP_NAME + ">\\d{4})\\)");
    private static Pattern ACTUAL_ELECTRICITY_POWER_DELIVERED = compile("1-0:1.7.0\\((?<" + GROUP_NAME + ">\\d{2}\\.\\d{3})\\*kW\\)");
    private static Pattern ACTUAL_ELECTRICITY_POWER_RECIEVED = compile("1-0:2.7.0\\((?<" + GROUP_NAME + ">\\d{2}\\.\\d{3})\\*kW\\)");
    private static Pattern NUMBER_OF_POWER_FAILURES_IN_ANY_PHASE = compile("0-0:96.7.21\\((?<"+ GROUP_NAME +">\\d{5})\\)");
    private static Pattern NUMBER_OF_LONG_POWER_FAILURES_IN_ANY_PHASE = compile("0-0:96.7.9\\((?<"+ GROUP_NAME +">\\d{5})\\)");
    private static Pattern LONG_POWER_FAILURE_EVENT_LOG_NR_OF_ITEMS = compile("1-0:99.97.0\\((?<" + GROUP_NAME + ">\\d*)\\)");
    private static Pattern LONG_POWER_FAILURE_EVENT_LOG_VALUE = compile("\\(0-0:96.7.19\\)(?<" + GROUP_NAME + ">.+?)\\R");
    private static Pattern NUMBER_OF_VOLTAGE_SAGS_IN_PHASE_L1 = compile("1-0:32.32.0\\((?<"+ GROUP_NAME +">\\d{5})\\)");
    private static Pattern NUMBER_OF_VOLTAGE_SAGS_IN_PHASE_L2 = compile("1-0:32.36.0\\((?<"+ GROUP_NAME +">\\d{5})\\)");
    private static Pattern TEXT_MESSAGE_CODES = compile("0-0:96.13.1\\((?<"+ GROUP_NAME +">.*?)\\)");
    private static Pattern TEXT_MESSAGE = compile("0-0:96.13.0\\((?<"+ GROUP_NAME +">.*?)\\)");
    private static Pattern INSTANTANEOUS_CURRENT_L1_IN_A_RESOLUTION = compile("1-0:31.7.0\\((?<"+ GROUP_NAME +">\\d{3})\\*A\\)");
    private static Pattern INSTANTANEOUS_ACTIVE_POWER_L1 = compile("1-0:21.7.0\\((?<" + GROUP_NAME + ">\\d{2}\\.\\d{3})\\*kW\\)");
    private static Pattern INSTANTANEOUS_ACTIVE_POWER_L2 = compile("1-0:22.7.0\\((?<" + GROUP_NAME + ">\\d{2}\\.\\d{3})\\*kW\\)");

    // Slave devices (minimum of 0, maximum of 4)
    private static Pattern[] DEVICE_TYPE = new Pattern[4];
    private static Pattern[] DEVICE_EQUIPMENT_IDENTIFIER = new Pattern[4];
    private static Pattern[] DEVICE_LAST_HOURLY_VALUE_CAPTURE_TIMESTAMP = new Pattern[4];
    private static Pattern[] DEVICE_LAST_HOURLY_VALUE_CAPTURE_TIMESTAMP_DST_INDICATOR = new Pattern[4];
    private static Pattern[] DEVICE_LAST_HOURLY_VALUE_DELIVERED_TO_CLIENT = new Pattern[4];

    static {
        for (int i = 0; i < 4; i++) {
            int deviceNumber = i + 1;
            DEVICE_TYPE[i] = compile("0-" + deviceNumber + ":24.1.0\\((?<"+ GROUP_NAME +">\\d{3})\\)");
            DEVICE_EQUIPMENT_IDENTIFIER[i] = compile("0-" + deviceNumber + ":96.1.0\\((?<"+ GROUP_NAME +">.+?)\\)");
            DEVICE_LAST_HOURLY_VALUE_CAPTURE_TIMESTAMP[i] = compile("0-" + deviceNumber + ":24.2.1\\((?<"+ GROUP_NAME + ">\\d{" + DSMR_TIMESTAMP_FORMAT.length() + "})");
            DEVICE_LAST_HOURLY_VALUE_CAPTURE_TIMESTAMP_DST_INDICATOR[i] = compile("0-" + deviceNumber + ":24.2.1\\(\\d{" + DSMR_TIMESTAMP_FORMAT.length() + "}(?<" + GROUP_NAME + ">(S|W))\\)");
            DEVICE_LAST_HOURLY_VALUE_DELIVERED_TO_CLIENT[i] = compile("0-" + deviceNumber + ":24.2.1\\(\\d{" + DSMR_TIMESTAMP_FORMAT.length() + "}(S|W)\\)\\((?<" + GROUP_NAME + ">.+?)\\*(m3|GJ|kWh)\\)");
        }
    }

    private static Pattern compile(String pattern) {
        return Pattern.compile(pattern, Pattern.DOTALL);
    }

    private String extractFromMessage(Pattern pattern, String message) {
        Matcher matcher = pattern.matcher(message);
        if (matcher.find()) {
            return matcher.group(GROUP_NAME);
        } else {
            return null;
        }
    }

    private static Pattern POWER_FAILURE_LOG_VALUE_TIMESTAMP_PATTERN = Pattern.compile("(?<timestamp>\\d{" + DSMR_TIMESTAMP_FORMAT.length() + "})(?<dstIndicator>(S|W))\\)\\((?<duration>\\d{10})\\*s\\)");

    public SmartMeterMessage parse(String message) throws InvalidSmartMeterMessageException, UnsupportedVersionException {
        String[] linesInMessage = message.split("\n|\r\n");
        verifyChecksum(linesInMessage);

        SmartMeterMessage smartMeterMessage = new SmartMeterMessage();

        String versionInformationForP1Output = extractFromMessage(VERSION_INFORMATION_FOR_P1_OUTPUT, message);

        if (!versionInformationForP1Output.equals(SUPPORTED_DSMR_VERSION)) {
            throw new UnsupportedVersionException("Unsupported DSMR version: " + versionInformationForP1Output + " (The supported version is " + SUPPORTED_DSMR_VERSION + ")");
        }

        smartMeterMessage.setVersionInformationForP1Output(versionInformationForP1Output);
        smartMeterMessage.setHeader(extractFromMessage(HEADER, message));
        smartMeterMessage.setTimestamp(toLocalDateTime(extractFromMessage(DATETIMESTAMP_OF_THE_P1_MESSAGE, message)));
        smartMeterMessage.setTimestampDstIndicator(toDstindicator(extractFromMessage(DATETIMESTAMP_OF_THE_P1_MESSAGE_DST_INDICATOR, message)));
        smartMeterMessage.setEquipmentIdentifierElectricity(extractFromMessage(EQUIPMENT_IDENTIFIER_ELECTRICITY, message));
        smartMeterMessage.setMeterReadingElectricityDeliveredToClientTariff1(bigDecimalFromString(extractFromMessage(METER_READING_ELECTRICITY_DELIVERED_TO_CLIENT_TARIFF_1, message)));
        smartMeterMessage.setMeterReadingElectricityDeliveredToClientTariff2(bigDecimalFromString(extractFromMessage(METER_READING_ELECTRICITY_DELIVERED_TO_CLIENT_TARIFF_2, message)));
        smartMeterMessage.setMeterReadingElectricityDeliveredByClientTariff1(bigDecimalFromString(extractFromMessage(METER_READING_ELECTRICITY_DELIVERED_BY_CLIENT_TARIFF_1, message)));
        smartMeterMessage.setMeterReadingElectricityDeliveredByClientTariff2(bigDecimalFromString(extractFromMessage(METER_READING_ELECTRICITY_DELIVERED_BY_CLIENT_TARIFF_2, message)));
        smartMeterMessage.setTariffIndicatorElectricity(integerFromString(extractFromMessage(TARIFF_INDICATOR_ELECTRICITY, message)));
        smartMeterMessage.setActualElectricityPowerDelivered(bigDecimalFromString(extractFromMessage(ACTUAL_ELECTRICITY_POWER_DELIVERED, message)));
        smartMeterMessage.setActualElectricityPowerRecieved(bigDecimalFromString(extractFromMessage(ACTUAL_ELECTRICITY_POWER_RECIEVED, message)));
        smartMeterMessage.setNumberOfPowerFailuresInAnyPhase(integerFromString(extractFromMessage(NUMBER_OF_POWER_FAILURES_IN_ANY_PHASE, message)));
        smartMeterMessage.setNumberOfLongPowerFailuresInAnyPhase(integerFromString(extractFromMessage(NUMBER_OF_LONG_POWER_FAILURES_IN_ANY_PHASE, message)));
        smartMeterMessage.setNumberOfVoltageSagsInPhaseL1(integerFromString(extractFromMessage(NUMBER_OF_VOLTAGE_SAGS_IN_PHASE_L1, message)));
        smartMeterMessage.setNumberOfVoltageSagsInPhaseL2(integerFromString(extractFromMessage(NUMBER_OF_VOLTAGE_SAGS_IN_PHASE_L2, message)));
        smartMeterMessage.setTextMessageCodes(octetStringToString(extractFromMessage(TEXT_MESSAGE_CODES, message)));
        smartMeterMessage.setTextMessage(octetStringToString(extractFromMessage(TEXT_MESSAGE, message)));
        smartMeterMessage.setInstantaneousCurrentL1(integerFromString(extractFromMessage(INSTANTANEOUS_CURRENT_L1_IN_A_RESOLUTION, message)));
        smartMeterMessage.setInstantaneousActivePowerL1(bigDecimalFromString(extractFromMessage(INSTANTANEOUS_ACTIVE_POWER_L1, message)));
        smartMeterMessage.setInstantaneousActivePowerL2(bigDecimalFromString(extractFromMessage(INSTANTANEOUS_ACTIVE_POWER_L2, message)));

        for (int i = 0; i < 4; i++) {
            String deviceType = extractFromMessage(DEVICE_TYPE[i], message);
            if (deviceType != null && DeviceType.GAS.getDeviceTypeIdentifier().equals(deviceType)) {
                smartMeterMessage.setEquipmentIdentifierGas(extractFromMessage(DEVICE_EQUIPMENT_IDENTIFIER[i], message));
                smartMeterMessage.setLastHourlyValueOfTemperatureConvertedGasDeliveredToClient(bigDecimalFromString(extractFromMessage(DEVICE_LAST_HOURLY_VALUE_DELIVERED_TO_CLIENT[i], message)));
                smartMeterMessage.setLastHourlyValueOfTemperatureConvertedGasDeliveredToClientCaptureTimestamp(
                        toLocalDateTime(extractFromMessage(DEVICE_LAST_HOURLY_VALUE_CAPTURE_TIMESTAMP[i], message)));
                smartMeterMessage.setLastHourlyValueOfTemperatureConvertedGasDeliveredToClientCaptureTimestampDstIndicator(toDstindicator(extractFromMessage(DEVICE_LAST_HOURLY_VALUE_CAPTURE_TIMESTAMP_DST_INDICATOR[i], message)));
            }
        }

        String powerFailureEventLogNrOfItems = extractFromMessage(LONG_POWER_FAILURE_EVENT_LOG_NR_OF_ITEMS, message);
        if (StringUtils.isNotBlank(powerFailureEventLogNrOfItems)) {
            Integer powerFailureEventLogNrOfItemsInteger = integerFromString(powerFailureEventLogNrOfItems);
            if (powerFailureEventLogNrOfItemsInteger != null && powerFailureEventLogNrOfItemsInteger > 0) {
                parsePowerFailureLog(message, smartMeterMessage);
            }
        }
        return smartMeterMessage;
    }

    private BigDecimal bigDecimalFromString(String string) {
        return string == null ? null : new BigDecimal(string) ;
    }

    private Integer integerFromString(String string) {
        return string == null ? null : Integer.valueOf(string) ;
    }

    private void parsePowerFailureLog(String message, SmartMeterMessage smartMeterMessage) throws InvalidSmartMeterMessageException {
        String powerFailureEventLogValue = extractFromMessage(LONG_POWER_FAILURE_EVENT_LOG_VALUE, message);

        if (powerFailureEventLogValue != null) {
            Matcher powerFailureLogTimestampMatcher = POWER_FAILURE_LOG_VALUE_TIMESTAMP_PATTERN.matcher(powerFailureEventLogValue);
            while (powerFailureLogTimestampMatcher.find()) {
                LocalDateTime timestamp = toLocalDateTime(powerFailureLogTimestampMatcher.group("timestamp"));
                SmartMeterMessage.DstIndicator dstIndicator = toDstindicator(powerFailureLogTimestampMatcher.group("dstIndicator"));
                int duration = Integer.parseInt(powerFailureLogTimestampMatcher.group("duration"));

                LongPowerFailureLogItem logItem = new LongPowerFailureLogItem();
                logItem.setFailureDurationInSeconds(duration);
                logItem.setTimestampOfEndOfFailure(timestamp);
                logItem.setTimestampOfEndOfFailureDstIndicator(dstIndicator);

                smartMeterMessage.addLongPowerFailureLogItem(logItem);
            }
        }
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

    private LocalDateTime toLocalDateTime(String dateFromMessage) {
        return LocalDateTime.parse(dateFromMessage, DateTimeFormatter.ofPattern(DSMR_TIMESTAMP_FORMAT));
    }

    private String octetStringToString(String octetString) {
        if (StringUtils.isBlank(octetString)) {
            return null;
        } else {
            byte bs[] = new byte[octetString.length() / 2];
            for (int i = 0; i < octetString.length(); i += 2) {
                bs[i / 2] = (byte) Integer.parseInt(octetString.substring(i, i + 2), 16);
            }
            return new String(bs, StandardCharsets.UTF_8);
        }
    }

    private void verifyChecksum(String[] linesInMessage) throws InvalidSmartMeterMessageException {
        String messageForCalculatingCrc = String.join("\r\n", ArrayUtils.subarray(linesInMessage, 0, linesInMessage.length - 1)) + "\r\n!";
        int calculatedCrc16 = Crc16.calculate(messageForCalculatingCrc);

        String actual = Integer.toHexString(getCrc(linesInMessage));
        String expected = Integer.toHexString(calculatedCrc16);

        LOG.debug("Actual CRC: " + actual + " / Expected CRC : " + expected.toUpperCase());

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

    public static class UnsupportedVersionException extends Exception {
        public UnsupportedVersionException(String message) {
            super(message);
        }
    }

}
