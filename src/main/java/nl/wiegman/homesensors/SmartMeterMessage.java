package nl.wiegman.homesensors;

import java.math.BigDecimal;
import java.util.Date;

public class SmartMeterMessage {

    public enum DstIndicator {
        SUMMER,
        WINTER
    }

    private String header;
    private String versionInformationForP1Output;
    private Date timestamp;
    private SmartMeterMessage.DstIndicator timestampDstIndicator;
    private String equipmentIdentifierElectricity;
    private BigDecimal meterReadingElectricityDeliveredToClientTariff1;
    private BigDecimal meterReadingElectricityDeliveredToClientTariff2;
    private BigDecimal meterReadingElectricityDeliveredByClientTariff1;
    private BigDecimal meterReadingElectricityDeliveredByClientTariff2;
    private String tariffIndicatorElectricity;
    private BigDecimal actualElectricityPowerDelivered;
    private BigDecimal actualElectricityPowerRecieved;
    private int numberOfPowerFailuresInAnyPhase;
    private int numberOfLongPowerFailuresInAnyPhase;
    private int numberOfVoltageSagsInPhaseL1;
    private int numberOfVoltageSagsInPhaseL2;
    private String textMessageCodes;
    private String textMessage;
    private int instantaneousCurrentL1;
    private BigDecimal instantaneousActivePowerL1;
    private BigDecimal instantaneousActivePowerL2;
    private String deviceType;
    private String equipmentIdentifierGas;
    private BigDecimal lastHourlyValueOfTemperatureConvertedGasDeliveredToClient;
    private Date lastHourlyValueOfTemperatureConvertedGasDeliveredToClientCaptureTimestamp;
    private SmartMeterMessage.DstIndicator lastHourlyValueOfTemperatureConvertedGasDeliveredToClientCaptureTimestampDstIndicator;

    public String getHeader() {
        return header;
    }

    public void setHeader(String header) {
        this.header = header;
    }

    public String getVersionInformationForP1Output() {
        return versionInformationForP1Output;
    }

    public void setVersionInformationForP1Output(String versionInformationForP1Output) {
        this.versionInformationForP1Output = versionInformationForP1Output;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public SmartMeterMessage.DstIndicator getTimestampDstIndicator() {
        return timestampDstIndicator;
    }

    public void setTimestampDstIndicator(SmartMeterMessage.DstIndicator timestampDstIndicator) {
        this.timestampDstIndicator = timestampDstIndicator;
    }

    public String getEquipmentIdentifierElectricity() {
        return equipmentIdentifierElectricity;
    }

    public void setEquipmentIdentifierElectricity(String equipmentIdentifierElectricity) {
        this.equipmentIdentifierElectricity = equipmentIdentifierElectricity;
    }

    public BigDecimal getMeterReadingElectricityDeliveredToClientTariff1() {
        return meterReadingElectricityDeliveredToClientTariff1;
    }

    public void setMeterReadingElectricityDeliveredToClientTariff1(BigDecimal meterReadingElectricityDeliveredToClientTariff1) {
        this.meterReadingElectricityDeliveredToClientTariff1 = meterReadingElectricityDeliveredToClientTariff1;
    }

    public BigDecimal getMeterReadingElectricityDeliveredToClientTariff2() {
        return meterReadingElectricityDeliveredToClientTariff2;
    }

    public void setMeterReadingElectricityDeliveredToClientTariff2(BigDecimal meterReadingElectricityDeliveredToClientTariff2) {
        this.meterReadingElectricityDeliveredToClientTariff2 = meterReadingElectricityDeliveredToClientTariff2;
    }

    public BigDecimal getMeterReadingElectricityDeliveredByClientTariff1() {
        return meterReadingElectricityDeliveredByClientTariff1;
    }

    public void setMeterReadingElectricityDeliveredByClientTariff1(BigDecimal meterReadingElectricityDeliveredByClientTariff1) {
        this.meterReadingElectricityDeliveredByClientTariff1 = meterReadingElectricityDeliveredByClientTariff1;
    }

    public BigDecimal getMeterReadingElectricityDeliveredByClientTariff2() {
        return meterReadingElectricityDeliveredByClientTariff2;
    }

    public void setMeterReadingElectricityDeliveredByClientTariff2(BigDecimal meterReadingElectricityDeliveredByClientTariff2) {
        this.meterReadingElectricityDeliveredByClientTariff2 = meterReadingElectricityDeliveredByClientTariff2;
    }

    public String getTariffIndicatorElectricity() {
        return tariffIndicatorElectricity;
    }

    public void setTariffIndicatorElectricity(String tariffIndicatorElectricity) {
        this.tariffIndicatorElectricity = tariffIndicatorElectricity;
    }

    public BigDecimal getActualElectricityPowerDelivered() {
        return actualElectricityPowerDelivered;
    }

    public void setActualElectricityPowerDelivered(BigDecimal actualElectricityPowerDelivered) {
        this.actualElectricityPowerDelivered = actualElectricityPowerDelivered;
    }

    public BigDecimal getActualElectricityPowerRecieved() {
        return actualElectricityPowerRecieved;
    }

    public void setActualElectricityPowerRecieved(BigDecimal actualElectricityPowerRecieved) {
        this.actualElectricityPowerRecieved = actualElectricityPowerRecieved;
    }

    public int getNumberOfPowerFailuresInAnyPhase() {
        return numberOfPowerFailuresInAnyPhase;
    }

    public void setNumberOfPowerFailuresInAnyPhase(int numberOfPowerFailuresInAnyPhase) {
        this.numberOfPowerFailuresInAnyPhase = numberOfPowerFailuresInAnyPhase;
    }

    public int getNumberOfLongPowerFailuresInAnyPhase() {
        return numberOfLongPowerFailuresInAnyPhase;
    }

    public void setNumberOfLongPowerFailuresInAnyPhase(int numberOfLongPowerFailuresInAnyPhase) {
        this.numberOfLongPowerFailuresInAnyPhase = numberOfLongPowerFailuresInAnyPhase;
    }

    public int getNumberOfVoltageSagsInPhaseL1() {
        return numberOfVoltageSagsInPhaseL1;
    }

    public void setNumberOfVoltageSagsInPhaseL1(int numberOfVoltageSagsInPhaseL1) {
        this.numberOfVoltageSagsInPhaseL1 = numberOfVoltageSagsInPhaseL1;
    }

    public int getNumberOfVoltageSagsInPhaseL2() {
        return numberOfVoltageSagsInPhaseL2;
    }

    public void setNumberOfVoltageSagsInPhaseL2(int numberOfVoltageSagsInPhaseL2) {
        this.numberOfVoltageSagsInPhaseL2 = numberOfVoltageSagsInPhaseL2;
    }

    public String getTextMessageCodes() {
        return textMessageCodes;
    }

    public void setTextMessageCodes(String textMessageCodes) {
        this.textMessageCodes = textMessageCodes;
    }

    public String getTextMessage() {
        return textMessage;
    }

    public void setTextMessage(String textMessage) {
        this.textMessage = textMessage;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    public BigDecimal getLastHourlyValueOfTemperatureConvertedGasDeliveredToClient() {
        return lastHourlyValueOfTemperatureConvertedGasDeliveredToClient;
    }

    public void setLastHourlyValueOfTemperatureConvertedGasDeliveredToClient(BigDecimal lastHourlyValueOfTemperatureConvertedGasDeliveredToClient) {
        this.lastHourlyValueOfTemperatureConvertedGasDeliveredToClient = lastHourlyValueOfTemperatureConvertedGasDeliveredToClient;
    }

    public int getInstantaneousCurrentL1() {
        return instantaneousCurrentL1;
    }

    public void setInstantaneousCurrentL1(int instantaneousCurrentL1) {
        this.instantaneousCurrentL1 = instantaneousCurrentL1;
    }

    public BigDecimal getInstantaneousActivePowerL1() {
        return instantaneousActivePowerL1;
    }

    public void setInstantaneousActivePowerL1(BigDecimal instantaneousActivePowerL1) {
        this.instantaneousActivePowerL1 = instantaneousActivePowerL1;
    }

    public BigDecimal getInstantaneousActivePowerL2() {
        return instantaneousActivePowerL2;
    }

    public void setInstantaneousActivePowerL2(BigDecimal instantaneousActivePowerL2) {
        this.instantaneousActivePowerL2 = instantaneousActivePowerL2;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public String getEquipmentIdentifierGas() {
        return equipmentIdentifierGas;
    }

    public void setEquipmentIdentifierGas(String equipmentIdentifierGas) {
        this.equipmentIdentifierGas = equipmentIdentifierGas;
    }

    public Date getLastHourlyValueOfTemperatureConvertedGasDeliveredToClientCaptureTimestamp() {
        return lastHourlyValueOfTemperatureConvertedGasDeliveredToClientCaptureTimestamp;
    }

    public void setLastHourlyValueOfTemperatureConvertedGasDeliveredToClientCaptureTimestamp(
            Date lastHourlyValueOfTemperatureConvertedGasDeliveredToClientCaptureTimestamp) {
        this.lastHourlyValueOfTemperatureConvertedGasDeliveredToClientCaptureTimestamp = lastHourlyValueOfTemperatureConvertedGasDeliveredToClientCaptureTimestamp;
    }

    public SmartMeterMessage.DstIndicator getLastHourlyValueOfTemperatureConvertedGasDeliveredToClientCaptureTimestampDstIndicator() {
        return lastHourlyValueOfTemperatureConvertedGasDeliveredToClientCaptureTimestampDstIndicator;
    }

    public void setLastHourlyValueOfTemperatureConvertedGasDeliveredToClientCaptureTimestampDstIndicator(
            SmartMeterMessage.DstIndicator lastHourlyValueOfTemperatureConvertedGasDeliveredToClientCaptureTimestampDstIndicator) {
        this.lastHourlyValueOfTemperatureConvertedGasDeliveredToClientCaptureTimestampDstIndicator = lastHourlyValueOfTemperatureConvertedGasDeliveredToClientCaptureTimestampDstIndicator;
    }
}
