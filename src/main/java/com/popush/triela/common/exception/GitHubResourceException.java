package com.popush.triela.common.exception;

public class GitHubResourceException extends OtherSystemException {
    public GitHubResourceException(String message, Throwable cause) {
        super(message, cause);
    }

    public GitHubResourceException(String message) {
        super(message);
    }
}
