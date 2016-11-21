package com.slamtec.android.uicommander.services;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.slamtec.android.uicommander.events.WifiScanEvent;

import org.greenrobot.eventbus.EventBus;


/**
 * Created by Alan on 1/8/16.
 */
public class WifiService extends Service {
    private final LocalBinder mBinder = new LocalBinder();

    private WifiManager wifiManager;

    private BroadcastReceiver wifiReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            EventBus.getDefault().post(new WifiScanEvent(wifiManager.getScanResults()));
        }
    };

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        registerReceiver(wifiReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
    }

    @Override
    public void onDestroy() {
        unregisterReceiver(wifiReceiver);

        super.onDestroy();
    }

    // return true and start scan wifi if wifi enabled. return false if wifi disabled.
    public boolean startScan() {
        if (wifiManager.isWifiEnabled()) {
            wifiManager.startScan();
            return true;
        } else {
            return false;
        }
    }

    public class LocalBinder extends Binder {
        public WifiService getService() {
            return WifiService.this;
        }
    }
}
