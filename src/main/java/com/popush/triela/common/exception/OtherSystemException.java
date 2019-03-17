package com.popush.triela.common.exception;

/**
 * 自分は悪くないとき
 * 連携しているシステム（IO、ネット越しのサービス、通信）などの異常
 */
public class OtherSystemException extends Exception {
    public OtherSystemException(String message, Throwable cause) {
        super(message, cause);
    }

    public OtherSystemException(String message) {
        super(message);
    }
}
