package com.example.cbumanage.application.entity.enums;

/**
 * 지원 경로입니다. ETC일때만 refSourceEtc로 텍스트를 받습니다.
 */
public enum RefSource {
    EVERYTIME,  // 에브리타임
    INSTAGRAM,  // 인스타
    FRIEND,     //지인 추천
    ETC         // 기타, 별도 텍스트 필수
}
