package com.slamtec.android.uicommander.data;

import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;

import com.slamtec.slamware.geometry.PointF;
import com.slamtec.slamware.geometry.Size;
import com.slamtec.slamware.robot.Map;

/**
 * Created by Alan on 10/12/15.
 */
public class MapDataCache {
    private final static String TAG = "MapDataCache";

    private RectF lastUpdateMapArea = null;
    private Rect lastUpdateLogicalArea = null;

    private PointF baseCoordinate = null;
    private Point basePixel = null;
    private PointF resolution = null;
    private MapDataStore dataStore = null;

    public MapDataCache() {
        dataStore = new MapDataStore();
        resolution = new PointF();
        baseCoordinate = new PointF();
    }

    public RectF getCachedArea() {
        PointF size = dimensionToSize(dataStore.getArea().width(), dataStore.getArea().height());
        return new RectF(baseCoordinate.getX(), baseCoordinate.getY(),
                baseCoordinate.getX() + size.getX(), baseCoordinate.getY() + size.getY());
    }

    public PointF getResolution() {
        return resolution;
    }

    public Point getDimension() {
        return new Point(dataStore.getArea().width(), dataStore.getArea().height());
    }

    private static PointF translate(com.slamtec.slamware.geometry.PointF pt)
    {
        return new PointF(pt.getX(), pt.getY());
    }

    private static Point translate(Size sz)
    {
        return new Point(sz.getWidth(), sz.getHeight());
    }

    public synchronized void update(Map map) {
        if (map == null) {
            return;
        }
        RectF mapArea = map.getMapArea();

        if (dataStore.isEmpty()) {
            baseCoordinate = translate(map.getOrigin());
            resolution = translate(map.getResolution());
        }

        Rect logicalArea = physicalAreaToLogicalArea(mapArea);

        if (dataStore.isEmpty()) {
            basePixel = new Point(logicalArea.left, logicalArea.top);
        }

        dataStore.expandArea(new Rect(logicalArea));

        lastUpdateMapArea = mapArea;
        lastUpdateLogicalArea = logicalArea;

        Point updateSize = translate(map.getDimension());
        if (logicalArea.width() < updateSize.x) {
            updateSize.x = logicalArea.width();
        }
        if (logicalArea.height() < updateSize.y) {
            updateSize.y = logicalArea.height();
        }

        byte[] mapRawData = map.getData();
        byte[] src = new byte[map.getDimension().getWidth()];
        Rect rect = new Rect();

        for(int y = 0; y < updateSize.y; y++) {
            int left = logicalArea.left;
            int top = logicalArea.bottom - y - 1;
            int right = logicalArea.left + updateSize.x;
            int bottom = top + 1;
            rect.set(left, top ,right, bottom);
            System.arraycopy(mapRawData, map.getDimension().getWidth() * y, src, 0, map.getDimension().getWidth());
            dataStore.update(rect, src);
        }
    }

    public synchronized void fetch(Rect area, byte[] buffer) {
        dataStore.fetch(area, buffer);
    }

    public synchronized byte get(int x, int y) {
        return dataStore.get(x, y);
    }

    public synchronized void clear() {
        dataStore.clear();
    }

    public synchronized boolean isEmpty() {
        return dataStore.isEmpty();
    }

    public PointF pixelToCoordinate(Point pixel) {
        PointF offset = new PointF(pixel.x * resolution.getX(), (-pixel.y - 1) * resolution.getY());
        return new PointF(baseCoordinate.getX() + offset.getX(), baseCoordinate.getY() + offset.getY());
    }

    public Point coordinateToPixel(float x, float y) {
        Point size = sizeToDimension(x - baseCoordinate.getX(), y - baseCoordinate.getY());
        return new Point(size.x, -size.y - 1);
    }

    public PointF dimensionToSize(int width, int height) {
        return new PointF(width * resolution.getX(), height * resolution.getY());
    }

    public Point sizeToDimension(float x, float y) {
        return new Point(Math.round(x / resolution.getX()), Math.round(y / resolution.getY()));
    }

    public Rect physicalAreaToLogicalArea(RectF rect) {
        Point logicalBasePoint = coordinateToPixel(rect.left, rect.top);
        Point logicalSize = sizeToDimension(rect.width(), rect.height());

        int left = logicalBasePoint.x;
        int top = logicalBasePoint.y - logicalSize.y + 1;
        int right = left + logicalSize.x;
        int bottom = top + logicalSize.y;

        return new Rect(left, top, right, bottom);
    }
}
