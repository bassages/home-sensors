package nl.wiegman.homesensors.smartmeter;

public enum DeviceType {
    GAS("003"),
    ;

    private String deviceTypeIdentifier;

    private DeviceType(String deviceTypeIdentifier) {
        this.deviceTypeIdentifier = deviceTypeIdentifier;
    }

    public String getDeviceTypeIdentifier() {
        return deviceTypeIdentifier;
    }
}
