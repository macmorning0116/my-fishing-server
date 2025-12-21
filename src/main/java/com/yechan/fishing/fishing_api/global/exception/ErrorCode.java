package com.yechan.fishing.fishing_api.global.exception;

public enum ErrorCode {

    INVALID_COORD("INVALID_COORD", "위도/경도가 올바르지 않습니다."),
    NAVER_API_ERROR("NAVER_API_ERROR", "주소 변환에 실패했습니다."),
    INTERNAL_SERVER_ERROR("INTERNAL_SERVER_ERROR", "서버 오류가 발생했습니다.");

    private final String code;
    private final String message;

    ErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

}
