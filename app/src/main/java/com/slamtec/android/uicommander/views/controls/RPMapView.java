package com.slamtec.android.uicommander.views.controls;

import android.content.Context;
import android.graphics.Point;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.slamtec.android.uicommander.R;
import com.slamtec.android.uicommander.agent.RPSlamwareSdpAgent;
import com.slamtec.slamware.action.Path;
import com.slamtec.slamware.geometry.Line;
import com.slamtec.slamware.geometry.PointF;
import com.slamtec.slamware.robot.LaserScan;
import com.slamtec.slamware.robot.Pose;

import java.lang.ref.WeakReference;
import java.util.Vector;

/**
 * Created by Alan on 10/14/15.
 */
public class RPMapView extends FrameLayout {
    private final static String TAG = "RPMapView";

    private WeakReference<RPSlamwareSdpAgent> agent;

    private boolean isMapScaleSet = false;
    private boolean isCenterLocationSet = false;
    private float targetMapScale;
    private float mapScale;
    private float sweepMapExtraScale;
    private PointF targetCenterLocation;
    private PointF centerLocation;

    private Point mapTransition;
    private float mapRotation;
    private float robotExtraRotation;

    private RPScrollTileMapView scrollMapView;
    private RPMoveActionView moveActionView;
    private RPLaserScanView laserScanView;
    private RPSweepMapView sweepMapView;
    private RPVirtualWallView virtualWallView;
    private RPMoveTrackView moveTrackView;

    private ImageView robotIndicatorView;

    private static int SCREEN_WIDTH = 0;
    private static int SCREEN_HEIGHT = 0;

    private final static float kRPMapViewControllerEasingRate = 0.1f;

    public RPMapView(Context context) {
        super(context);
    }

    public RPMapView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public RPMapView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @SuppressWarnings("deprecation")
    public void init(WeakReference<RPSlamwareSdpAgent> agent, int screenWidth, int screenHeight,
                     boolean shouldInitializeMaps) {
        setBackgroundColor(getContext().getResources().getColor(R.color.MapViewBackground));

        this.agent = agent;

        SCREEN_WIDTH = screenWidth;
        SCREEN_HEIGHT = screenHeight;

        targetMapScale = 1f;
        mapScale = 1f;
        sweepMapExtraScale = 1f;
        targetCenterLocation = new PointF();
        centerLocation = new PointF();
        mapTransition = new Point();
        mapRotation = 0f;
        robotExtraRotation = 0f;

        if (shouldInitializeMaps) {
            initView();
        }
    }

    private void initView() {
        LayoutParams lp = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);

        scrollMapView = new RPScrollTileMapView(getContext(), agent);
        moveActionView = new RPMoveActionView(getContext(), agent);
        laserScanView = new RPLaserScanView(getContext(), agent);
        sweepMapView = new RPSweepMapView(getContext(), agent);
        virtualWallView = new RPVirtualWallView(getContext(), agent);
        moveTrackView = new RPMoveTrackView(getContext(), agent);

        robotIndicatorView = new ImageView(getContext());
        robotIndicatorView.setImageResource(R.mipmap.robotindicator);

        addView(scrollMapView, lp);
        addView(sweepMapView, lp);
        addView(moveActionView, lp);
        addView(laserScanView, lp);
        addView(virtualWallView, lp);
        addView(moveTrackView, lp);

        lp = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.gravity = Gravity.CENTER;
        addView(robotIndicatorView, lp);

        scrollMapView.layout(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);
        sweepMapView.layout(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);
        moveActionView.layout(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);
        laserScanView.layout(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);
        moveTrackView.layout(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);
    }

    public void updateMapArea(RectF area) {
        scrollMapView.updateArea(agent.get().getMapData().physicalAreaToLogicalArea(area));
        sweepMapView.updateSweepMap(agent.get().getSweepMapData().physicalAreaToLogicalArea(area));
    }

    public void updateRemainingMilestones(Path remainingMilestones, Path remainingPath) {
        moveActionView.updateRemainingMilestones(remainingMilestones, remainingPath);
    }

    public void updateLaserScan(LaserScan laserScan, Pose pose) {
        laserScanView.updateLaserScan(laserScan, pose);
    }

    public void update() {
        if (isMapScaleSet && mapScale != targetMapScale) {
            updateCurrentMapScale(ease(mapScale, targetMapScale, 0.01f));
        }

        if (isCenterLocationSet && (centerLocation.getX() != targetCenterLocation.getX() ||
                centerLocation.getY() != targetCenterLocation.getY())) {
            float newX = ease(centerLocation.getX(), targetCenterLocation.getX(), 0.01f);
            float newY = ease(centerLocation.getY(), targetCenterLocation.getY(), 0.01f);
            updateCurrentCenterLocation(new PointF(newX, newY));
        }
    }

    public float getMapScale() {
        return mapScale;
    }

    public void setMapScale(float scale, boolean animated) {
        if (!animated || !isMapScaleSet) {
            updateCurrentMapScale(scale);
            targetMapScale = scale;
            isMapScaleSet = true;
        } else {
            targetMapScale = scale;
        }
    }

    private void updateCurrentMapScale(float mapScale) {
        this.mapScale = mapScale;

        this.scrollMapView.setMapScale(mapScale);
        this.moveActionView.setMapScale(mapScale);
        this.laserScanView.setMapScale(mapScale);
        this.virtualWallView.setMapScale(mapScale);
        this.sweepMapView.setMapScale(mapScale * sweepMapExtraScale);
        this.moveTrackView.setMapScale(mapScale);
    }

    public float getSweepMapExtraScale() {
        return sweepMapExtraScale;
    }

    public void setSweepMapExtraScale(float sweepMapExtraScale) {
        this.sweepMapExtraScale = sweepMapExtraScale;
        this.sweepMapView.setMapScale(mapScale * sweepMapExtraScale);
        this.sweepMapView.setCenterLocation(new PointF(centerLocation.getX() / sweepMapExtraScale,
                centerLocation.getY() / sweepMapExtraScale));
    }

    public PointF getCenterLocation() {
        return centerLocation;
    }

    public void setCenterLocation(PointF location, boolean animated) {
        if (!animated || !isCenterLocationSet) {
            updateCurrentCenterLocation(location);
            targetCenterLocation = location;
            isCenterLocationSet = true;
        } else {
            targetCenterLocation = location;
        }
    }

    private void updateCurrentCenterLocation(PointF centerLocation) {
        this.centerLocation = centerLocation;

        this.scrollMapView.setCenterLocation(centerLocation);
        this.moveActionView.setCenterLocation(centerLocation);
        this.laserScanView.setCenterLocation(centerLocation);
        this.virtualWallView.setCenterLocation(centerLocation);
        this.sweepMapView.setCenterLocation(new PointF(centerLocation.getX() / sweepMapExtraScale,
                centerLocation.getY() / sweepMapExtraScale));
        this.moveTrackView.setCenterLocation(centerLocation);
    }

    public Point getMapTransition() {
        return mapTransition;
    }

    public void setMapTransition(Point mapTransition) {
        if (mapTransition == null) {
            return;
        }

        Point trans = new Point(this.mapTransition.x + mapTransition.x,
                this.mapTransition.y + mapTransition.y);

        if (Math.abs(trans.x) > SCREEN_WIDTH / 2 || Math.abs(trans.y) > SCREEN_HEIGHT / 2) {
            return;
        }

        this.mapTransition = trans;

        robotIndicatorView.setX(robotIndicatorView.getX() + mapTransition.x);
        robotIndicatorView.setY(robotIndicatorView.getY() + mapTransition.y);

        this.scrollMapView.setTransition(mapTransition);
        this.moveActionView.setTransition(mapTransition);
        this.laserScanView.setTransition(mapTransition);
        this.sweepMapView.setTransition(mapTransition);
        this.virtualWallView.setTransition(mapTransition);
        this.moveTrackView.setTransition(mapTransition);

        this.moveActionView.refreshMilestonesAfterTransition();
        this.virtualWallView.refreshIndicatorAfterZoomAndRotate();
    }

    public float getMapRotation() {
        return mapRotation;
    }

    public void setMapRotation(float mapRotation) {
        this.mapRotation += mapRotation;

        float degress = (float) ((robotExtraRotation + this.mapRotation) * 180 / Math.PI);
        this.robotIndicatorView.setRotation(degress);

        this.scrollMapView.setRotation(mapRotation);
        this.moveActionView.setRotation(mapRotation);
        this.laserScanView.setRotation(mapRotation);
        this.sweepMapView.setRotation(mapRotation);
        this.virtualWallView.setRotation(mapRotation);
        this.moveTrackView.setRotation(mapRotation);

        this.scrollMapView.applyRotation();
        this.sweepMapView.applyRotation();

        this.virtualWallView.refreshIndicatorAfterZoomAndRotate();
    }

    public void moveTo(Point rawPoint) {
        scrollMapView.moveTo(rawPoint);
    }

    public void sweepSpot(Point rawPoint) {
        scrollMapView.sweepSpot(rawPoint);
    }

    public void setRobotIndicatorRotation(float r) {
        robotExtraRotation = r;
        float rotation = r + mapRotation;

        float degree = (float) (rotation * 180 / Math.PI);

        robotIndicatorView.setRotation(degree);
    }

    private float ease(float old, float target, float min) {
        float diff = target - old;
        float absDiff = Math.abs(diff);

        if (absDiff < min) {
            return target;
        }

        return old + diff * kRPMapViewControllerEasingRate;
    }

    public void updateWalls(Vector<Line> walls) {
        this.virtualWallView.updateWalls(walls);
    }

    public void setVirtualWallIndicator(Point touchPoint) {
        this.virtualWallView.setWallIndicator(touchPoint);
    }

    public void removeWall(Point touchPoint) {
        this.virtualWallView.removeWall(touchPoint);
    }

    public void clearWallIndicator() {
        this.virtualWallView.clearIndicators();
    }

    public void appendMoveTrack(Point logicalPoint) {
        moveTrackView.appendMoveTrack(logicalPoint);
    }

    public void clearMoveTrack() {
        moveTrackView.clearMoveTrack();
    }
}
