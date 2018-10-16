package nl.homesensors.smartmeter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SmartMeterMessage {

    public enum DstIndicator {
        SUMMER,
        WINTER
    }

    private String header;
    private String versionInformationForP1Output;
    private LocalDateTime timestamp;
    private SmartMeterMessage.DstIndicator timestampDstIndicator;
    private String equipmentIdentifierElectricity;
    private BigDecimal meterReadingElectricityDeliveredToClientTariff1;
    private BigDecimal meterReadingElectricityDeliveredToClientTariff2;
    private BigDecimal meterReadingElectricityDeliveredByClientTariff1;
    private BigDecimal meterReadingElectricityDeliveredByClientTariff2;
    private Integer tariffIndicatorElectricity;
    private BigDecimal actualElectricityPowerDelivered;
    private BigDecimal actualElectricityPowerRecieved;
    private Integer numberOfPowerFailuresInAnyPhase;
    private Integer numberOfLongPowerFailuresInAnyPhase;
    private List<LongPowerFailureLogItem> longPowerFailureLog = new ArrayList<>();
    private Integer numberOfVoltageSagsInPhaseL1;
    private Integer numberOfVoltageSagsInPhaseL2;
    private String textMessageCodes;
    private String textMessage;
    private Integer instantaneousCurrentL1;
    private BigDecimal instantaneousActivePowerL1;
    private BigDecimal instantaneousActivePowerL2;

    private String equipmentIdentifierGas;
    private BigDecimal lastHourlyValueOfTemperatureConvertedGasDeliveredToClient;
    private LocalDateTime lastHourlyValueOfTemperatureConvertedGasDeliveredToClientCaptureTimestamp;
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

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
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

    public Integer getTariffIndicatorElectricity() {
        return tariffIndicatorElectricity;
    }

    public void setTariffIndicatorElectricity(Integer tariffIndicatorElectricity) {
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

    public Integer getNumberOfPowerFailuresInAnyPhase() {
        return numberOfPowerFailuresInAnyPhase;
    }

    public void setNumberOfPowerFailuresInAnyPhase(Integer numberOfPowerFailuresInAnyPhase) {
        this.numberOfPowerFailuresInAnyPhase = numberOfPowerFailuresInAnyPhase;
    }

    public Integer getNumberOfLongPowerFailuresInAnyPhase() {
        return numberOfLongPowerFailuresInAnyPhase;
    }

    public void setNumberOfLongPowerFailuresInAnyPhase(Integer numberOfLongPowerFailuresInAnyPhase) {
        this.numberOfLongPowerFailuresInAnyPhase = numberOfLongPowerFailuresInAnyPhase;
    }

    public Integer getNumberOfVoltageSagsInPhaseL1() {
        return numberOfVoltageSagsInPhaseL1;
    }

    public void setNumberOfVoltageSagsInPhaseL1(Integer numberOfVoltageSagsInPhaseL1) {
        this.numberOfVoltageSagsInPhaseL1 = numberOfVoltageSagsInPhaseL1;
    }

    public Integer getNumberOfVoltageSagsInPhaseL2() {
        return numberOfVoltageSagsInPhaseL2;
    }

    public void setNumberOfVoltageSagsInPhaseL2(Integer numberOfVoltageSagsInPhaseL2) {
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

    public BigDecimal getLastHourlyValueOfTemperatureConvertedGasDeliveredToClient() {
        return lastHourlyValueOfTemperatureConvertedGasDeliveredToClient;
    }

    public void setLastHourlyValueOfTemperatureConvertedGasDeliveredToClient(BigDecimal lastHourlyValueOfTemperatureConvertedGasDeliveredToClient) {
        this.lastHourlyValueOfTemperatureConvertedGasDeliveredToClient = lastHourlyValueOfTemperatureConvertedGasDeliveredToClient;
    }

    public Integer getInstantaneousCurrentL1() {
        return instantaneousCurrentL1;
    }

    public void setInstantaneousCurrentL1(Integer instantaneousCurrentL1) {
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

    public String getEquipmentIdentifierGas() {
        return equipmentIdentifierGas;
    }

    public void setEquipmentIdentifierGas(String equipmentIdentifierGas) {
        this.equipmentIdentifierGas = equipmentIdentifierGas;
    }

    public LocalDateTime getLastHourlyValueOfTemperatureConvertedGasDeliveredToClientCaptureTimestamp() {
        return lastHourlyValueOfTemperatureConvertedGasDeliveredToClientCaptureTimestamp;
    }

    public void setLastHourlyValueOfTemperatureConvertedGasDeliveredToClientCaptureTimestamp(
            LocalDateTime lastHourlyValueOfTemperatureConvertedGasDeliveredToClientCaptureTimestamp) {
        this.lastHourlyValueOfTemperatureConvertedGasDeliveredToClientCaptureTimestamp = lastHourlyValueOfTemperatureConvertedGasDeliveredToClientCaptureTimestamp;
    }

    public SmartMeterMessage.DstIndicator getLastHourlyValueOfTemperatureConvertedGasDeliveredToClientCaptureTimestampDstIndicator() {
        return lastHourlyValueOfTemperatureConvertedGasDeliveredToClientCaptureTimestampDstIndicator;
    }

    public void setLastHourlyValueOfTemperatureConvertedGasDeliveredToClientCaptureTimestampDstIndicator(
            SmartMeterMessage.DstIndicator lastHourlyValueOfTemperatureConvertedGasDeliveredToClientCaptureTimestampDstIndicator) {
        this.lastHourlyValueOfTemperatureConvertedGasDeliveredToClientCaptureTimestampDstIndicator = lastHourlyValueOfTemperatureConvertedGasDeliveredToClientCaptureTimestampDstIndicator;
    }

    public List<LongPowerFailureLogItem> getLongPowerFailureLog() {
        return Collections.unmodifiableList(longPowerFailureLog);
    }

    public void addLongPowerFailureLogItem(LongPowerFailureLogItem longLongPowerFailureLogItem) {
        this.longPowerFailureLog.add(longLongPowerFailureLogItem);
    }
}
