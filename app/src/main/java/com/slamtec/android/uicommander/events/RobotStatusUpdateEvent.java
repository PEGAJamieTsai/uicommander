package com.slamtec.android.uicommander.events;

/**
 * Created by Alan on 10/21/15.
 */
public class RobotStatusUpdateEvent {
    private int batteryPercentage;
    private boolean charging;
    private int localizationQuality;

    public RobotStatusUpdateEvent(int batteryPercentage, boolean charging, int localizationQuality) {
        this.batteryPercentage = batteryPercentage;
        this.charging = charging;
        this.localizationQuality = localizationQuality;
    }

    public int getBatteryPercentage() {
        return batteryPercentage;
    }

    public boolean isCharging() {
        return charging;
    }

    public int getLocalizationQuality() {
        return localizationQuality;
    }
}
