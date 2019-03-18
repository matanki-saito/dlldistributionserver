package com.popush.triela.common.exception;

public class ArgumentException extends Exception {
    public ArgumentException(String message, Throwable cause, Object... bads) {
        super(message, cause);
    }

    public ArgumentException(String message, Object... bads) {
        super(message);
    }


    public ArgumentException(String message, Throwable cause) {
        super(message, cause);
    }

    public ArgumentException(String message) {
        super(message);
    }
}
