package nl.wiegman.homesensors;

import java.util.Date;

public class PowerFailureLogItem {

    private Date timestampOfEndOfFailure;
    private long failureDurationInSeconds;
    private SmartMeterMessage.DstIndicator timestampOfEndOfFailureDstIndicator;

    public Date getTimestampOfEndOfFailure() {
        return timestampOfEndOfFailure;
    }

    public void setTimestampOfEndOfFailure(Date timestampOfEndOfFailure) {
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
