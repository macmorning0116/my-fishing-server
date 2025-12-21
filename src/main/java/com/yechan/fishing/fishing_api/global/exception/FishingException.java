package com.yechan.fishing.fishing_api.global.exception;

public class FishingException extends RuntimeException {

    private final ErrorCode errorCode;

    public FishingException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }
}
