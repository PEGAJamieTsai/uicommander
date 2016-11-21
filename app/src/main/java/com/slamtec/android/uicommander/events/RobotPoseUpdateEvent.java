package com.slamtec.android.uicommander.events;

import com.slamtec.slamware.robot.Pose;

/**
 * Created by Alan on 10/21/15.
 */
public class RobotPoseUpdateEvent {
    private Pose pose;

    public RobotPoseUpdateEvent(Pose pose) {
        this.pose = pose;
    }

    public Pose getPose() {
        return pose;
    }
}
