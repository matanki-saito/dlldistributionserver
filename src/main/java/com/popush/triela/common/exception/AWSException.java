package com.popush.triela.common.exception;

public class AWSException extends OtherSystemException {
    public AWSException(String message, Throwable cause) {
        super(message, cause);
    }

    public AWSException(String message) {
        super(message);
    }
}
