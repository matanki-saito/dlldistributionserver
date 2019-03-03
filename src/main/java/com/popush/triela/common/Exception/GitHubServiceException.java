package com.popush.triela.common.Exception;

public class GitHubServiceException extends OtherSystemException {
    public GitHubServiceException(String message, Throwable cause) {
        super(message, cause);
    }

    public GitHubServiceException(String message) {
        super(message);
    }
}
