package nl.homesensors.smartmeter;

public enum DeviceType {
    GAS("003"),
    ;

    private final String deviceTypeIdentifier;

    DeviceType(String deviceTypeIdentifier) {
        this.deviceTypeIdentifier = deviceTypeIdentifier;
    }

    public String getDeviceTypeIdentifier() {
        return deviceTypeIdentifier;
    }
}
