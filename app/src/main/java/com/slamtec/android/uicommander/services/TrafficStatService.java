package com.slamtec.android.uicommander.services;

import android.app.Service;
import android.content.Intent;
import android.net.TrafficStats;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.slamtec.android.uicommander.events.TrafficStatEvent;
import com.slamtec.android.uicommander.utils.LogUtil;

import org.greenrobot.eventbus.EventBus;


/**
 * Created by Alan on 12/16/15.
 */
public class TrafficStatService extends Service {
    private final static String TAG = "TrafficStatService";

    private long traffic;

    // 2 seconds
    private final static float TRAFFIC_INTERVAL = 2f;

    private final static float KILOBYTE = 1024f;

    private static int uid;

    private Thread workingThread;


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        uid = android.os.Process.myUid();
        traffic = TrafficStats.getUidTxBytes(uid) + TrafficStats.getUidRxBytes(uid);

        workingThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    long current = TrafficStats.getUidTxBytes(uid) + TrafficStats.getUidRxBytes(uid);
                    long diff = current - traffic;

                    traffic = current;

                    float speed = diff / TRAFFIC_INTERVAL / KILOBYTE;

                    EventBus.getDefault().post(new TrafficStatEvent(String.format("%.2f KB/s", speed)));

                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        LogUtil.e(TAG, "TrafficStat workingThread interrupted");
                        workingThread = null;
                    }
                }
            }
        });
        workingThread.start();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (workingThread != null && !workingThread.isInterrupted()) {
            workingThread.interrupt();
            workingThread = null;
        }
    }
}
