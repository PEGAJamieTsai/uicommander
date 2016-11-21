package com.slamtec.android.uicommander.views.controls;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;

import com.slamtec.android.uicommander.agent.RPSlamwareSdpAgent;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

/**
 * Created by Alan on 10/13/15.
 */
public class RPSweepMapView extends RPSlamwareBaseView {

    private ArrayList<RPMapTileView> mosaics;

    private final static int kRPScrollTileMapViewTileSize = 512;

    public RPSweepMapView(Context context, WeakReference<RPSlamwareSdpAgent> agent) {
        super(context, agent);

        mosaics = new ArrayList<>();

        setBackgroundColor(Color.TRANSPARENT);
    }

    @Override
    public void invalidate() {
        super.invalidate();
        refreshUI();
    }

    private void refreshUI() {
        Point offset = getLayoutOffset();
        for (RPMapTileView tileView: mosaics) {

            if (tileView == null) {
                continue;
            }

            Point index = tileView.getIndex();

            int left = index.x * kRPScrollTileMapViewTileSize;
            int top = index.y * kRPScrollTileMapViewTileSize;
            int right = left + kRPScrollTileMapViewTileSize;
            int bottom = top + kRPScrollTileMapViewTileSize;
            Rect logicalRect = new Rect(left, top, right, bottom);

            RectF layoutRect = layoutRectForLogicalRect(logicalRect, offset);
            tileView.layout(Math.round(layoutRect.left), Math.round(layoutRect.top),
                    Math.round(layoutRect.right), Math.round(layoutRect.bottom));

            // 旋转的情况
            float rotationPivotX = getWidth() / 2 - tileView.getX() + getTransition().x;
            float rotationPivotY = getHeight() / 2 - tileView.getY() + getTransition().y;
            tileView.setPivotX(rotationPivotX);
            tileView.setPivotY(rotationPivotY);
            tileView.setRotation(this.getRotation());

            tileView.updateScale(getMapScale());
        }
    }

    public void updateSweepMap(Rect area) {
        Point startIndex = getTileIndexForPixel(new Point(area.left, area.top));
        Point count = getTileCountForSize(new Point(area.width(), area.height()));

        if (area.right >= ((startIndex.x + count.x) * kRPScrollTileMapViewTileSize)) {
            count.x++;
        }

        if (area.bottom >= ((startIndex.y + count.y) * kRPScrollTileMapViewTileSize)) {
            count.y++;
        }

        for (int y = 0; y < count.y; y++) {
            int j = y + startIndex.y;
            for (int x = 0; x < count.x; x++) {
                int i = x + startIndex.x;
                RPMapTileView tileView = lookupOrCreateTileForIndex(new Point(i, j));
                tileView.updateArea(area);
            }
        }
    }

    private Point getTileIndexForPixel(Point pixel) {
        int x = (int)Math.floor(pixel.x / (double) kRPScrollTileMapViewTileSize);
        int y = (int)Math.floor(pixel.y / (double) kRPScrollTileMapViewTileSize);
        return new Point(x, y);
    }

    private Point getTileCountForSize(Point size) {
        int x = (int)Math.ceil(size.x / (double) kRPScrollTileMapViewTileSize);
        int y = (int)Math.ceil(size.y / (double) kRPScrollTileMapViewTileSize);
        return new Point(x, y);
    }

    private RPMapTileView lookupOrCreateTileForIndex(Point index) {
        for (RPMapTileView view: mosaics) {
            if (view == null) {
                continue;
            }

            if (view.getIndex().equals(index)) {
                return view;
            }
        }

        int left = index.x * kRPScrollTileMapViewTileSize;
        int top = index.y * kRPScrollTileMapViewTileSize;
        int right = left + kRPScrollTileMapViewTileSize;
        int bottom = top + kRPScrollTileMapViewTileSize;
        Rect tileArea = new Rect(left, top, right, bottom);

        RPMapTileView tile = new RPMapTileView(getContext(), agent, index, tileArea, true);
        mosaics.add(tile);

        addView(tile);

        RectF frame = layoutRectForLogicalRect(tileArea);
        tile.layout(Math.round(frame.left), Math.round(frame.top), Math.round(frame.right),
                Math.round(frame.bottom));

        return tile;
    }

    public void applyRotation() {
        for (RPMapTileView tileView : mosaics) {
            tileView.setRotation(getRotation());
        }
    }
}
