package nl.homesensors.smartmeter;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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
    private Integer instantaneousCurrentL1;
    private BigDecimal instantaneousActivePowerL1;
    private BigDecimal instantaneousActivePowerL2;

    private String equipmentIdentifierGas;
    private BigDecimal lastHourlyValueOfTemperatureConvertedGasDeliveredToClient;
    private LocalDateTime lastHourlyValueOfTemperatureConvertedGasDeliveredToClientCaptureTimestamp;
    private SmartMeterMessage.DstIndicator lastHourlyValueOfTemperatureConvertedGasDeliveredToClientCaptureTimestampDstIndicator;

    public void addLongPowerFailureLogItem(final LongPowerFailureLogItem longLongPowerFailureLogItem) {
        this.longPowerFailureLog.add(longLongPowerFailureLogItem);
    }
}
