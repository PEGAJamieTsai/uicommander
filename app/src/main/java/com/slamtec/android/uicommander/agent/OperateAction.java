package com.slamtec.android.uicommander.agent;

/**
 * Created by Alan on 12/9/15.
 */
public class OperateAction {
    private final static String EMPTY_ACTION = "EmptyAction";
    private final static String MOVE_ACTION = "MoveAction";

    public final static int MODE_IDLE = 0;
    public final static int MODE_OPERATING = 1;
    public final static int MODE_STOP = 2;

    private static int mode = MODE_IDLE;

    public synchronized static void on() {
        mode = MODE_OPERATING;
    }

    public synchronized static void off() {
        mode = MODE_STOP;
    }

    public synchronized static void reset() {
        mode = MODE_IDLE;
    }

    public static boolean shouldStop(String action) {
        return mode == MODE_STOP && action.equals(MOVE_ACTION);
    }
}
