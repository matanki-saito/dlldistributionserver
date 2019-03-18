package com.popush.triela.common.exception;

public class MachineException extends OtherSystemException {
    public MachineException(String message, Throwable cause) {
        super(message, cause);
    }

    public MachineException(String message) {
        super(message);
    }
}
