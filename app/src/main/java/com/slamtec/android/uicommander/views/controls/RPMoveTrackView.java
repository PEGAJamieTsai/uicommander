package com.slamtec.android.uicommander.views.controls;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;

import com.slamtec.android.uicommander.R;
import com.slamtec.android.uicommander.agent.RPSlamwareSdpAgent;
import com.slamtec.android.uicommander.utils.ExecutorUtil;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

/**
 * Created by Alan on 12/10/15.
 */
public class RPMoveTrackView extends RPSlamwareBaseView {

    private ArrayList<Point> tracks;
    private ArrayList<Point> rawTracks;

    private Paint paint;

    private Runnable workingRunnable = new Runnable() {
        @Override
        public void run() {
            ArrayList<Point> data;
            ArrayList<Point> handled = new ArrayList<>();
            synchronized (RPMoveTrackView.this) {
                data = new ArrayList<>(rawTracks);
            }

            int count = data.size();

            if (count <= 0) {
                return;
            }

            Point offset = getLayoutOffset();

            Point prev = data.get(0);

            for (int i = 1; i < count; i++) {
                Point point = data.get(i);

                if (distance(prev, point) < 10) {
                    Point h = layoutRotatedCoordinateForLogicalPixel(prev, offset);
                    handled.add(h);
                }

                prev.set(point.x, point.y);
            }

            synchronized (RPMoveTrackView.this) {
                tracks = new ArrayList<>(handled);
            }
        }
    };


    @SuppressWarnings("deprecation")
    public RPMoveTrackView(Context context, WeakReference<RPSlamwareSdpAgent> agent) {
        super(context, agent);

        setWillNotDraw(false);

        tracks = new ArrayList<>();
        rawTracks = new ArrayList<>();

        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(context.getResources().getColor(R.color.red));
        paint.setStrokeWidth(4);

        setBackgroundColor(Color.TRANSPARENT);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        ArrayList<Point> data;

        synchronized (this) {
            data = tracks;
        }

        int count = data.size();
        for (int i = 0; i < count; i++) {
            if ((i + 1) >= count) {
                return;
            }

            Point p1 = data.get(i);
            Point p2 = data.get(i + 1);

            canvas.drawLine(p1.x, p1.y, p2.x, p2.y, paint);
        }
    }

    @Override
    public void invalidate() {
        super.invalidate();
        ExecutorUtil.getInstance().execute(workingRunnable);
    }

    public synchronized void appendMoveTrack(Point logicalPoint) {
        if (rawTracks.size() == 0 || !logicalPoint.equals(rawTracks.get(rawTracks.size() - 1))) {
            rawTracks.add(logicalPoint);
            ExecutorUtil.getInstance().execute(workingRunnable);
        }
    }

    public synchronized void clearMoveTrack() {
        rawTracks.clear();
        ExecutorUtil.getInstance().execute(workingRunnable);
    }

    private double distance(Point p1, Point p2) {
        return Math.sqrt(Math.pow(p1.x - p2.x, 2) + Math.pow(p1.y - p2.y, 2));
    }
}