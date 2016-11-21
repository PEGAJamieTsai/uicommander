package com.slamtec.android.uicommander.agent.exception;

/**
 * Created by Alan on 10/20/15.
 */
public class PathFindFailException extends Exception {
    public PathFindFailException() {
        super("Path Find Fail Exception");
    }

    public PathFindFailException(String detailMessage) {
        super(detailMessage);
    }

    public PathFindFailException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public PathFindFailException(Throwable throwable) {
        super("Path Find Fail Exception", throwable);
    }
}
