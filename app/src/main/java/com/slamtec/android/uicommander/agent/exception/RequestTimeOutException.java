package com.slamtec.android.uicommander.agent.exception;

/**
 * Created by Alan on 10/20/15.
 */
public class RequestTimeOutException extends Exception {
    public RequestTimeOutException() {
        super("Request Time Out Exception");
    }

    public RequestTimeOutException(String detailMessage) {
        super(detailMessage);
    }

    public RequestTimeOutException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public RequestTimeOutException(Throwable throwable) {
        super("Request Time Out Exception", throwable);
    }
}
