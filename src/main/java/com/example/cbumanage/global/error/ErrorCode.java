package com.example.cbumanage.global.error;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

/*
에러를 처리하는 코드입니다
 */
@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    INVALID_REQUEST("E-COMMON-0001", "잘못된 요청입니다. ", HttpStatus.BAD_REQUEST),
    NOT_FOUND("E-COMMON-0002", "리소스를 찾을 수 없음", HttpStatus.NOT_FOUND),
    DUPLICATE_RESOURCE("E-COMMON-0003", "중복 리소스", HttpStatus.CONFLICT),
    NOT_ALLOWED_FILETYPE("E-COMMON-0004","잘못된 파일 타입", HttpStatus.CONFLICT),
    FILE_SIZE_EXCEEDED("E-COMMON-0006","파일 크기가 제한을 초과했습니다 (최대 10MB)", HttpStatus.BAD_REQUEST),
    FILE_PROCESS_FAILED("E-COMMON-0007","파일 처리에 실패했습니다. 파일이 손상되었거나 올바르지 않습니다.", HttpStatus.BAD_REQUEST),
    ALREADY_JOINED_MEMBER("E-COMMON-0005","이미 가입된 멤버",HttpStatus.CONFLICT),

    UNAUTHORIZED("E-AUTH-0001", "인증이 필요합니다. ", HttpStatus.UNAUTHORIZED),
    FORBIDDEN("E-AUTH-0002", "권한이 없습니다. ", HttpStatus.FORBIDDEN),
    USER_NOT_FOUND("E-AUTH-0004", "사용자를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    INVALID_PASSWORD("E-AUTH-0005", "비밀번호가 일치하지 않습니다.", HttpStatus.UNAUTHORIZED),
    MEMBER_NOT_APPROVED("E-AUTH-0006", "회비 확인 및 관리자 승인이 완료되지 않았습니다.", HttpStatus.FORBIDDEN),
    INVALID_EMAIL_DOMAIN("E-AUTH-0007", "학교 이메일(@tukorea.ac.kr)만 사용할 수 있습니다.", HttpStatus.BAD_REQUEST),
    EMAIL_AUTH_FAILED("E-AUTH-0008", "이메일 인증이 완료되지 않았습니다.", HttpStatus.UNAUTHORIZED),

    //그룹 에러 코드
    GROUP_NOT_FOUND("E-GROUP-0001", "그룹을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    GROUP_MEMBER_NOT_FOUND("E-GROUP-0002", "그룹 멤버를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    NOT_GROUP_LEADER("E-GROUP-0003", "그룹 리더가 아닙니다.", HttpStatus.FORBIDDEN),
    GROUP_NOT_RECRUITING("E-GROUP-0004", "모집 중인 그룹이 아닙니다.", HttpStatus.BAD_REQUEST),

    //POST 공용
    POST_NOT_FOUND("E-POST-0001", "게시글을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),

    //소식 게시판
    NEWS_NOT_FOUND("E-NEWS-0001", "소식 게시글을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    NEWS_INVALID_POST_CATEGORY("E-NEWS-0002", "뉴스 카테고리 게시글만 소식으로 생성할 수 있습니다.", HttpStatus.BAD_REQUEST),
    NEWS_ATTACHMENT_NOT_FOUND("E-NEWS-0003", "소식 첨부파일을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    NEWS_ATTACHMENT_TYPE_NOT_ALLOWED("E-NEWS-0004", "허용되지 않는 첨부파일 형식입니다.", HttpStatus.BAD_REQUEST),
    NEWS_ATTACHMENT_SIZE_EXCEEDED("E-NEWS-0005", "첨부파일 크기가 제한을 초과했습니다 (최대 20MB).", HttpStatus.BAD_REQUEST),
    //모임
    GATHERING_NOT_FOUND("E-GATHERING-0001", "모임을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    VOTE_CLOSED("E-GATHERING-0002", "투표가 마감되었습니다.", HttpStatus.BAD_REQUEST),
    GATHERING_TYPE_IMMUTABLE("E-GATHERING-0003", "모임 유형은 변경할 수 없습니다. 삭제 후 다시 생성해주세요.", HttpStatus.BAD_REQUEST),

    // 회원가입 신청서 에러 코드
    APPLICATION_NOT_FOUND("E-APP-0001", "신청서를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    APPLICATION_DUPLICATED("E-APP-0002", "이미 신청서가 존재합니다.", HttpStatus.CONFLICT),
    APPLICATION_DECIDED("E-APP-0003", "수정기한이 지났습니다. 최종 결정된 신청서 입니다.", HttpStatus.BAD_REQUEST),
    APPLICATION_CANCELLED("E-APP-0004", "이미 취소된 신청서 입니다.", HttpStatus.BAD_REQUEST),
    INVALID_PIN("E-APP-0005", "PIN번호가 올바르지 않습니다.", HttpStatus.UNAUTHORIZED),
    INVALID_APPLICATION_STATUS("E-APP-0006", "현재 상태에서 허용되지 않는 작업입니다.", HttpStatus.BAD_REQUEST),
    QUESTION_NOT_FOUND("E-APP-0007", "질문을 찾을 수 없습니다", HttpStatus.NOT_FOUND),
    FAIL_REASON_REQUIRED("E-APP-0009", "탈락 사유는 필수입니다. ", HttpStatus.BAD_REQUEST),
    ACCEPTED_APPLICATION_NOT_FOUND("E-APP-0010", "합격자 중 존재하지 않습니다.", HttpStatus.NOT_FOUND),
    VOTE_NOT_FOUND("E-APP-0011", "투표 내역이 없습니다.", HttpStatus.NOT_FOUND),
    VOTING_NOT_COMPLETED("E-APP-0012", "아직 투표가 완료되지 않은 신청서가 있습니다.", HttpStatus.BAD_REQUEST),
    UNDECIDED_APPLICATION_EXISTS("E-APP-0013", "최종 결정되지 않은(보류) 신청서가 있습니다.", HttpStatus.BAD_REQUEST),
    REQUIRED_ANSWER_MISSING("E-APP-0014", "필수 답변이 누락되었습니다.", HttpStatus.BAD_REQUEST),

    // 모집(Recruitment) 에러 코드
    RECRUITMENT_NOT_FOUND("E-REC-0001", "모집 정보를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    RECRUITMENT_ALREADY_OPEN("E-REC-0002", "이미 진행 중인 모집이 있습니다.", HttpStatus.CONFLICT),
    RECRUITMENT_DUPLICATED("E-REC-0003", "이미 존재하는 모집 기수입니다.", HttpStatus.CONFLICT),
    RECRUITMENT_ALREADY_CLOSED("E-REC-0004", "이미 마감된 모집입니다.", HttpStatus.BAD_REQUEST);

    private final String code;
    private final String message;
    private final HttpStatus httpStatus;

}

