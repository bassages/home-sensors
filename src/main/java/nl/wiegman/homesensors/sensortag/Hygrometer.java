package nl.wiegman.homesensors.sensortag;

import net.sf.expectit.Expect;

import java.io.IOException;
import java.math.BigDecimal;

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
public class Hygrometer extends AbstractSensor {

    private static final String NOTIFICATION_REGEXP = "Notification handle = 0x003b value: (?!00 00 00 00)(\\w{2} \\w{2} \\w{2} \\w{2})";

    public BigDecimal getHumidity(Expect expect) throws IOException, SensortagException {
        enable(expect);
        String value = expectSuccesfulMatch(expect, NOTIFICATION_REGEXP);
        disable(expect);
        discardNotifications(expect, NOTIFICATION_REGEXP);
        return BigDecimal.valueOf(humidityFromHex(value));
    }

    @Override
    void enableNotifications(Expect expect) throws IOException {
        expect.sendLine("char-write-cmd 0x3c 0100");
    }

    @Override
    void enable(Expect expect) throws IOException {
        expect.sendLine("char-write-cmd 0x3f 01");
    }

    @Override
    void disable(Expect expect) throws IOException {
        expect.sendLine("char-write-cmd 0x3f 00");
    }

    private double temperatureFromHex(String hexValue) {
        double result = 0.0;

        String[] hexValues = hexValue.split(" ");
        if (hexValues.length == 4) {
            int rawTemperature = Integer.parseInt(hexValues[1] + hexValues[0], 16);
            result = getAmbientTemperature(rawTemperature);
        }
        return result;
    }

    private double humidityFromHex(String hexValue) {
        double result = 0.0;

        String[] hexValues = hexValue.split(" ");
        if (hexValues.length == 4) {
            int rawHumidity = Integer.parseInt(hexValues[3] + hexValues[2], 16);
            result =  getHumidity(rawHumidity);
        }
        return result;
    }

    private float getAmbientTemperature(int temperatureRaw) {
        return -46.85f + 175.72f/65536f *(float)temperatureRaw;
    }

    private float getHumidity(int rawHumidity) {
        // bits [1..0] are status bits and need to be cleared according to the user guide,
        // but the iOS code doesn't bother. It should have minimal impact.
        rawHumidity = rawHumidity - (rawHumidity % 4);
        return (-6f) + 125f * (rawHumidity / 65535f);
    }
}
