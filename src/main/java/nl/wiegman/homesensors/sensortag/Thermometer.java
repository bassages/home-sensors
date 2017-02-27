package nl.wiegman.homesensors.sensortag;

import net.sf.expectit.Expect;

import java.io.IOException;
import java.math.BigDecimal;

/**
 * <pre>
 *
 * Hardware on TI SensorTag: Texas Instruments TMP006 @ U5
 *
 * Two types of data are obtained from the IR Temperature sensor: object temperature and ambient temperature.
 *
 * --------------------------------------------------------------------------------------
 * | Type                |  UUID  |  Read/Write | Format                                |
 * |------------------------------------------------------------------------------------|
 * | <Data>              | AA01 * | Read/Notify | ObjLSB ObjMSB AmbLSB AmbMSB (4 bytes) |
 * | <Data Notification> | -      | R/W         | 2 bytes                               |
 * | <Configuration>     | AA02 * | R/W         | 1 byte                                |
 * | <Period>            | AA03 * | R/W         | 1 byte                                |
 * --------------------------------------------------------------------------------------
 *
 * When the enable command is issued, the sensor starts to perform measurements each second (average over four measurements)
 * and the data is stored in the <Data> each second as well. When the disable command is issued, the sensor is put in stand-by mode.
 *
 * To obtain data either use notifications or read the data directly.
 * The period range varies from 300 ms to 2.55 seconds. The unit is 10 ms. i.e. writing 0x32 gives 500 ms, 0x64 1 second etc. The default value is 1 second.
 *
 * For more information please refer to TI TMP006 User's Guide
 *
 * The raw data value read from this sensor are two unsigned 16 bit values, one for die temperature and one for object temperature.
 *
 * The IR Temperature sensor produces two measurements; Object (AKA target or IR) Temperature, and Ambient (AKA die) temperature.
 * Both need some conversion, and Object temperature is dependent on Ambient temperature.
 * They are stored as [ObjLSB, ObjMSB, AmbLSB, AmbMSB] (4 bytes)
 *
 * </pre>
 */
public class Thermometer extends AbstractSensor {

    public static final String NOTIFICATION_REGEXP = "Notification handle = 0x0025 value: (?!00 00 00 00)(\\w{2} \\w{2} \\w{2} \\w{2})";

    public BigDecimal getAmbientTemperature(Expect expect) throws IOException, SensortagException {
        enable(expect);
        String value = expectSuccesfulMatch(expect, NOTIFICATION_REGEXP);
        disable(expect);
        discardNotifications(expect, NOTIFICATION_REGEXP);
        return BigDecimal.valueOf(ambientTemperatureFromHex(value));
    }

    @Override
    void enableNotifications(Expect expect) throws IOException {
        expect.sendLine("char-write-cmd 0x26 0100");
    }

    @Override
    void enable(Expect expect) throws IOException {
        expect.sendLine("char-write-cmd 0x29 01");
    }

    @Override
    void disable(Expect expect) throws IOException {
        expect.sendLine("char-write-cmd 0x29 00");
    }

    private double ambientTemperatureFromHex(String hexValue) {
        double result = 0.0;
        String[] hexValues = hexValue.split(" ");
        if (hexValues.length == 4) {
            int rawAmbient = Integer.parseInt(hexValues[3] + hexValues[2], 16);
            result = getAmbientTemperature(rawAmbient);
        }
        return result;
    }

    private double getAmbientTemperature(int rawAmbient) {
        return rawAmbient / 128.0;
    }

}
