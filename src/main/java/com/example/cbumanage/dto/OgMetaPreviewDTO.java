package com.example.cbumanage.dto;

import com.example.cbumanage.utils.OgMetaParser.OgMeta;
import lombok.Getter;

/**
 * OG 메타 미리보기 응답 DTO.
 * 자료 등록 전 URL에서 추출한 OG 정보를 프론트에 전달합니다.
 */
@Getter
public class OgMetaPreviewDTO {

    private final String ogTitle;
    private final String ogImage;
    private final String ogDescription;

    public OgMetaPreviewDTO(OgMeta ogMeta) {
        this.ogTitle = ogMeta.title();
        this.ogImage = ogMeta.image();
        this.ogDescription = ogMeta.description();
    }
}
