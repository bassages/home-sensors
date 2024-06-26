package nl.homesensors.smartmeter;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
public class Dsmr422Parser {
    private static final String SUPPORTED_DSMR_VERSION = "42";
    private static final String DSMR_TIMESTAMP_FORMAT = "yyMMddHHmmss";
    private static final String GROUP_NAME = "attributeValue";

    private static final String KWH_VALUE = "\\d{6}\\.\\d{3})\\*kWh";
    private static final String KW_VALUE = "\\d{2}\\.\\d{3})\\*kW";
    private static final String FIVE_DIGITS = "\\d{5}";

    private static final Pattern HEADER = compile("^/(?<" + GROUP_NAME + ">.+?)\\R{2}");
    private static final Pattern VERSION_INFORMATION_FOR_P1_OUTPUT = compile("1-3:0.2.8\\((?<" + GROUP_NAME + ">\\w{2})\\)");
    private static final Pattern DATETIMESTAMP_OF_THE_P1_MESSAGE = compile("0-0:1.0.0\\((?<" + GROUP_NAME + ">\\d{" + DSMR_TIMESTAMP_FORMAT.length() + "})");
    private static final Pattern DATETIMESTAMP_OF_THE_P1_MESSAGE_DST_INDICATOR= compile("0-0:1.0.0.+?(?<" + GROUP_NAME + ">(S|W))\\)");
    private static final Pattern EQUIPMENT_IDENTIFIER_ELECTRICITY = compile("0-0:96.1.1\\((?<" + GROUP_NAME + ">\\w+)\\)");
    private static final Pattern METER_READING_ELECTRICITY_DELIVERED_TO_CLIENT_TARIFF_1 = compile("1-0:1.8.1\\((?<" + GROUP_NAME + ">" + KWH_VALUE + "\\)");
    private static final Pattern METER_READING_ELECTRICITY_DELIVERED_TO_CLIENT_TARIFF_2 = compile("1-0:1.8.2\\((?<" + GROUP_NAME + ">" + KWH_VALUE + "\\)");
    private static final Pattern METER_READING_ELECTRICITY_DELIVERED_BY_CLIENT_TARIFF_1 = compile("1-0:2.8.1\\((?<" + GROUP_NAME + ">" + KWH_VALUE + "\\)");
    private static final Pattern METER_READING_ELECTRICITY_DELIVERED_BY_CLIENT_TARIFF_2 = compile("1-0:2.8.2\\((?<" + GROUP_NAME + ">" + KWH_VALUE + "\\)");
    private static final Pattern TARIFF_INDICATOR_ELECTRICITY = compile("0-0:96.14.0\\((?<" + GROUP_NAME + ">\\d{4})\\)");
    private static final Pattern ACTUAL_ELECTRICITY_POWER_DELIVERED = compile("1-0:1.7.0\\((?<" + GROUP_NAME + ">" + KW_VALUE + "\\)");
    private static final Pattern ACTUAL_ELECTRICITY_POWER_RECIEVED = compile("1-0:2.7.0\\((?<" + GROUP_NAME + ">" + KW_VALUE + "\\)");
    private static final Pattern NUMBER_OF_POWER_FAILURES_IN_ANY_PHASE = compile("0-0:96.7.21\\((?<"+ GROUP_NAME + ">" + FIVE_DIGITS + ")\\)");
    private static final Pattern NUMBER_OF_LONG_POWER_FAILURES_IN_ANY_PHASE = compile("0-0:96.7.9\\((?<"+ GROUP_NAME + ">" + FIVE_DIGITS + ")\\)");
    private static final Pattern LONG_POWER_FAILURE_EVENT_LOG_NR_OF_ITEMS = compile("1-0:99.97.0\\((?<" + GROUP_NAME + ">\\d*)\\)");
    private static final Pattern LONG_POWER_FAILURE_EVENT_LOG_VALUE = compile("\\(0-0:96.7.19\\)(?<" + GROUP_NAME + ">.+?)\\R");
    private static final Pattern NUMBER_OF_VOLTAGE_SAGS_IN_PHASE_L1 = compile("1-0:32.32.0\\((?<"+ GROUP_NAME + ">" + FIVE_DIGITS + ")\\)");
    private static final Pattern NUMBER_OF_VOLTAGE_SAGS_IN_PHASE_L2 = compile("1-0:32.36.0\\((?<"+ GROUP_NAME + ">" + FIVE_DIGITS + ")\\)");
    private static final Pattern TEXT_MESSAGE_CODES = compile("0-0:96.13.1\\((?<"+ GROUP_NAME +">.*?)\\)");
    private static final Pattern TEXT_MESSAGE = compile("0-0:96.13.0\\((?<"+ GROUP_NAME +">.*?)\\)");
    private static final Pattern INSTANTANEOUS_CURRENT_L1_IN_A_RESOLUTION = compile("1-0:31.7.0\\((?<"+ GROUP_NAME +">\\d{3})\\*A\\)");
    private static final Pattern INSTANTANEOUS_ACTIVE_POWER_L1 = compile("1-0:21.7.0\\((?<" + GROUP_NAME + ">" + KW_VALUE + "\\)");
    private static final Pattern INSTANTANEOUS_ACTIVE_POWER_L2 = compile("1-0:22.7.0\\((?<" + GROUP_NAME + ">" + KW_VALUE + "\\)");
    private static final Pattern POWER_FAILURE_LOG_VALUE_TIMESTAMP_PATTERN = Pattern.compile("(?<timestamp>\\d{" + DSMR_TIMESTAMP_FORMAT.length() + "})(?<dstIndicator>([SW]))\\)\\((?<duration>\\d{10})\\*s\\)");

    // Slave devices (minimum of 0, maximum of 4)
    private static final Pattern[] DEVICE_TYPE = new Pattern[4];
    private static final Pattern[] DEVICE_EQUIPMENT_IDENTIFIER = new Pattern[4];
    private static final Pattern[] DEVICE_LAST_HOURLY_VALUE_CAPTURE_TIMESTAMP = new Pattern[4];
    private static final Pattern[] DEVICE_LAST_HOURLY_VALUE_CAPTURE_TIMESTAMP_DST_INDICATOR = new Pattern[4];
    private static final Pattern[] DEVICE_LAST_HOURLY_VALUE_DELIVERED_TO_CLIENT = new Pattern[4];

    static {
        for (int i = 0; i < 4; i++) {
            final int deviceNumber = i + 1;
            DEVICE_TYPE[i] = compile("0-" + deviceNumber + ":24.1.0\\((?<"+ GROUP_NAME +">\\d{3})\\)");
            DEVICE_EQUIPMENT_IDENTIFIER[i] = compile("0-" + deviceNumber + ":96.1.0\\((?<"+ GROUP_NAME +">.+?)\\)");
            DEVICE_LAST_HOURLY_VALUE_CAPTURE_TIMESTAMP[i] = compile("0-" + deviceNumber + ":24.2.1\\((?<"+ GROUP_NAME + ">\\d{" + DSMR_TIMESTAMP_FORMAT.length() + "})");
            DEVICE_LAST_HOURLY_VALUE_CAPTURE_TIMESTAMP_DST_INDICATOR[i] = compile("0-" + deviceNumber + ":24.2.1\\(\\d{" + DSMR_TIMESTAMP_FORMAT.length() + "}(?<" + GROUP_NAME + ">(S|W))\\)");
            DEVICE_LAST_HOURLY_VALUE_DELIVERED_TO_CLIENT[i] = compile("0-" + deviceNumber + ":24.2.1\\(\\d{" + DSMR_TIMESTAMP_FORMAT.length() + "}(S|W)\\)\\((?<" + GROUP_NAME + ">.+?)\\*(m3|GJ|kWh)\\)");
        }
    }

    private static Pattern compile(final String pattern) {
        return Pattern.compile(pattern, Pattern.DOTALL);
    }

    private String extractFromMessage(final Pattern pattern, final String message) {
        final Matcher matcher = pattern.matcher(message);
        if (matcher.find()) {
            return matcher.group(GROUP_NAME);
        } else {
            return null;
        }
    }

    public SmartMeterMessage parse(final String message) throws InvalidSmartMeterMessageException, UnsupportedVersionException {
        final String[] linesInMessage = message.split("\n|\r\n");
        verifyChecksum(linesInMessage);

        final SmartMeterMessage smartMeterMessage = new SmartMeterMessage();

        final String versionInformationForP1Output = extractFromMessage(VERSION_INFORMATION_FOR_P1_OUTPUT, message);

        if (!SUPPORTED_DSMR_VERSION.equals(versionInformationForP1Output)) {
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
            final String deviceType = extractFromMessage(DEVICE_TYPE[i], message);
            if (DeviceType.GAS.getDeviceTypeIdentifier().equals(deviceType)) {
                smartMeterMessage.setEquipmentIdentifierGas(extractFromMessage(DEVICE_EQUIPMENT_IDENTIFIER[i], message));
                smartMeterMessage.setLastHourlyValueOfTemperatureConvertedGasDeliveredToClient(bigDecimalFromString(extractFromMessage(DEVICE_LAST_HOURLY_VALUE_DELIVERED_TO_CLIENT[i], message)));
                smartMeterMessage.setLastHourlyValueOfTemperatureConvertedGasDeliveredToClientCaptureTimestamp(
                        toLocalDateTime(extractFromMessage(DEVICE_LAST_HOURLY_VALUE_CAPTURE_TIMESTAMP[i], message)));
                smartMeterMessage.setLastHourlyValueOfTemperatureConvertedGasDeliveredToClientCaptureTimestampDstIndicator(toDstindicator(extractFromMessage(DEVICE_LAST_HOURLY_VALUE_CAPTURE_TIMESTAMP_DST_INDICATOR[i], message)));
            }
        }

        final String powerFailureEventLogNrOfItems = extractFromMessage(LONG_POWER_FAILURE_EVENT_LOG_NR_OF_ITEMS, message);
        if (StringUtils.isNotBlank(powerFailureEventLogNrOfItems)) {
            final Integer powerFailureEventLogNrOfItemsInteger = integerFromString(powerFailureEventLogNrOfItems);
            if (powerFailureEventLogNrOfItemsInteger != null && powerFailureEventLogNrOfItemsInteger > 0) {
                parsePowerFailureLog(message, smartMeterMessage);
            }
        }
        return smartMeterMessage;
    }

    private BigDecimal bigDecimalFromString(final String string) {
        return string == null ? null : new BigDecimal(string) ;
    }

    private Integer integerFromString(final String string) {
        return string == null ? null : Integer.valueOf(string) ;
    }

    private void parsePowerFailureLog(final String message, final SmartMeterMessage smartMeterMessage) throws InvalidSmartMeterMessageException {
        final String powerFailureEventLogValue = extractFromMessage(LONG_POWER_FAILURE_EVENT_LOG_VALUE, message);

        if (powerFailureEventLogValue != null) {
            final Matcher powerFailureLogTimestampMatcher = POWER_FAILURE_LOG_VALUE_TIMESTAMP_PATTERN.matcher(powerFailureEventLogValue);
            while (powerFailureLogTimestampMatcher.find()) {
                final LocalDateTime timestamp = toLocalDateTime(powerFailureLogTimestampMatcher.group("timestamp"));
                final SmartMeterMessage.DstIndicator dstIndicator = toDstindicator(powerFailureLogTimestampMatcher.group("dstIndicator"));
                final int duration = Integer.parseInt(powerFailureLogTimestampMatcher.group("duration"));

                final LongPowerFailureLogItem logItem = new LongPowerFailureLogItem();
                logItem.setFailureDurationInSeconds(duration);
                logItem.setTimestampOfEndOfFailure(timestamp);
                logItem.setTimestampOfEndOfFailureDstIndicator(dstIndicator);

                smartMeterMessage.addLongPowerFailureLogItem(logItem);
            }
        }
    }

    private SmartMeterMessage.DstIndicator toDstindicator(final String dstIndicatorFromMessage) throws InvalidSmartMeterMessageException {
        return switch (dstIndicatorFromMessage) {
            case "W" -> SmartMeterMessage.DstIndicator.WINTER;
            case "S" -> SmartMeterMessage.DstIndicator.SUMMER;
            default -> throw new InvalidSmartMeterMessageException("Invalid value for dst indicator: " + dstIndicatorFromMessage);
        };
    }

    private LocalDateTime toLocalDateTime(final String dateFromMessage) {
        return LocalDateTime.parse(dateFromMessage, DateTimeFormatter.ofPattern(DSMR_TIMESTAMP_FORMAT));
    }

    private String octetStringToString(final String octetString) {
        if (StringUtils.isBlank(octetString)) {
            return null;
        } else {
            final byte[] bs = new byte[octetString.length() / 2];
            for (int i = 0; i < octetString.length(); i += 2) {
                bs[i / 2] = (byte) Integer.parseInt(octetString.substring(i, i + 2), 16);
            }
            return new String(bs, StandardCharsets.UTF_8);
        }
    }

    private void verifyChecksum(final String... linesInMessage) throws InvalidSmartMeterMessageException {
        final String messageForCalculatingCrc = String.join("\r\n", ArrayUtils.subarray(linesInMessage, 0, linesInMessage.length - 1)) + "\r\n!";
        final int calculatedCrc16 = Crc16.calculate(messageForCalculatingCrc);

        final String actual = Integer.toHexString(getCrc(linesInMessage));
        final String expected = Integer.toHexString(calculatedCrc16);

        if (log.isDebugEnabled()) {
            log.debug("Actual CRC: {} / Expected CRC : {}", actual, expected.toUpperCase());
        }

        if (getCrc(linesInMessage) != calculatedCrc16) {
            throw new InvalidSmartMeterMessageException("CRC checksum failed. Expected " + expected + " but was " + actual);
        }
    }

    private int getCrc(final String[] linesInMessage) {
        return Integer.parseInt(linesInMessage[linesInMessage.length - 1].substring(1, 5), 16);
    }

    public static class InvalidSmartMeterMessageException extends Exception {
        public InvalidSmartMeterMessageException(final String message) {
            super(message);
        }
    }

    public static class UnsupportedVersionException extends Exception {
        public UnsupportedVersionException(final String message) {
            super(message);
        }
    }
}
