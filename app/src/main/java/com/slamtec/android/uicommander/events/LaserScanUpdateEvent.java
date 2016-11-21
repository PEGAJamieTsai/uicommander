package com.slamtec.android.uicommander.events;

import com.slamtec.slamware.robot.LaserScan;

/**
 * Created by Alan on 10/21/15.
 */
public class LaserScanUpdateEvent {
    private LaserScan laserScan;

    public LaserScanUpdateEvent(LaserScan laserScan) {
        this.laserScan = laserScan;
    }

    public LaserScan getLaserScan() {
        return laserScan;
    }
}
