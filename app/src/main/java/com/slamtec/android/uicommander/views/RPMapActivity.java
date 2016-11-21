package com.slamtec.android.uicommander.views;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.slamtec.android.uicommander.R;
import com.slamtec.android.uicommander.agent.OperateAction;
import com.slamtec.android.uicommander.events.ConnectedEvent;
import com.slamtec.android.uicommander.events.ConnectionFailedEvent;
import com.slamtec.android.uicommander.events.ConnectionLostEvent;
import com.slamtec.android.uicommander.events.LaserScanUpdateEvent;
import com.slamtec.android.uicommander.events.MapUpdateEvent;
import com.slamtec.android.uicommander.events.MoveActionUpdateEvent;
//import com.slamtec.android.uicommander.events.RobotHealthInfoEvent;
import com.slamtec.android.uicommander.events.RobotPoseUpdateEvent;
import com.slamtec.android.uicommander.events.RobotStatusUpdateEvent;
import com.slamtec.android.uicommander.events.TrafficStatEvent;
import com.slamtec.android.uicommander.events.WallUpdateEvent;
import com.slamtec.android.uicommander.services.TrafficStatService;
import com.slamtec.android.uicommander.utils.LogUtil;
import com.slamtec.android.uicommander.utils.RPGestureDetector;
import com.slamtec.android.uicommander.views.controls.RPControlBar;
import com.slamtec.android.uicommander.views.controls.RPMapView;
import com.slamtec.android.uicommander.views.controls.RPMoveControlPanel;
import com.slamtec.android.uicommander.views.controls.RPVirtualWallEditPanel;
import com.slamtec.slamware.action.MoveDirection;
import com.slamtec.slamware.geometry.PointF;
//import com.slamtec.slamware.robot.HealthInfo;
import com.slamtec.slamware.robot.LaserScan;
import com.slamtec.slamware.robot.Pose;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.lang.ref.WeakReference;

public class RPMapActivity extends BaseActivity implements RPControlBar.IOnButtonClickListener,
        RPMoveControlPanel.IOnButtonClickListener, RPGestureDetector.OnRPGestureListener,
        RPVirtualWallEditPanel.IOnVirtualWallPanelClickListener {
    private final static String TAG = "RPMapActivity";

    private RPMapView mapView;
    private RPControlBar controlBar;
    private RPMoveControlPanel controlPanel;
    private RPVirtualWallEditPanel virtualWallEditPanel;

    private RelativeLayout root;
    private TextView textSdpVersion;
    private TextView textBatteryPercentage;
    private TextView textConnectionStatus;
    private TextView textStatus;
    private TextView textLocalizationQuality;

    private static String sdpVersion = "";
    private static int batteryPercentage = 0;
    private static boolean charging = false;

    private AlertDialog alertDialog = null;

    private RPGestureDetector gestureDetector;

    private boolean lastMapUpdateRequestFinished;

    private Float mapScale = 2.0f;
    private Float mapRotation = 0.0f;
    private Point mapTransition = new Point(0, 0);

    private final static float kRPMapViewMinScale = 0.5f;
    private final static float kRPMapViewMaxScale = 16.0f;

    private static boolean isInitializedView = false;

    private final static float PI = (float) Math.PI;
    private final static float M_PI_2 = PI / 2;

    // wall edit mode
    private boolean isWallEditMode = false;

    // sweep spot mode
    private boolean isSweepSpot = false;

    // wall edit add or remove
    private int wallEditMode = MODE_WALL_NONE;

    // wall edit mode
    private final static int MODE_WALL_NONE = 0;
    private final static int MODE_WALL_ADD = 1;
    private final static int MODE_WALL_REMOVE = 2;

    // robot state update thread
    private Thread robotStateUpdateThread;

    private boolean isRobotStateUpdating;

    // map update runnable
    private Runnable mapUpdateRunnable = new Runnable() {
        @Override
        public void run() {
            mapView.update();
        }
    };

    // robot state update loop runnable
    private Runnable robotStateUpdateRunnable = new Runnable() {
        // main loop count
        private int cnt;

        @Override
        public void run() {
            cnt = 0;
            while (true) {
                if (robotStateUpdateThread == null || !robotStateUpdateThread.isAlive() ||
                        robotStateUpdateThread.isInterrupted()) {
                    isRobotStateUpdating = false;
                    return;
                }

                agent.updatePose();
                agent.updateMoveAction();

                if ((cnt % 30) == 15) {
                    agent.updateRobotStatus();
                    agent.getRobotHealth();
                }

                if ((cnt % 15) == 0) {
                    agent.updateLaserScan();
                    agent.updateWalls();
                }

                synchronized (RPMapActivity.this) {
                    if (!(cnt % 30 == 0) && lastMapUpdateRequestFinished) {
                        lastMapUpdateRequestFinished = false;
                        agent.updateMap();
                    }
                }

                runOnUiThread(mapUpdateRunnable);

                cnt++;

                try {
                    Thread.sleep(33);
                } catch (InterruptedException e) {
                    LogUtil.w(TAG, "robot state update thread interrupted");
                    isRobotStateUpdating = false;
                }
            }
        }
    };

    // flag: whether map view need to initialize drawing maps;
    private static boolean shouldInitializeMaps;

    // if it is screen rotation, do not disconnect
    private static boolean notReconnectWhenScreenRotation = false;

    // status bar height
    private static int statusBarHeight = 0;

    private final static String SCHEME_MAP_SCALE = "map_scale";
    private final static String SCHEME_MAP_ROTATION = "map_rotation";
    private final static String SCHEME_MAP_TRANSITION_X = "map_transition_x";
    private final static String SCHEME_MAP_TRANSITION_Y = "map_transition_y";
    private final static String SCHEME_SDP_VERSION = "sdp_version";
    private final static String SCHEME_BATTERY_PERCENTAGE = "battery_percentage";
    private final static String SCHEME_IS_CHARGING = "is_charging";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rpmap);

        root = (RelativeLayout) findViewById(R.id.root);

        mapView = (RPMapView) findViewById(R.id.map_view);

        controlBar = (RPControlBar) findViewById(R.id.control_bar);
        controlBar.setClickListener(this);

        textSdpVersion = (TextView) findViewById(R.id.text_sdp_version_content);
        textBatteryPercentage = (TextView) findViewById(R.id.text_battery_percentage_content);
        textConnectionStatus = (TextView) findViewById(R.id.text_connection_status_content);
        textStatus = (TextView) findViewById(R.id.text_status);
        textLocalizationQuality = (TextView) findViewById(R.id.text_localization_quality);
        ImageView buttonStop = (ImageView) findViewById(R.id.button_stop);

        buttonStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                agent.cancelAllActions();
            }
        });

        gestureDetector = new RPGestureDetector(this);

        shouldInitializeMaps = true;
        lastMapUpdateRequestFinished = true;

        int resId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resId > 0) {
            statusBarHeight = getResources().getDimensionPixelSize(resId);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putFloat(SCHEME_MAP_SCALE, mapScale);
        outState.putFloat(SCHEME_MAP_ROTATION, mapView.getMapRotation());
        outState.putInt(SCHEME_MAP_TRANSITION_X, mapView.getMapTransition().x);
        outState.putInt(SCHEME_MAP_TRANSITION_Y, mapView.getMapTransition().y);
        outState.putString(SCHEME_SDP_VERSION, sdpVersion);
        outState.putInt(SCHEME_BATTERY_PERCENTAGE, batteryPercentage);
        outState.putBoolean(SCHEME_IS_CHARGING, charging);
        notReconnectWhenScreenRotation = true;

        isInitializedView = false;
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState.containsKey(SCHEME_MAP_SCALE)) {
            mapScale = savedInstanceState.getFloat(SCHEME_MAP_SCALE);
            mapRotation = savedInstanceState.getFloat(SCHEME_MAP_ROTATION);
            int x = savedInstanceState.getInt(SCHEME_MAP_TRANSITION_X, 0);
            int y = savedInstanceState.getInt(SCHEME_MAP_TRANSITION_Y, 0);
            sdpVersion = savedInstanceState.getString(SCHEME_SDP_VERSION);
            batteryPercentage = savedInstanceState.getInt(SCHEME_BATTERY_PERCENTAGE, 100);
            charging = savedInstanceState.getBoolean(SCHEME_IS_CHARGING, false);
            notReconnectWhenScreenRotation = false;

            mapTransition = new Point(x, y);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);

        startService(new Intent(this, TrafficStatService.class));
    }

    @Override
    protected void onStop() {
        super.onStop();

        stopService(new Intent(this, TrafficStatService.class));

        EventBus.getDefault().unregister(this);

        // avoid app crash in MIUI.
        isInitializedView = false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        lastMapUpdateRequestFinished = true;
        initMapView();
        startRobotStateUpdate();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopRobotStateUpdate();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (!notReconnectWhenScreenRotation) {
            agent.cancelAllActions();
            agent.disconnect();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mapScale = mapView.getMapScale();
                break;
        }
        gestureDetector.onTouchEvent(event);
        return super.onTouchEvent(event);
    }

    @Override
    public void onButtonEditWallClicked() {
        textStatus.setText(getString(R.string.edit_wall));
        isWallEditMode = true;
        wallEditMode = MODE_WALL_NONE;
        showVirtualWallEditPanel();
    }

    @Override
    public void onButtonAddWallClicked() {
        textStatus.setText(getString(R.string.add_wall));
        wallEditMode = MODE_WALL_ADD;
    }

    @Override
    public void onButtonRemoveWallClicked() {
        textStatus.setText(getString(R.string.remove_wall));
        wallEditMode = MODE_WALL_REMOVE;
    }

    @Override
    public void onButtonClearWallsClicked() {
        textStatus.setText(getString(R.string.clear_walls));
        agent.clearWalls();
        wallEditMode = MODE_WALL_NONE;
    }

    @Override
    public void onButtonExitWallEditClicked() {
        textStatus.setText("");
        mapView.clearWallIndicator();
        isWallEditMode = false;
        wallEditMode = MODE_WALL_NONE;

        hideVirtualWallEditPanel();
    }

    @Override
    public void onButtonClearWallClicked() {
        if (isWallEditMode) {
            return;
        }
        agent.clearMap();
        agent.clearWalls();
        mapView.clearMoveTrack();
    }

    @Override
    public void onButtonSweepClicked() {
        if (isWallEditMode) {
            return;
        }
        agent.startSweep();
    }

    @Override
    public void onButtonHomeClicked() {
        if (isWallEditMode) {
            return;
        }
        agent.goHome();
    }

    @Override
    public void onButtonClearTrackClicked() {
        if (isWallEditMode) {
            return;
        }
        mapView.clearMoveTrack();
    }

    @Override
    public void onButtonQuitToConnectClicked() {
        if (isWallEditMode) {
            return;
        }
        goBackToMainActivity();
    }

    @Override
    public void onButtonControllerClicked() {
        if (isWallEditMode) {
            return;
        }
        showMoveControlPanel();
    }

    @Override
    public void onButtonSweepSpotClicked() {
        isSweepSpot = !isSweepSpot;
        if (isSweepSpot) {
            textStatus.setText(R.string.sweep_spot);
        } else {
            textStatus.setText("");
        }
    }

    @Override
    public void onButtonForwardClicked() {
        if (isWallEditMode) {
            return;
        }
        agent.moveBy(MoveDirection.FORWARD);
    }

    @Override
    public void onButtonBackwardClicked() {
        if (isWallEditMode) {
            return;
        }
        agent.moveBy(MoveDirection.BACKWARD);
    }

    @Override
    public void onButtonTurnLeftClicked() {
        if (isWallEditMode) {
            return;
        }
        agent.moveBy(MoveDirection.TURN_LEFT);
    }

    @Override
    public void onButtonTurnRightClicked() {
        if (isWallEditMode) {
            return;
        }
        agent.moveBy(MoveDirection.TURN_RIGHT);
    }

    @Override
    public void onButtonHideClicked() {
        hideMoveControlPanel();
    }

    @Override
    public void onMapTap(MotionEvent event) {
        // 地图点击
        int x = Math.round(event.getX());
        int y = Math.round(event.getY()) - statusBarHeight;
        Point rawPoint = new Point(x, y);
        if (isWallEditMode) {
            if (wallEditMode == MODE_WALL_ADD) {
                mapView.setVirtualWallIndicator(rawPoint);
            } else if (wallEditMode == MODE_WALL_REMOVE) {
                mapView.removeWall(rawPoint);
                wallEditMode = MODE_WALL_NONE;
            }
        } else {
            if (isSweepSpot) {
                mapView.sweepSpot(rawPoint);
                isSweepSpot = false;
                textStatus.setText("");
            } else {
                mapView.moveTo(rawPoint);
            }
        }
    }

    @Override
    public void onMapPinch(float factor) {
        // 地图缩放
        mapScale *= factor;
        if (mapScale < kRPMapViewMinScale) {
            mapScale = kRPMapViewMinScale;
        } else if (mapScale > kRPMapViewMaxScale) {
            mapScale = kRPMapViewMaxScale;
        }
        mapView.setMapScale(mapScale, false);
    }

    @Override
    public void onMapMove(int distanceX, int distanceY) {
        mapView.setMapTransition(new Point(Math.round(distanceX), Math.round(distanceY)));
    }

    @Override
    public void onMapRotate(float factor) {
        mapView.setMapRotation(factor);
    }

    // do not remove this method. It is required for EventBus to answer ConnectedEvent.
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(ConnectedEvent event) {
        OperateAction.reset();
        startRobotStateUpdate();
        mapView.clearMoveTrack();
    }

    // do not remove this method. It is required for EventBus to answer ConnectionFailedEvent.
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(ConnectionFailedEvent event) {
        stopRobotStateUpdate();
        showDisconnectedDialog();
        OperateAction.reset();
    }

    // do not remove this method. It is required for EventBus to answer ConnectionLostEvent.
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(ConnectionLostEvent event) {
        stopRobotStateUpdate();
        showDisconnectedDialog();
        OperateAction.reset();
    }

    // do not remove this method. It is required for EventBus to answer LaserScanUpdateEvent.
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(LaserScanUpdateEvent event) {
        LaserScan laserScan = agent.getLaserScan();
        Pose pose = agent.getRobotPose();
        mapView.updateLaserScan(laserScan, pose);
    }

    // do not remove this method. It is required for EventBus to answer MapUpdateEvent.
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(MapUpdateEvent event) {
        synchronized (this) {
            lastMapUpdateRequestFinished = true;
        }

        if (event.getArea().isEmpty()) {
            return;
        }

        mapView.updateMapArea(event.getArea());

        setSweepMapExtraScale();
    }

    // do not remove this method. It is required for EventBus to answer MoveActionUpdateEvent
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(MoveActionUpdateEvent event) {
        mapView.updateRemainingMilestones(event.getRemainingMilestones(), event.getRemainingPath());
    }

    // do not remove this method. It is required for EventBus to answer RobotPoseUpdateEvent
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(RobotPoseUpdateEvent event) {
        Pose pose = event.getPose();

        if (pose == null) {
            return;
        }

        Point logicalLocation = agent.getMapData().coordinateToPixel((float) pose.getX(), (float) pose.getY());

        mapView.setCenterLocation(new PointF(logicalLocation.x, logicalLocation.y), false);
        mapView.setRobotIndicatorRotation((float) (-pose.getYaw() + M_PI_2));

        mapView.appendMoveTrack(logicalLocation);
    }

    // do not remove this method. It is required for EventBus to answer RobotStatusUpdateEvent
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(RobotStatusUpdateEvent event) {
        if (isWallEditMode) {
            return;
        }

        textBatteryPercentage.setText(!event.isCharging() ?
                String.format("%d%%", event.getBatteryPercentage()) :
                String.format("%d%% | AC on", event.getBatteryPercentage()));

        textLocalizationQuality.setText(Integer.toString(event.getLocalizationQuality()));
    }

    // do not remove this method. It is required for EventBus to answer WallUpdateEvent
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(WallUpdateEvent event) {
        mapView.updateWalls(event.getWalls());
    }

    // do not remove this method. It is required for EventBus to answer TrafficStatEvent
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(TrafficStatEvent event) {
        textConnectionStatus.setText(event.getTraffic());
    }

    // do not remove this method. It is required for EventBus to answer RobotHealthInfoEvent
    /*@Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(RobotHealthInfoEvent event) {
        HealthInfo info = event.getInfo();

        if (info == null) {
            return;
        }

        if (info.isWarning() || info.isError() || info.isFatal() || (info.getErrors() != null && info.getErrors().size() > 0)) {
            String msg = "";
            for (HealthInfo.BaseError error : info.getErrors()) {
                String level;
                switch (error.getErrorLevel()) {
                    case HealthInfo.BaseError.BaseErrorLevelWarn:
                        level = "Warning";
                        break;
                    case HealthInfo.BaseError.BaseErrorLevelError:
                        level = "Error";
                        break;
                    case HealthInfo.BaseError.BaseErrorLevelFatal:
                        level = "Fatal";
                        break;
                    default:
                        level = "Unknown";
                        break;
                }
                String component;
                switch (error.getErrorComponent()) {
                    case HealthInfo.BaseError.BaseErrorComponentUser:
                        component = "User";
                        break;
                    case HealthInfo.BaseError.BaseErrorComponentMotion:
                        component = "Motion";
                        break;
                    case HealthInfo.BaseError.BaseErrorComponentPower:
                        component = "Power";
                        break;
                    case HealthInfo.BaseError.BaseErrorComponentSensor:
                        component = "Sensor";
                        break;
                    case HealthInfo.BaseError.BaseErrorComponentSystem:
                        component = "System";
                        break;
                    default:
                        component = "Unknown";
                        break;
                }
                msg += String.format("Error ID: %d\nError level: %s\nError Component: %s\nError message: %s\n------\n", error.getId(), level, component, error.getErrorMessage());

                new AlertDialog.Builder(this)
                        .setTitle("Robot Health")
                        .setMessage(msg)
                        .setNegativeButton("OKey", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .show();

            }
        }
    }*/

    private void initMapView() {
        if (!isInitializedView) {
            Point size = new Point();
            getWindowManager().getDefaultDisplay().getSize(size);
            mapView.init(new WeakReference<>(agent), size.x, size.y, shouldInitializeMaps);
            isInitializedView = true;
            shouldInitializeMaps = false;

            mapView.setMapScale(mapScale, false);
            mapView.setMapTransition(mapTransition);
            mapView.setMapRotation(mapRotation);

            setSweepMapExtraScale();

            textSdpVersion.setText(agent.getSDPVersion());
        }
    }

    private void setSweepMapExtraScale() {
        if (agent.getSweepMapData().getResolution().getX() != 0 ||
                agent.getMapData().getResolution().getX() != 0) {
            float extraScale = agent.getSweepMapData().getResolution().getX() /
                    agent.getMapData().getResolution().getX();
            if (extraScale != mapView.getSweepMapExtraScale()) {
                mapView.setSweepMapExtraScale(extraScale);
            }
        }
    }

    private void startRobotStateUpdate() {
        if (!isRobotStateUpdating) {
            robotStateUpdateThread = new Thread(robotStateUpdateRunnable);
            robotStateUpdateThread.start();
            isRobotStateUpdating = true;
        }
    }

    private void stopRobotStateUpdate() {
        if (robotStateUpdateThread != null && !robotStateUpdateThread.isInterrupted()) {
            robotStateUpdateThread.interrupt();
            robotStateUpdateThread = null;
        }
        isRobotStateUpdating = false;
    }

    private void showDisconnectedDialog() {
        if (alertDialog != null) {
            return;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.disconnected_message)
                .setTitle(R.string.disconnected_title)
                .setPositiveButton(R.string.reconnect_button, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        agent.reconnect();
                        alertDialog = null;
                    }
                })
                .setNegativeButton(R.string.back_button, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface1, int which) {
                        dialogInterface1.dismiss();
                        alertDialog = null;
                        goBackToMainActivity();
                    }
                });
        alertDialog = builder.show();
        alertDialog.setCancelable(false);
        alertDialog.setCanceledOnTouchOutside(false);
    }

    private void goBackToMainActivity() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    private void showMoveControlPanel() {
        if (controlPanel != null) {
            return;
        }
        controlPanel = new RPMoveControlPanel(this);
        controlPanel.setClickListener(this);

        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.addRule(RelativeLayout.ABOVE, R.id.control_bar);
        lp.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
        lp.bottomMargin = 10;

        root.addView(controlPanel, lp);
    }

    private void hideMoveControlPanel() {
        if (controlPanel == null) {
            return;
        }
        controlPanel.setVisibility(View.GONE);
        controlPanel = null;
    }

    private void showVirtualWallEditPanel() {
        if (virtualWallEditPanel != null) {
            return;
        }

        hideMoveControlPanel();

        virtualWallEditPanel = new RPVirtualWallEditPanel(this);
        virtualWallEditPanel.setClickListener(this);

        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
        lp.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
        lp.bottomMargin = 20;

        root.addView(virtualWallEditPanel, lp);

        controlBar.setVisibility(View.INVISIBLE);
    }

    private void hideVirtualWallEditPanel() {
        if (virtualWallEditPanel == null) {
            return;
        }

        virtualWallEditPanel.setVisibility(View.GONE);
        virtualWallEditPanel = null;

        controlBar.setVisibility(View.VISIBLE);
    }
}
