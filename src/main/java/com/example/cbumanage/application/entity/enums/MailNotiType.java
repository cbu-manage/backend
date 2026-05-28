package com.example.cbumanage.application.entity.enums;

/**
 * 발송한 메일의 종류를 템플릿화 했습니다.
 * 메일을 발송할 때, 어떤 템플릿으로 발송할지 구분하기 위해 사용됩니다.
 */
public enum MailNotiType {
    ACCEPTED,   // ACCEPTED: 지원이 승인되었을 때 발송되는 메일
    REJECTED    // REJECTED: 지원이 거절되었을 때 발송되는 메일
}
