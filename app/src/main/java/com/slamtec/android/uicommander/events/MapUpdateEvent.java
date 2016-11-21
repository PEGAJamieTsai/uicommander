package com.slamtec.android.uicommander.events;

import android.graphics.RectF;

/**
 * Created by Alan on 10/21/15.
 */
public class MapUpdateEvent {
    private RectF area;

    public MapUpdateEvent(RectF area) {
        this.area = area;
    }

    public RectF getArea() {
        return area;
    }
}
