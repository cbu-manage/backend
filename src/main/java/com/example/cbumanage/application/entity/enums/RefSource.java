package com.example.cbumanage.application.entity.enums;

/**
 * 지원 경로입니다. ETC일때만 refSourceEtc로 텍스트를 받습니다.
 */
public enum RefSource {
    SNS,        // SNS
    FRIEND,     // 지인 추천
    POSTER,     // 포스터/현수막
    WEBSITE,    // 홈페이지
    ETC         // 기타, 별도 텍스트 필수
}
