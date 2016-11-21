package com.slamtec.android.uicommander.agent.exception;

/**
 * Created by Alan on 10/20/15.
 */
public class ConnectionTimeOutException extends Exception {
    public ConnectionTimeOutException() {
        super("Connection Time Out Exception");
    }

    public ConnectionTimeOutException(String detailMessage) {
        super(detailMessage);
    }

    public ConnectionTimeOutException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public ConnectionTimeOutException(Throwable throwable) {
        super("Connection Time Out Exception", throwable);
    }
}
