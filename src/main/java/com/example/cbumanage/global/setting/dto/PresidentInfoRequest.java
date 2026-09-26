package com.example.cbumanage.global.setting.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * 활동 내역서(HWP) 하단의 대표자 정보. 전달한 항목만 갱신한다(null 은 기존값 유지).
 * 회장이 바뀔 때마다 템플릿을 고치지 않도록 설정으로 뺀다.
 */
public record PresidentInfoRequest(
        @Size(max = 20, message = "회장 이름은 20자 이내여야 합니다.")
        String presidentName,

        /** 서명 이미지 URL. 업로드(/api/v1/file/image)로 받은 주소를 넣는다. */
        @Pattern(regexp = URL_OR_BLANK, message = "signatureImageUrl 은 http(s):// 로 시작해야 합니다.")
        String signatureImageUrl
) {
    /** 비워서 지우는 것은 허용한다 */
    private static final String URL_OR_BLANK = "^$|^https?://.+";
}
