package nl.wiegman.smartmeter;

// Fast byte-wise CRC16 calculation.
// Author: Christian d'Heureuse, www.source-code.biz
public class Crc16_1 {

    // Generator polynom codes:
    public static final int stdPoly = 0xA001; // standard CRC-16 x16+x15+x2+1 (CRC-16-IBM)
    public static final int stdRPoly = 0xC002; // standard reverse x16+x14+x+1 (CRC-16-IBM)
    public static final int ccittPoly = 0x8408; // CCITT/SDLC/HDLC X16+X12+X5+1 (CRC-16-CCITT)
    // The initial CRC value is usually 0xFFFF and the result is complemented.
    public static final int ccittRPoly = 0x8810; // CCITT reverse X16+X11+X4+1   (CRC-16-CCITT)
    public static final int lrcPoly = 0x8000; // LRCC-16 X16+1

    private short[] crcTable;

    public Crc16_1(int polynom) {
        crcTable = genCrc16Table(polynom);
    }

    public int calculate(byte[] data, int initialCrcValue) {
        int crc = initialCrcValue;
        for (int p = 0; p < data.length; p++) {
            crc = (crc >> 8) ^ (crcTable[(crc & 0xFF) ^ (data[p] & 0xFF)] & 0xFFFF);
        }
        return crc;
    }

    private static short[] genCrc16Table(int polynom) {
        short[] table = new short[256];
        for (int x = 0; x < 256; x++) {
            int w = x;
            for (int i = 0; i < 8; i++) {
                if ((w & 1) != 0) {
                    w = (w >> 1) ^ polynom;
                } else {
                    w = w >> 1;
                }
            }
            table[x] = (short) w;
        }
        return table;
    }

}
