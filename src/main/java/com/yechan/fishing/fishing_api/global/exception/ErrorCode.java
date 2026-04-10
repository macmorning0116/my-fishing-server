package com.yechan.fishing.fishing_api.global.exception;

public enum ErrorCode {

  // 공용
  INVALID_COORD("INVALID_COORD", "위도/경도가 올바르지 않습니다."),
  INTERNAL_SERVER_ERROR("INTERNAL_SERVER_ERROR", "서버 오류가 발생했습니다."),
  USER_NOT_FOUND("USER_NOT_FOUND", "사용자를 찾을 수 없습니다."),
  AUTH_PROVIDER_NOT_SUPPORTED("AUTH_PROVIDER_NOT_SUPPORTED", "지원하지 않는 로그인 제공자입니다."),
  AUTH_SOCIAL_USER_INFO_ERROR("AUTH_SOCIAL_USER_INFO_ERROR", "소셜 사용자 정보를 불러오는 중 에러가 발생했습니다."),
  AUTH_LOGIN_REQUIRED("AUTH_LOGIN_REQUIRED", "로그인이 필요합니다."),
  AUTH_INVALID_TOKEN("AUTH_INVALID_TOKEN", "유효하지 않은 토큰입니다."),
  AUTH_INVALID_REFRESH_TOKEN("AUTH_INVALID_REFRESH_TOKEN", "유효하지 않은 리프레시 토큰입니다."),
  AUTH_REFRESH_TOKEN_NOT_FOUND("AUTH_REFRESH_TOKEN_NOT_FOUND", "리프레시 토큰을 찾을 수 없습니다."),
  AUTH_USER_INACTIVE("AUTH_USER_INACTIVE", "활성 상태의 사용자가 아닙니다."),

  // 네이버
  NAVER_API_ERROR("NAVER_API_ERROR", "주소 변환에 실패했습니다."),

  // 날씨
  WEATHER_API_ERROR("WEATHER_API_ERROR", "날씨 정보를 불러오는 과정 중 에러가 발생했습니다."),
  WEATHER_API_INVALID_DATA("WEATHER_API_INVALID_DATA", "날씨DATA가 올바르지 않습니다."),

  // GPT
  GPT_API_ERROR("GPT_API_ERROR", "GPT 응답을 불러오는 중 에러가 발생했습니다."),
  GPT_RESPONSE_PARSE_ERROR("GPT_RESPONSE_PARSE_ERROR", "GPT 응답을 파싱하는 과정에서 에러가 발생했습니다."),
  COMMUNITY_POST_NOT_FOUND("COMMUNITY_POST_NOT_FOUND", "게시글을 찾을 수 없습니다."),
  COMMUNITY_COMMENT_NOT_FOUND("COMMUNITY_COMMENT_NOT_FOUND", "댓글을 찾을 수 없습니다."),
  COMMUNITY_REPORT_DUPLICATE("COMMUNITY_REPORT_DUPLICATE", "이미 신고한 대상입니다."),
  COMMUNITY_INVALID_PARENT_COMMENT(
      "COMMUNITY_INVALID_PARENT_COMMENT", "다른 게시글의 댓글에는 답글을 작성할 수 없습니다."),
  COMMUNITY_IMAGE_COUNT_EXCEEDED("COMMUNITY_IMAGE_COUNT_EXCEEDED", "이미지는 최대 5장까지 업로드할 수 있습니다."),
  COMMUNITY_INVALID_IMAGE_FILE("COMMUNITY_INVALID_IMAGE_FILE", "이미지 파일만 업로드할 수 있습니다."),
  COMMUNITY_IMAGE_UPLOAD_ERROR("COMMUNITY_IMAGE_UPLOAD_ERROR", "이미지를 저장하는 중 에러가 발생했습니다."),
  COMMUNITY_IMAGE_METADATA_ERROR("COMMUNITY_IMAGE_METADATA_ERROR", "이미지 메타데이터를 읽는 중 에러가 발생했습니다."),
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
