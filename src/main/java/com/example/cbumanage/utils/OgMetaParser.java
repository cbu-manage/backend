package com.example.cbumanage.utils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Component;

/**
 * 외부 URL에서 Open Graph 메타 태그를 파싱하는 유틸리티.
 */
@Component
public class OgMetaParser {

    private static final int TIMEOUT_MS = 5000;

    public record OgMeta(String title, String image, String description) {}

    /**
     * 주어진 URL의 HTML에서 og:title, og:image, og:description을 파싱합니다.
     * 파싱 실패 시 모든 필드가 null인 OgMeta를 반환합니다.
     */
    public OgMeta parse(String url) {
        try {
            Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (compatible; CbuBot/1.0)")
                    .timeout(TIMEOUT_MS)
                    .get();

            String title = doc.select("meta[property=og:title]").attr("content");
            String image = doc.select("meta[property=og:image]").attr("content");
            String description = doc.select("meta[property=og:description]").attr("content");

            return new OgMeta(
                    title.isBlank() ? null : title,
                    image.isBlank() ? null : image,
                    description.isBlank() ? null : description
            );
        } catch (Exception e) {
            return new OgMeta(null, null, null);
        }
    }
}
