package nl.homesensors.smartmeter;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@ToString
@Getter
@Setter
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
    private BigDecimal voltageL1;
    private BigDecimal voltageL2;
    private BigDecimal voltageL3;
    private Integer instantaneousCurrentL1;
    private Integer instantaneousCurrentL2;
    private Integer instantaneousCurrentL3;
    private BigDecimal instantaneousActivePowerL1;
    private BigDecimal instantaneousActivePowerL2;
    private BigDecimal instantaneousPowerDeliveredL1;
    private BigDecimal instantaneousPowerDeliveredL2;
    private BigDecimal instantaneousPowerDeliveredL3;
    private BigDecimal instantaneousPowerReceivedL1;
    private BigDecimal instantaneousPowerReceivedL2;
    private BigDecimal instantaneousPowerReceivedL3;

    private String equipmentIdentifierGas;
    private BigDecimal lastHourlyValueOfTemperatureConvertedGasDeliveredToClient;
    private LocalDateTime lastHourlyValueOfTemperatureConvertedGasDeliveredToClientCaptureTimestamp;
    private SmartMeterMessage.DstIndicator lastHourlyValueOfTemperatureConvertedGasDeliveredToClientCaptureTimestampDstIndicator;

    public void addLongPowerFailureLogItem(final LongPowerFailureLogItem longLongPowerFailureLogItem) {
        this.longPowerFailureLog.add(longLongPowerFailureLogItem);
    }
}
