package nl.wiegman.smartmeter;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SmartMeterMessage {

    private static final Logger LOG = LoggerFactory.getLogger(SmartMeterReaderNative.class);

    // OBIS codes
    public static final String VERSION_INFORMATION_FOR_P1_OUTPUT = "1-3:0.2.8";
    public static final String DATETIMESTAMP_OF_THE_P1_MESSAGE = "0-0:1.0.0";
    public static final String EQUIPMENT_IDENTIFIER = "0-0:96.1.1";
    public static final String METER_READING_ELECTRICITY_DELIVERED_TO_CLIENT_TARIFF_1 = "1-0:1.8.1";
    public static final String METER_READING_ELECTRICITY_DELIVERED_TO_CLIENT_TARIFF_2 = "1-0:1.8.2";
    public static final String METER_READING_ELECTRICITY_DELIVERED_BY_CLIENT_TARIFF_1 = "1-0:2.8.1";
    public static final String METER_READING_ELECTRICITY_DELIVERED_BY_CLIENT_TARIFF_2 = "1-0:2.8.2";
    public static final String TARIFF_INDICATOR_ELECTRICITY = "0-0:96.14.0";
    public static final String ACTUAL_ELECTRICITY_POWER_DELIVERED = "1-0:1.7.0";
    public static final String LAST_HOURLY_VALUE_GAS_DELIVERED_TO_CLIENT = "0-1:24.2.1";

    private static final String DST_ACTIVE = "S";
    private static final String DST_NOT_ACTIVE = "W";

    private String[] linesInMessage;

    public SmartMeterMessage(String message) throws InvalidSmartMeterMessageException {
        this.linesInMessage = message.split("\n|\r\n");
        verifyChecksum();
    }

    public SmartMeterMessage(String[] linesInMessage) throws InvalidSmartMeterMessageException {
        this.linesInMessage = linesInMessage;
        verifyChecksum();
    }

    public String getVersionInformationForP1Output() {
        return getSingleStringValue(VERSION_INFORMATION_FOR_P1_OUTPUT);
    }

    public String getEquipmentIdentifier() {
        return getSingleStringValue(EQUIPMENT_IDENTIFIER);
    }

    public Date getDatetimestamp() {
        try {
            return stringToDate(getSingleStringValue(DATETIMESTAMP_OF_THE_P1_MESSAGE));
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    public BigDecimal getMeterReadingElectricityDeliveredToClientTariff1() {
        return kwhStringToBigDecimal(getSingleStringValue(METER_READING_ELECTRICITY_DELIVERED_TO_CLIENT_TARIFF_1));
    }

    public BigDecimal getMeterReadingElectricityDeliveredToClientTariff2() {
        return kwhStringToBigDecimal(getSingleStringValue(METER_READING_ELECTRICITY_DELIVERED_TO_CLIENT_TARIFF_2));
    }

    public BigDecimal getMeterReadingElectricityDeliveredByClientTariff1() {
        return kwhStringToBigDecimal(getSingleStringValue(METER_READING_ELECTRICITY_DELIVERED_BY_CLIENT_TARIFF_1));
    }

    public BigDecimal getMeterReadingElectricityDeliveredByClientTariff2() {
        return kwhStringToBigDecimal(getSingleStringValue(METER_READING_ELECTRICITY_DELIVERED_BY_CLIENT_TARIFF_2));
    }

    public String getTariffIndicatorElectricity() {
        return getSingleStringValue(TARIFF_INDICATOR_ELECTRICITY);
    }

    public BigDecimal getActualElectricityPowerDelivered() {
        return kwStringToBigDecimal(getSingleStringValue(ACTUAL_ELECTRICITY_POWER_DELIVERED));
    }

    public BigDecimal getLastHourlyValueGasDeliveredToClient() {
        String[] stringValues = getMultipleStringValue(LAST_HOURLY_VALUE_GAS_DELIVERED_TO_CLIENT);
        return m3StringToBigDecimal(stringValues[1]);
    }

    private int getCrc() {
        return Integer.parseInt(linesInMessage[linesInMessage.length - 1].substring(1, 5), 16);
    }

    private Date stringToDate(String value) throws ParseException {
        String dstActive = value.substring(13, 13);

        if (DST_ACTIVE.equals(dstActive)) {
            // Now what?
        } else if (DST_NOT_ACTIVE.equals(dstActive)) {
            // Now what?
        }
        return new SimpleDateFormat("yyMMddhhmmss").parse(value.substring(0, 12));
    }

    private String getSingleStringValue(String key) {
        String result = null;

        for (String line : linesInMessage) {
            if (line.startsWith(key)) {
                result = StringUtils.substringBetween(line, "(", ")");
            }
        }
        return result;
    }

    private String[] getMultipleStringValue(String key) {
        String[] result = null;

        for (String line : linesInMessage) {
            if (line.startsWith(key)) {
                result = StringUtils.substringsBetween(line, "(", ")");
                break;
            }
        }
        return result;
    }


    private BigDecimal kwhStringToBigDecimal(String value) {
        BigDecimal result = null;

        if (value != null) {
            result = new BigDecimal(value.replace("*kWh", ""));
        }
        return result;
    }

    private BigDecimal kwStringToBigDecimal(String value) {
        BigDecimal result = null;

        if (value != null) {
            result = new BigDecimal(value.replace("*kW", ""));
        }
        return result;
    }

    private BigDecimal m3StringToBigDecimal(String value) {
        BigDecimal result = null;

        if (value != null) {
            result = new BigDecimal(value.replace("*m3", ""));
        }
        return result;
    }

    private void verifyChecksum() throws InvalidSmartMeterMessageException {
        String messageForCalculatingCrc = String.join("\r\n", ArrayUtils.subarray(linesInMessage, 0, linesInMessage.length-1)) + "\r\n!";

        // Both seems to be correct:
        int calculatedCrc16 = Crc16.calculate(messageForCalculatingCrc);

        LOG.debug("CRC from message text / Calculated CRC: " + Integer.toHexString(getCrc()) + "/" + Integer.toHexString(calculatedCrc16));

        if (getCrc() != calculatedCrc16) {
            throw new InvalidSmartMeterMessageException();
        }
    }

    @Override
    public String toString() {
        return String.join("\r\n", linesInMessage);
    }

    public static class InvalidSmartMeterMessageException extends Exception { }
}
