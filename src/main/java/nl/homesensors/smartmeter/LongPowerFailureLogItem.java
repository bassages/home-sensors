package nl.homesensors.smartmeter;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class LongPowerFailureLogItem {
    private LocalDateTime timestampOfEndOfFailure;
    private long failureDurationInSeconds;
    private SmartMeterMessage.DstIndicator timestampOfEndOfFailureDstIndicator;
}
