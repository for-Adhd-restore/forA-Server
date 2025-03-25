package com.project.foradhd.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

import static org.springframework.http.HttpStatus.*;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    //auth
    NOT_SUPPORTED_SNS_TYPE(BAD_REQUEST, "지원하지 않는 SNS 타입입니다."),
    INVALID_AUTH_TOKEN(UNAUTHORIZED, "유효하지 않은 인증 토큰입니다."),
    NOT_MATCH_USERNAME_PASSWORD(UNAUTHORIZED, "아이디 또는 비밀번호가 일치하지 않습니다."),

    //user
    NOT_FOUND_USER(NOT_FOUND, "존재하지 않는 유저입니다."),
    NOT_FOUND_USER_PROFILE(NOT_FOUND, "존재하지 않는 유저 프로필입니다."),
    NOT_FOUND_USER_PRIVACY(NOT_FOUND, "존재하지 않는 유저 개인정보입니다."),
    ALREADY_EXISTS_EMAIL(CONFLICT, "이미 가입한 이메일입니다."),
    ALREADY_EXISTS_NICKNAME(CONFLICT, "이미 사용 중인 닉네임입니다."),
    NOT_FOUND_TERMS(NOT_FOUND, "존재하지 않는 이용약관입니다."),
    NOT_FOUND_PUSH_NOTIFICATION_APPROVAL(NOT_FOUND, "존재하지 않는 푸시 알림 동의 항목입니다."),
    REQUIRED_TERMS_APPROVAL(BAD_REQUEST, "필수 이용 약관에 동의해야 합니다."),
    EMAIL_AUTH_TIMEOUT(BAD_REQUEST, "이메일 인증 가능 시간을 초과했습니다."),

    //hospital
    NOT_FOUND_HOSPITAL(NOT_FOUND, "존재하지 않는 병원입니다."),
    NOT_FOUND_DOCTOR(NOT_FOUND, "존재하지 않는 의사입니다."),
    NOT_FOUND_HOSPITAL_RECEIPT_REVIEW(NOT_FOUND, "존재하지 않는 영수증 리뷰입니다."),
    NOT_FOUND_HOSPITAL_EVALUATION_REVIEW(NOT_FOUND, "존재하지 않는 포에이리본 병원 평가 리뷰입니다."),
    NOT_FOUND_HOSPITAL_EVALUATION_QUESTION(NOT_FOUND, "존재하지 않는 포에이리본 병원 평가 질문입니다."),
    REQUIRED_HOSPITAL_EVALUATION_ANSWER(BAD_REQUEST, "포에이리본 병원 평가 필수 답변이 누락되었습니다."),
    FORBIDDEN_HOSPITAL_RECEIPT_REVIEW(FORBIDDEN, "영수증 리뷰에 접근할 수 있는 권한이 없습니다."),
    FORBIDDEN_HOSPITAL_EVALUATION_REVIEW(FORBIDDEN, "포에이리본 병원 평가 리뷰에 접근할 수 있는 권한이 없습니다."),
    ALREADY_EXISTS_HOSPITAL_RECEIPT_REVIEW(CONFLICT, "이미 작성한 영수증 리뷰가 있습니다."),
    ALREADY_EXISTS_HOSPITAL_EVALUATION_REVIEW(CONFLICT, "이미 작성한 포에이리본 병원 평가 리뷰가 있습니다."),
    INVALID_HOSPITAL_REVIEW_TYPE(INTERNAL_SERVER_ERROR, "유효하지 않은 병원 리뷰 타입입니다."),

    //medicine
    NOT_FOUND_MEDICINE(NOT_FOUND, "존재하지 않는 약입니다."),
    JSON_PARSE_ERROR(BAD_REQUEST,"파싱 에러가 발생했습니다"),

    //medicine_review
    NOT_FOUND_MEDICINE_REVIEW(NOT_FOUND, "존재하지 않는 약 리뷰입니다."),
    FORBIDDEN_MEDICINE_REVIEW(FORBIDDEN, "약 리뷰에 접근할 수 있는 권한이 없습니다."),
    //S3
    EMPTY_FILE(BAD_REQUEST, "유효한 파일이어야 합니다."),
    NOT_FOUND_FILE_EXTENSION(BAD_REQUEST, "파일 확장자가 존재하지 않습니다."),
    INVALID_FILE_EXTENSION(BAD_REQUEST, "지원하지 않는 파일 확장자입니다."),
    INVALID_FILE_SIZE(BAD_REQUEST, "최대 허용 파일 용량을 초과하였습니다."),
    FILE_UPLOAD_ERROR(INTERNAL_SERVER_ERROR, "파일 업로드에 실패했습니다."),

    //validation
    INVALID_REQUEST(BAD_REQUEST, "잘못된 요청입니다."),

    //system
    SYSTEM_ERROR(INTERNAL_SERVER_ERROR, "시스템 에러입니다."),

    //board
    NOT_FOUND_POST(NOT_FOUND, "존재하지 않는 게시글입니다."),
    ACCESS_DENIED(FORBIDDEN, "접근 권한이 없습니다."),

    //comment
    NOT_FOUND_COMMENT(NOT_FOUND, "존재하지 않는 댓글입니다."),
    NOT_FOUND_COMMENT_LIKE(NOT_FOUND, "존재하지 않는 댓글 좋아요입니다."),
    ALREADY_LIKED_COMMENT(CONFLICT, "이미 좋아요한 댓글입니다."),

    // notification
    NOT_FOUND_NOTIFICATION(NOT_FOUND, "존재하지 않는 알림입니다."),
    PASSWORD_SAME_AS_PREVIOUS(BAD_REQUEST, "기존 비밀번호와 동일한 비밀번호는 사용할 수 없습니다.");
    private final HttpStatus status;
    private final String message;
}
