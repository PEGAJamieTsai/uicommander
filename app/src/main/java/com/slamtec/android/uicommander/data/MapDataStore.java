package com.slamtec.android.uicommander.data;

import android.graphics.Point;
import android.graphics.Rect;

import com.slamtec.android.uicommander.utils.LogUtil;

import java.util.Arrays;

/**
 * Created by Alan on 10/8/15.
 * RectangleI is Rect in android
 * RectangleF is RectF in android
 */
public class MapDataStore {
    private final static String TAG = "MapDataStore";

    private Rect area = null;
    private byte[] mapData = null;

    public MapDataStore() {
        this.area = new Rect(0, 0, 0, 0);
    }

    public synchronized void update(Rect area, byte[] data) {
        if (mapData == null) {
            this.area = new Rect(area);
            mapData = Arrays.copyOf(data, data.length);
        } else {
            expandArea(new Rect(area));

            copyBuffer(mapData, this.area, data, area,
                    new Point(area.left - this.area.left, area.top - this.area.top),
                    new Point(0, 0), new Point(area.width(), area.height()));
        }
    }

    public synchronized void fetch(Rect area, byte[] buffer) {
        Arrays.fill(buffer, (byte) 0);
        copyBuffer(buffer, area, mapData, this.area, new Point(0, 0),
                new Point(area.left - this.area.left, area.top - this.area.top),
                new Point(area.width(), area.height()));
    }

    public void expandArea(Rect area) {
        if (mapData != null) {
            area.union(this.area);

            if (area.equals(this.area)) {
                return;
            }
        }
        byte[] newBuffer = new byte[Math.abs(area.width() * area.height())];
        copyBuffer(newBuffer, area, mapData, this.area,
                new Point(this.area.left - area.left, this.area.top - area.top),
                new Point(0, 0), new Point(this.area.width(), this.area.height()));
        this.area = area;
        this.mapData = newBuffer;
    }

    public void clear() {
        this.area = new Rect(0, 0, 0, 0);
        if (mapData != null) {
            mapData = null;
        }
    }

    public boolean isEmpty() {
        return this.mapData == null;
    }

    public byte get(int x, int y) {
        if (x < area.left || y < area.top || x >= area.right || y >= area.bottom) {
            return 0;
        }

        return mapData[(y - area.top) * area.width() + (x - area.left)];
    }

    public Rect getArea() {
        return this.area;
    }

    private static void copyBuffer(byte[] dest, Rect destSize, byte[] src,
                                         Rect srcSize, Point destOffset, Point srcOffset,
                                         Point dimension) {
        if (dest == null || src == null) {
            return;
        }

//        LogUtil.d(TAG, String.format("Copying buffer from %d, %d to %d, %d of %dx%d", srcOffset.x,
//                srcOffset.y, destOffset.x, destOffset.y, dimension.x, dimension.y));

        if (srcOffset.x < 0) {
            dimension.x += srcOffset.x;
            destOffset.x -= srcOffset.x;
            srcOffset.x = 0;
        }

        if (srcOffset.y < 0) {
            dimension.y += srcOffset.y;
            destOffset.y -= srcOffset.y;
            srcOffset.y = 0;
        }

        if (destOffset.x < 0) {
            dimension.x += destOffset.x;
            srcOffset.x -= destOffset.x;
            destOffset.x = 0;
        }

        if (destOffset.y < 0) {
            dimension.y += destOffset.y;
            srcOffset.y -= destOffset.y;
            destOffset.y = 0;
        }

        Rect srcRect = new Rect(srcOffset.x, srcOffset.y, srcOffset.x + dimension.x,
                srcOffset.y + dimension.y);
        Rect destRect = new Rect(destOffset.x, destOffset.y, destOffset.x + dimension.x,
                destOffset.y + dimension.y);

        srcRect.intersect(new Rect(0, 0, srcSize.width(), srcSize.height()));
        destRect.intersect(new Rect(0, 0, destSize.width(), destSize.height()));

        if (srcRect.width() < destRect.width()) {
            dimension.x = srcRect.width();
        } else {
            dimension.x = destRect.width();
        }

        if (srcRect.height() < destRect.height()) {
            dimension.y = srcRect.height();
        } else {
            dimension.y = destRect.height();
        }

        if (dimension.x <= 0 || dimension.y <= 0) {
            return;
        }

        int destIndex = destRect.top * destSize.width() + destRect.left;
        int srcIndex = srcRect.top * srcSize.width() + srcRect.left;
        int copySize = dimension.x;

        try {
            for (int y = 0; y < dimension.y; y++) {
                System.arraycopy(src, srcIndex, dest, destIndex, copySize);

                destIndex += destSize.width();
                srcIndex += srcSize.width();
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            LogUtil.e(TAG, "copy buffer array index out of bounds");
        }
    }
}
