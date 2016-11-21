package com.slamtec.android.uicommander.events;

import com.slamtec.slamware.geometry.Line;

import java.util.Vector;

/**
 * Created by denvoko on 12/1/2015.
 */
public class WallUpdateEvent {
    private Vector<Line> walls;

    public WallUpdateEvent(Vector<Line> walls) {
        this.walls = walls;
    }

    public Vector<Line> getWalls() {
        return walls;
    }
}
