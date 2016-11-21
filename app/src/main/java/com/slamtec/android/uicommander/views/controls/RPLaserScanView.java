package com.slamtec.android.uicommander.views.controls;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import com.slamtec.slamware.geometry.PointF;

import com.slamtec.android.uicommander.agent.RPSlamwareSdpAgent;
import com.slamtec.slamware.robot.LaserPoint;
import com.slamtec.slamware.robot.LaserScan;
import com.slamtec.slamware.robot.Pose;

import java.lang.ref.WeakReference;
import java.util.Vector;

/**
 * Created by Alan on 10/13/15.
 */
public class RPLaserScanView extends RPSlamwareBaseView {
    private final static String TAG = "RPLaserScanView";

    private LaserScan laserScan;
    private Pose pose;

    private Paint paint;
    private PointF centerPosition;
    private Point uiCenter;

    public RPLaserScanView(Context context, WeakReference<RPSlamwareSdpAgent> agent) {
        super(context, agent);

        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        centerPosition = new PointF();
        uiCenter = new Point();

        setBackgroundColor(Color.TRANSPARENT);

        setWillNotDraw(false);
    }

    public void updateLaserScan(LaserScan laserScan, Pose pose) {
        this.laserScan = laserScan;
        this.pose = pose;

        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (laserScan != null) {
            Vector<LaserPoint> scanPoints = laserScan.getLaserPoints();

            Pose robotPose = this.pose;

            paint.setColor(Color.RED);

            for (LaserPoint scanPoint : scanPoints) {
                if (!scanPoint.isValid()) {
                    continue;
                }

                double phi = scanPoint.getAngle() + robotPose.getYaw();
                double r = scanPoint.getDistance();

                double physicalX = robotPose.getX() + r * Math.cos(phi);
                double physicalY = robotPose.getY() + r * Math.sin(phi);

                centerPosition.setX((float)physicalX);
                centerPosition.setY((float)physicalY);

                uiCenter = layoutRotatedCoordinateForPhysicalCoordinate(
                        centerPosition, getLayoutOffset());

                canvas.drawRect(uiCenter.x - 1, uiCenter.y - 1, uiCenter.x + 1, uiCenter.y + 1, paint);
            }
        }
    }
}
