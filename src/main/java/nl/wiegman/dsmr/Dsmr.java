package nl.wiegman.dsmr;

import org.apache.commons.lang3.ArrayUtils;
import sun.misc.CRC16;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Dsmr {

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

    // https://github.com/matthijskooijman/arduino-dsmr/blob/master/examples/parse/parse.ino
    // https://github.com/robertklep/node-dsmr-parser/blob/master/index.js

    public Dsmr(String message) throws InvalidChecksumException {
        this.linesInMessage = message.split("\n|\r\n");
        verifyChecksum();
    }

    public String getVersionInformationForP1Output() {
        return getSingleStringValue(VERSION_INFORMATION_FOR_P1_OUTPUT);
    }

    public String getEquipmentIdentifier() {
        return getSingleStringValue(EQUIPMENT_IDENTIFIER);
    }

    public Date getDatetimestamp() throws ParseException {
        return stringToDate(getSingleStringValue(DATETIMESTAMP_OF_THE_P1_MESSAGE));
    }

    public Double getMeterReadingElectricityDeliveredToClientTariff1() {
        return kwhStringToDouble(getSingleStringValue(METER_READING_ELECTRICITY_DELIVERED_TO_CLIENT_TARIFF_1));
    }

    public Double getMeterReadingElectricityDeliveredToClientTariff2() {
        return kwhStringToDouble(getSingleStringValue(METER_READING_ELECTRICITY_DELIVERED_TO_CLIENT_TARIFF_2));
    }

    public Double getMeterReadingElectricityDeliveredByClientTariff1() {
        return kwhStringToDouble(getSingleStringValue(METER_READING_ELECTRICITY_DELIVERED_BY_CLIENT_TARIFF_1));
    }

    public Double getMeterReadingElectricityDeliveredByClientTariff2() {
        return kwhStringToDouble(getSingleStringValue(METER_READING_ELECTRICITY_DELIVERED_BY_CLIENT_TARIFF_2));
    }

    public String getTariffIndicatorElectricity() {
        return getSingleStringValue(TARIFF_INDICATOR_ELECTRICITY);
    }

    public Double getActualElectricityPowerDelivered() {
        return kwStringToDouble(getSingleStringValue(ACTUAL_ELECTRICITY_POWER_DELIVERED));
    }

    public Double getLastHourlyValueGasDeliveredToClient() {
        String[] stringValues = getMultipleStringValue(LAST_HOURLY_VALUE_GAS_DELIVERED_TO_CLIENT);
        return m3StringToDouble(stringValues[1]);
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

        String regex = "^" + key + "\\((.*)\\)$";

        Pattern pattern = Pattern.compile(regex);

        for (String line : linesInMessage) {
            Matcher m = pattern.matcher(line.trim());
            if (m.matches()) {
                result = m.group(1);
                break;
            }
        }

        return result;
    }

    private String[] getMultipleStringValue(String key) {
        List<String> result = new ArrayList<>();

        for (String line : linesInMessage) {
            if (line.startsWith(key)) {

                Pattern pattern = Pattern.compile(".*?\\((.*?)\\)", Pattern.DOTALL);
                Matcher matcher = pattern.matcher(line);

                while(matcher.find()) {
                    result.add(matcher.group(1));
                }
            }
        }
        return result.toArray(new String[] {});
    }


    private Double kwhStringToDouble(String value) {
        Double result = null;

        if (value != null) {
            result = Double.parseDouble(value.replace("*kWh", ""));
        }
        return result;
    }

    private Double kwStringToDouble(String value) {
        Double result = null;

        if (value != null) {
            result = Double.parseDouble(value.replace("*kW", ""));
        }
        return result;
    }

    private Double m3StringToDouble(String value) {
        Double result = null;

        if (value != null) {
            result = Double.parseDouble(value.replace("*m3", ""));
        }
        return result;
    }


    private void verifyChecksum() throws InvalidChecksumException {
        System.out.println("CRC from message text: " + Integer.toHexString(getCrc()));

        String messageForCalculatingCrc = String.join("\r\n", ArrayUtils.subarray(linesInMessage, 0, linesInMessage.length-1)) + "\r\n!";

//        CRC16 crc16 = new CRC16();
//        for (byte b : message.getBytes()) {
//            crc16.update(b);
//        }
//        System.out.println("sun_misc CRC: " + Integer.toHexString(crc16.value));
//
//        // using the polynomial: x16+x15+x2+1
//        int c1 = new Crc16_1(Crc16_1.stdPoly).calculate(message.getBytes(), 0xFFFF);
//        System.out.println("Crc16_1 CRC: " + Integer.toHexString(c1));

        int calculatedCrc16 = Crc16_2.calculate(messageForCalculatingCrc);
        System.out.println("Calculated CRC: " + Integer.toHexString(calculatedCrc16));

        if (getCrc() != calculatedCrc16) {
            throw new InvalidChecksumException();
        }
    }

    public static class InvalidChecksumException extends Exception { }
}
