package com.slamtec.android.uicommander.events;

/**
 * Created by Alan on 12/16/15.
 */
public class TrafficStatEvent {
    private String traffic;

    public TrafficStatEvent(String traffic) {
        this.traffic = traffic;
    }

    public String getTraffic() {
        return traffic;
    }
}
