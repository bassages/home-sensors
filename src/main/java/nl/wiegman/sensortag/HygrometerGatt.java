package nl.wiegman.sensortag;

/**
 * Hardware on TI SensorTag: Sensirion SHT21 @ U6
 *
 * Two types of data are obtained from the Humidity sensor, relative humidity and ambient temperature
 * 
 * ----------------------------------------------------------------------------------------
 * | Type                | UUID   | Read/Write  | Format                                  |
 * |--------------------------------------------------------------------------------------|
 * | <Data>              | AA21 * | Read/Notify | TempLSB TempMSB HumLSB HumMSB (4 bytes) |
 * | <Data Notification> | -      | R/W         | 2 bytes                                 |
 * | <Configuration>     | AA22 * | R/W         | 1 byte                                  |
 * | <Period>            | AA23 * | R/W         | 1 byte                                  |
 * ----------------------------------------------------------------------------------------
 *
 * The driver for this sensor is using a state machine so when the enable command is issued, the sensor starts to perform one measurements and the data is stored in the <Data>. 
 * 
 * To obtain data either use notifications or read the data directly. The update rate ranges from 100 ms to 2.55 seconds.
 * 
 * The humidity and temperature data in the sensor is issued and measured explicitly where the humidity data takes ~64ms to measure. 
 */
public class HygrometerGatt {

    public static double convertTemperature(String hexValue) {
        String[] hexValues = hexValue.split(" ");
        if (hexValues.length == 4) {
            int rawTemperature = Integer.parseInt(hexValues[1] + hexValues[0], 16);
            return getAmbientTemperature(rawTemperature);
        } else {
            return 0.0;
        }
    }

    public static double fromHex(String hexValue) {
        String[] hexValues = hexValue.split(" ");
        if (hexValues.length == 4) {
            int rawHumidity = Integer.parseInt(hexValues[3] + hexValues[2], 16);
            return getHumidity(rawHumidity);
        } else {
            return 0.0;
        }
    }

    private static float getAmbientTemperature(int temperatureRaw) {
        return -46.85f + 175.72f/65536f *(float)temperatureRaw;
    }

    private static float getHumidity(int rawHumidity) {
        // bits [1..0] are status bits and need to be cleared according to the user guide,
        // but the iOS code doesn't bother. It should have minimal impact.
        rawHumidity = rawHumidity - (rawHumidity % 4);
        return (-6f) + 125f * (rawHumidity / 65535f);
    }
}
