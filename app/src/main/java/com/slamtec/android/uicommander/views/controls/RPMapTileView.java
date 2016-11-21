package com.slamtec.android.uicommander.views.controls;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.view.View;

import com.slamtec.android.uicommander.agent.RPSlamwareSdpAgent;
import com.slamtec.android.uicommander.utils.ExecutorUtil;
import com.slamtec.android.uicommander.utils.ImageUtil;
import com.slamtec.android.uicommander.utils.LogUtil;

import java.lang.ref.WeakReference;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Alan on 10/8/15.
 */
public class RPMapTileView extends View {
    private final static String TAG = "RPMapTileView";

    private WeakReference<RPSlamwareSdpAgent> agent;
    private Point index;
    private Rect area;

    private Bitmap bitmap;

    private Paint paint;

    private float scale;

    private boolean isSweepMode;

    private Runnable updateRunnable = new Runnable() {
        @Override
        public void run() {
            byte[] buffer = new byte[Math.abs(area.width() * area.height())];

            if (agent.get() != null) {
                if (isSweepMode) {
                    agent.get().getSweepMapData().fetch(new Rect(area), buffer);
                } else {
                    agent.get().getMapData().fetch(new Rect(area), buffer);
                }
            }

            Bitmap bm;

            if (isSweepMode) {
                bm = ImageUtil.createSweepImage(buffer, new Point(Math.abs(area.width()),
                        Math.abs(area.height())));
            } else {
                bm = ImageUtil.createImage(buffer, new Point(Math.abs(area.width()),
                        Math.abs(area.height())));
            }

            synchronized (RPMapTileView.this) {
                bitmap = bm;
            }

            postInvalidate();
        }
    };

    public RPMapTileView(Context context, WeakReference<RPSlamwareSdpAgent> agent, Point index,
                         Rect area, boolean isSweepMode) {
        super(context);

        this.agent = agent;
        this.index = index;
        this.area = area;
        this.scale = 1f;
        this.isSweepMode = isSweepMode;

        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setFilterBitmap(!isSweepMode);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.scale(scale, scale);
        synchronized (this) {
            if (bitmap != null) {
                canvas.drawBitmap(bitmap, 0f, 0f, paint);
            }
        }
        super.onDraw(canvas);
    }

    public void updateArea(Rect area) {
        area.intersect(this.area);

        if (area.isEmpty()) {
            return;
        }

        ExecutorUtil.getInstance().execute(updateRunnable);
    }

    public void updateScale(float scale) {
        this.scale = scale;
    }

    public Point getIndex() {
        return index;
    }
}
