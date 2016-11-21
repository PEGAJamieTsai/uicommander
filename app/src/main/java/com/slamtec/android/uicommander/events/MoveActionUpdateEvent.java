package com.slamtec.android.uicommander.events;

import com.slamtec.slamware.action.Path;

/**
 * Created by Alan on 10/21/15.
 */
public class MoveActionUpdateEvent {
    private Path remainingMilestones;
    private Path remainingPath;

    public MoveActionUpdateEvent(Path remainingMilestones, Path remainingPath) {
        this.remainingMilestones = remainingMilestones;
        this.remainingPath = remainingPath;
    }

    public Path getRemainingMilestones() {
        return remainingMilestones;
    }

    public Path getRemainingPath() {
        return remainingPath;
    }
}
