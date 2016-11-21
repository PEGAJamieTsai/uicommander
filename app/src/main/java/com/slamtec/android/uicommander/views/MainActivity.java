package com.slamtec.android.uicommander.views;

import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.slamtec.android.uicommander.R;
import com.slamtec.android.uicommander.agent.RPSlamwareSdpAgent;
import com.slamtec.android.uicommander.events.ConnectedEvent;
import com.slamtec.android.uicommander.events.ConnectionFailedEvent;
import com.slamtec.android.uicommander.events.ConnectionLostEvent;
import com.slamtec.android.uicommander.utils.LogUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class MainActivity extends BaseActivity
        implements ConnectFragment.IConnectFragmentInteractionListener {
    private final static String TAG = "MainActivity";

    private static final String STATE_IS_ROTATE = "isRotate";
    private static final String IP_ADDRESS = "ipAddress";

    private String ipAddress;

    private ProgressDialog connectingDialog;

    private AlertDialog connectionLostDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState != null && savedInstanceState.getBoolean(STATE_IS_ROTATE)) {
            ipAddress = savedInstanceState.getString(IP_ADDRESS);
            showConnectFragment();
        } else {
            showCoverFragment();
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    showConnectFragment();
                }
            },1000*2);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(STATE_IS_ROTATE, true);
        outState.putString(IP_ADDRESS, ipAddress);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        EventBus.getDefault().unregister(this);
        hideConnectingDialog();
        super.onStop();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void connectDevice(String addr, int port) {
        if (agent == null) {
            agent = new RPSlamwareSdpAgent();
        }

        connectingDialog = new ProgressDialog(this);
        connectingDialog.setMessage("Connecting");
        connectingDialog.setCancelable(false);
        connectingDialog.setCanceledOnTouchOutside(false);
        connectingDialog.show();

        agent.connectTo(addr, port);
    }

    // do not remove this method. It is required for EventBus to answer ConnectedEvent.
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(ConnectedEvent event) {
        gotoRPMapActivity();
    }

    // do not remove this method. It is required for EventBus to answer ConnectionLostEvent.
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(ConnectionLostEvent event) {
        showConnectionFailedDialog();
    }

    // do not remove this method. It is required for EventBus to answer ConnectionFailedEvent.
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(ConnectionFailedEvent event) {
        showConnectionFailedDialog();
    }

    private void showCoverFragment() {
        CoverFragment fragmentCover = new CoverFragment();

        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.add(R.id.fragment_container, fragmentCover);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    private void showConnectFragment() {
        ConnectFragment fragmentConnect = new ConnectFragment();

        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.animator.fragment_fade_in, R.animator.fragment_fade_out);
        transaction.replace(R.id.fragment_container, fragmentConnect, "CONNECT");
        transaction.addToBackStack(null);
        try {
            transaction.commitAllowingStateLoss();
        } catch (IllegalStateException e) {
            LogUtil.e(TAG, "fragment commit illegal state exception");
        }
    }

    private void showConnectionFailedDialog() {
        hideConnectingDialog();

        if (connectionLostDialog != null && connectionLostDialog.isShowing()) {
            return;
        }

        connectionLostDialog = new AlertDialog.Builder(this)
                .setMessage(R.string.connected_failed_message)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int which) {
                        dialogInterface.dismiss();
                    }
                })
                .show();
    }

    private void hideConnectingDialog() {
        if (connectingDialog != null && connectingDialog.isShowing()) {
            connectingDialog.dismiss();
            connectingDialog = null;
        }
    }

    private void gotoRPMapActivity() {
        hideConnectingDialog();
        startActivity(new Intent(this, RPMapActivity.class));
        finish();
    }
}
