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
    private final List<LongPowerFailureLogItem> longPowerFailureLog = new ArrayList<>();
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

    void setHeader(final String header) {
        this.header = header;
    }

    public String getVersionInformationForP1Output() {
        return versionInformationForP1Output;
    }

    void setVersionInformationForP1Output(final String versionInformationForP1Output) {
        this.versionInformationForP1Output = versionInformationForP1Output;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(final LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public SmartMeterMessage.DstIndicator getTimestampDstIndicator() {
        return timestampDstIndicator;
    }

    void setTimestampDstIndicator(final SmartMeterMessage.DstIndicator timestampDstIndicator) {
        this.timestampDstIndicator = timestampDstIndicator;
    }

    public String getEquipmentIdentifierElectricity() {
        return equipmentIdentifierElectricity;
    }

    void setEquipmentIdentifierElectricity(final String equipmentIdentifierElectricity) {
        this.equipmentIdentifierElectricity = equipmentIdentifierElectricity;
    }

    public BigDecimal getMeterReadingElectricityDeliveredToClientTariff1() {
        return meterReadingElectricityDeliveredToClientTariff1;
    }

    void setMeterReadingElectricityDeliveredToClientTariff1(final BigDecimal meterReadingElectricityDeliveredToClientTariff1) {
        this.meterReadingElectricityDeliveredToClientTariff1 = meterReadingElectricityDeliveredToClientTariff1;
    }

    public BigDecimal getMeterReadingElectricityDeliveredToClientTariff2() {
        return meterReadingElectricityDeliveredToClientTariff2;
    }

    void setMeterReadingElectricityDeliveredToClientTariff2(final BigDecimal meterReadingElectricityDeliveredToClientTariff2) {
        this.meterReadingElectricityDeliveredToClientTariff2 = meterReadingElectricityDeliveredToClientTariff2;
    }

    public BigDecimal getMeterReadingElectricityDeliveredByClientTariff1() {
        return meterReadingElectricityDeliveredByClientTariff1;
    }

    void setMeterReadingElectricityDeliveredByClientTariff1(final BigDecimal meterReadingElectricityDeliveredByClientTariff1) {
        this.meterReadingElectricityDeliveredByClientTariff1 = meterReadingElectricityDeliveredByClientTariff1;
    }

    public BigDecimal getMeterReadingElectricityDeliveredByClientTariff2() {
        return meterReadingElectricityDeliveredByClientTariff2;
    }

    void setMeterReadingElectricityDeliveredByClientTariff2(final BigDecimal meterReadingElectricityDeliveredByClientTariff2) {
        this.meterReadingElectricityDeliveredByClientTariff2 = meterReadingElectricityDeliveredByClientTariff2;
    }

    public Integer getTariffIndicatorElectricity() {
        return tariffIndicatorElectricity;
    }

    void setTariffIndicatorElectricity(final Integer tariffIndicatorElectricity) {
        this.tariffIndicatorElectricity = tariffIndicatorElectricity;
    }

    public BigDecimal getActualElectricityPowerDelivered() {
        return actualElectricityPowerDelivered;
    }

    public void setActualElectricityPowerDelivered(final BigDecimal actualElectricityPowerDelivered) {
        this.actualElectricityPowerDelivered = actualElectricityPowerDelivered;
    }

    public BigDecimal getActualElectricityPowerRecieved() {
        return actualElectricityPowerRecieved;
    }

    void setActualElectricityPowerRecieved(final BigDecimal actualElectricityPowerRecieved) {
        this.actualElectricityPowerRecieved = actualElectricityPowerRecieved;
    }

    public Integer getNumberOfPowerFailuresInAnyPhase() {
        return numberOfPowerFailuresInAnyPhase;
    }

    void setNumberOfPowerFailuresInAnyPhase(final Integer numberOfPowerFailuresInAnyPhase) {
        this.numberOfPowerFailuresInAnyPhase = numberOfPowerFailuresInAnyPhase;
    }

    public Integer getNumberOfLongPowerFailuresInAnyPhase() {
        return numberOfLongPowerFailuresInAnyPhase;
    }

    void setNumberOfLongPowerFailuresInAnyPhase(final Integer numberOfLongPowerFailuresInAnyPhase) {
        this.numberOfLongPowerFailuresInAnyPhase = numberOfLongPowerFailuresInAnyPhase;
    }

    public Integer getNumberOfVoltageSagsInPhaseL1() {
        return numberOfVoltageSagsInPhaseL1;
    }

    void setNumberOfVoltageSagsInPhaseL1(final Integer numberOfVoltageSagsInPhaseL1) {
        this.numberOfVoltageSagsInPhaseL1 = numberOfVoltageSagsInPhaseL1;
    }

    public Integer getNumberOfVoltageSagsInPhaseL2() {
        return numberOfVoltageSagsInPhaseL2;
    }

    void setNumberOfVoltageSagsInPhaseL2(final Integer numberOfVoltageSagsInPhaseL2) {
        this.numberOfVoltageSagsInPhaseL2 = numberOfVoltageSagsInPhaseL2;
    }

    public String getTextMessageCodes() {
        return textMessageCodes;
    }

    void setTextMessageCodes(final String textMessageCodes) {
        this.textMessageCodes = textMessageCodes;
    }

    public String getTextMessage() {
        return textMessage;
    }

    void setTextMessage(final String textMessage) {
        this.textMessage = textMessage;
    }

    public BigDecimal getLastHourlyValueOfTemperatureConvertedGasDeliveredToClient() {
        return lastHourlyValueOfTemperatureConvertedGasDeliveredToClient;
    }

    void setLastHourlyValueOfTemperatureConvertedGasDeliveredToClient(final BigDecimal lastHourlyValueOfTemperatureConvertedGasDeliveredToClient) {
        this.lastHourlyValueOfTemperatureConvertedGasDeliveredToClient = lastHourlyValueOfTemperatureConvertedGasDeliveredToClient;
    }

    public Integer getInstantaneousCurrentL1() {
        return instantaneousCurrentL1;
    }

    void setInstantaneousCurrentL1(final Integer instantaneousCurrentL1) {
        this.instantaneousCurrentL1 = instantaneousCurrentL1;
    }

    public BigDecimal getInstantaneousActivePowerL1() {
        return instantaneousActivePowerL1;
    }

    void setInstantaneousActivePowerL1(final BigDecimal instantaneousActivePowerL1) {
        this.instantaneousActivePowerL1 = instantaneousActivePowerL1;
    }

    public BigDecimal getInstantaneousActivePowerL2() {
        return instantaneousActivePowerL2;
    }

    void setInstantaneousActivePowerL2(final BigDecimal instantaneousActivePowerL2) {
        this.instantaneousActivePowerL2 = instantaneousActivePowerL2;
    }

    public String getEquipmentIdentifierGas() {
        return equipmentIdentifierGas;
    }

    void setEquipmentIdentifierGas(final String equipmentIdentifierGas) {
        this.equipmentIdentifierGas = equipmentIdentifierGas;
    }

    public LocalDateTime getLastHourlyValueOfTemperatureConvertedGasDeliveredToClientCaptureTimestamp() {
        return lastHourlyValueOfTemperatureConvertedGasDeliveredToClientCaptureTimestamp;
    }

    void setLastHourlyValueOfTemperatureConvertedGasDeliveredToClientCaptureTimestamp(final LocalDateTime lastHourlyValueOfTemperatureConvertedGasDeliveredToClientCaptureTimestamp) {
        this.lastHourlyValueOfTemperatureConvertedGasDeliveredToClientCaptureTimestamp = lastHourlyValueOfTemperatureConvertedGasDeliveredToClientCaptureTimestamp;
    }

    public SmartMeterMessage.DstIndicator getLastHourlyValueOfTemperatureConvertedGasDeliveredToClientCaptureTimestampDstIndicator() {
        return lastHourlyValueOfTemperatureConvertedGasDeliveredToClientCaptureTimestampDstIndicator;
    }

    void setLastHourlyValueOfTemperatureConvertedGasDeliveredToClientCaptureTimestampDstIndicator(final SmartMeterMessage.DstIndicator lastHourlyValueOfTemperatureConvertedGasDeliveredToClientCaptureTimestampDstIndicator) {
        this.lastHourlyValueOfTemperatureConvertedGasDeliveredToClientCaptureTimestampDstIndicator = lastHourlyValueOfTemperatureConvertedGasDeliveredToClientCaptureTimestampDstIndicator;
    }

    public List<LongPowerFailureLogItem> getLongPowerFailureLog() {
        return Collections.unmodifiableList(longPowerFailureLog);
    }

    public void addLongPowerFailureLogItem(final LongPowerFailureLogItem longLongPowerFailureLogItem) {
        this.longPowerFailureLog.add(longLongPowerFailureLogItem);
    }
}
