package com.slamtec.android.uicommander.agent.exception;

/**
 * Created by Alan on 10/20/15.
 */
public class ConnectionFailException extends Exception {
    public ConnectionFailException() {
        super("Connection Fail Exception");
    }

    public ConnectionFailException(String detailMessage) {
        super(detailMessage);
    }

    public ConnectionFailException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public ConnectionFailException(Throwable throwable) {
        super("Connection Fail Exception", throwable);
    }
}
