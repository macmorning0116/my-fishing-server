package com.yechan.fishing.fishing_api.global.exception;

public enum ErrorCode {

    // 공용
    INVALID_COORD("INVALID_COORD", "위도/경도가 올바르지 않습니다."),
    INTERNAL_SERVER_ERROR("INTERNAL_SERVER_ERROR", "서버 오류가 발생했습니다."),

    // 네이버
    NAVER_API_ERROR("NAVER_API_ERROR", "주소 변환에 실패했습니다."),

    // 날씨
    WEATHER_API_ERROR("WEATHER_API_ERROR", "날씨 정보를 불러오는 과정 중 에러가 발생했습니다."),
    WEATHER_API_INVALID_DATA("WEATHER_API_INVALID_DATA", "날씨DATA가 올바르지 않습니다."),

    // GPT
    GPT_API_ERROR("GPT_API_ERROR", "GPT 응답을 불러오는 중 에러가 발생했습니다."),
    GPT_RESPONSE_PARSE_ERROR("GPT_RESPONSE_PARSE_ERROR", "GPT 응답을 파싱하는 과정에서 에러가 발생했습니다."),
    ;

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
