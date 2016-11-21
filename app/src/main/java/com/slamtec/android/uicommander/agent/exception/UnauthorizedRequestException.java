package com.slamtec.android.uicommander.agent.exception;

/**
 * Created by Alan on 10/20/15.
 */
public class UnauthorizedRequestException extends Exception {
    public UnauthorizedRequestException() {
        super("Unauthorized Request Exception");
    }

    public UnauthorizedRequestException(String detailMessage) {
        super(detailMessage);
    }

    public UnauthorizedRequestException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public UnauthorizedRequestException(Throwable throwable) {
        super("Unauthorized Request Exception", throwable);
    }
}
