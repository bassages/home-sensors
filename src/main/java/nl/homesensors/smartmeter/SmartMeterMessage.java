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
    private BigDecimal actualElectricityPowerDeliveredKw;
    private BigDecimal actualElectricityPowerReceivedKw;
    private Integer numberOfPowerFailuresInAnyPhase;
    private Integer numberOfLongPowerFailuresInAnyPhase;
    private final List<LongPowerFailureLogItem> longPowerFailureLog = new ArrayList<>();
    private Integer numberOfVoltageSagsInPhaseL1;
    private Integer numberOfVoltageSagsInPhaseL2;
    private Integer numberOfVoltageSagsInPhaseL3;
    private String textMessageCodes;
    private String textMessage;

    // Spanning per fase. Unit: Volt
    private BigDecimal voltageL1;
    private BigDecimal voltageL2;
    private BigDecimal voltageL3;

    // Stroomsterkte. Unit: Ampere
    private Integer instantaneousCurrentL1Ampere;
    private Integer instantaneousCurrentL2Ampere;
    private Integer instantaneousCurrentL3Ampere;

    // Actueel verbruik per fase. Unit: kW
    private BigDecimal instantaneousPowerDeliveredL1Kw;
    private BigDecimal instantaneousPowerDeliveredL2Kw;
    private BigDecimal instantaneousPowerDeliveredL3Kw;

    // Actuele teruglevering per fase. Unit: kW
    private BigDecimal instantaneousPowerReceivedL1Kw;
    private BigDecimal instantaneousPowerReceivedL2Kw;
    private BigDecimal instantaneousPowerReceivedL3Kw;

    private String equipmentIdentifierGas;
    private BigDecimal lastHourlyValueOfTemperatureConvertedGasDeliveredToClient;
    private LocalDateTime lastHourlyValueOfTemperatureConvertedGasDeliveredToClientCaptureTimestamp;
    private SmartMeterMessage.DstIndicator lastHourlyValueOfTemperatureConvertedGasDeliveredToClientCaptureTimestampDstIndicator;

    public void addLongPowerFailureLogItem(final LongPowerFailureLogItem longLongPowerFailureLogItem) {
        this.longPowerFailureLog.add(longLongPowerFailureLogItem);
    }
}
