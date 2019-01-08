package com.popush.triela.common.Exception;

public class GitHubException extends Exception {
    public GitHubException(String message, Throwable cause) {
        super(message, cause);
    }

    public GitHubException(String message) {
        super(message);
    }
}
