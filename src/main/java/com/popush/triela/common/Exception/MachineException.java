package com.popush.triela.common.Exception;

public class MachineException extends Exception {
    public MachineException(String message, Throwable cause) {
        super(message, cause);
    }

    public MachineException(String message) {
        super(message);
    }
}
