package com.slamtec.android.uicommander;

import android.app.Application;

import com.slamtec.android.uicommander.agent.RPSlamwareSdpAgent;

/**
 * Created by Alan on 9/30/15.
 */
public class BaseApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        agent = new RPSlamwareSdpAgent();
    }

    public RPSlamwareSdpAgent getAgent() {
        return agent;
    }

    private RPSlamwareSdpAgent agent;
}
