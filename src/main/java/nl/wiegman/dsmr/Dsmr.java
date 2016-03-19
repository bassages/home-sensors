package nl.wiegman.dsmr;

import sun.misc.CRC16;

public class Dsmr {

    private String message;

    public Dsmr(String message) throws InvalidChecksumException {
        this.message = message;

//        verifyChecksum();
    }

    private void verifyChecksum() throws InvalidChecksumException {

        int checksumFromMessage = calculateChecksum();

        if (checksumFromMessage != 1) {
            throw new InvalidChecksumException();
        }
    }

    private int calculateChecksum() {
        CRC16 crc16 = new CRC16();

        for (byte b : message.getBytes()) {
            crc16.update(b);
        }
        return crc16.value;
    }

    public static class InvalidChecksumException extends Exception { }
}
