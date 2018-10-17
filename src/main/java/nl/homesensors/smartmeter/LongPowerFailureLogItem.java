package nl.homesensors.smartmeter;

import java.time.LocalDateTime;

public class LongPowerFailureLogItem {

    private LocalDateTime timestampOfEndOfFailure;
    private long failureDurationInSeconds;
    private SmartMeterMessage.DstIndicator timestampOfEndOfFailureDstIndicator;

    public LocalDateTime getTimestampOfEndOfFailure() {
        return timestampOfEndOfFailure;
    }

    public void setTimestampOfEndOfFailure(final LocalDateTime timestampOfEndOfFailure) {
        this.timestampOfEndOfFailure = timestampOfEndOfFailure;
    }

    public long getFailureDurationInSeconds() {
        return failureDurationInSeconds;
    }

    public void setFailureDurationInSeconds(final long failureDurationInSeconds) {
        this.failureDurationInSeconds = failureDurationInSeconds;
    }

    public SmartMeterMessage.DstIndicator getTimestampOfEndOfFailureDstIndicator() {
        return timestampOfEndOfFailureDstIndicator;
    }

    public void setTimestampOfEndOfFailureDstIndicator(final SmartMeterMessage.DstIndicator timestampOfEndOfFailureDstIndicator) {
        this.timestampOfEndOfFailureDstIndicator = timestampOfEndOfFailureDstIndicator;
    }

}
