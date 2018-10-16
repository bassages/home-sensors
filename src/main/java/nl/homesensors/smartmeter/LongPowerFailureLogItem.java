package nl.homesensors.smartmeter;

import java.time.LocalDateTime;

public class LongPowerFailureLogItem {

    private LocalDateTime timestampOfEndOfFailure;
    private long failureDurationInSeconds;
    private SmartMeterMessage.DstIndicator timestampOfEndOfFailureDstIndicator;

    public LocalDateTime getTimestampOfEndOfFailure() {
        return timestampOfEndOfFailure;
    }

    public void setTimestampOfEndOfFailure(LocalDateTime timestampOfEndOfFailure) {
        this.timestampOfEndOfFailure = timestampOfEndOfFailure;
    }

    public long getFailureDurationInSeconds() {
        return failureDurationInSeconds;
    }

    public void setFailureDurationInSeconds(long failureDurationInSeconds) {
        this.failureDurationInSeconds = failureDurationInSeconds;
    }

    public SmartMeterMessage.DstIndicator getTimestampOfEndOfFailureDstIndicator() {
        return timestampOfEndOfFailureDstIndicator;
    }

    public void setTimestampOfEndOfFailureDstIndicator(SmartMeterMessage.DstIndicator timestampOfEndOfFailureDstIndicator) {
        this.timestampOfEndOfFailureDstIndicator = timestampOfEndOfFailureDstIndicator;
    }

}
