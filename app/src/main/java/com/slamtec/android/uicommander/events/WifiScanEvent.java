package com.slamtec.android.uicommander.events;

import android.net.wifi.ScanResult;

import java.util.List;

/**
 * Created by Alan on 1/8/16.
 */
public class WifiScanEvent {
    private List<ScanResult> results;

    public WifiScanEvent(List<ScanResult> results) {
        this.results = results;
    }

    public List<ScanResult> getResults() {
        return results;
    }
}
