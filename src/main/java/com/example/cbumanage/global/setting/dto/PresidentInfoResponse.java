package com.example.cbumanage.global.setting.dto;

/** 활동 내역서 하단에 찍히는 대표자 정보 */
public record PresidentInfoResponse(
        String presidentName,
        String signatureImageUrl
) {}
