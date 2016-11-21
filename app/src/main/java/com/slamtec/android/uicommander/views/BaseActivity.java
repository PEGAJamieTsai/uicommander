package com.slamtec.android.uicommander.views;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.slamtec.android.uicommander.BaseApplication;
import com.slamtec.android.uicommander.agent.RPSlamwareSdpAgent;

/**
 * Created by Alan on 9/30/15.
 */
public class BaseActivity extends AppCompatActivity {

    protected static RPSlamwareSdpAgent agent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        agent = ((BaseApplication) getApplication()).getAgent();
    }
}
