package com.example.cbumanage.resource.util;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;

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
        if (!isFetchableUrl(url)) {
            return new OgMeta(null, null, null);
        }
        try {
            Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (compatible; CbuBot/1.0)")
                    .timeout(TIMEOUT_MS)
                    // 리다이렉트를 따라가면 공개 주소로 시작해 내부 주소로 끌려갈 수 있다.
                    .followRedirects(false)
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

    /**
     * 이 주소로 서버가 대신 요청을 보내도 되는지 판단한다.
     * 사용자가 준 주소로 서버가 요청을 내보내는 기능이라, 막지 않으면
     * 사내망 주소나 클라우드 메타데이터 주소를 대신 찔러보는 통로가 된다.
     */
    private boolean isFetchableUrl(String url) {
        if (url == null || url.isBlank()) {
            return false;
        }
        final URI uri;
        try {
            uri = URI.create(url.trim());
        } catch (IllegalArgumentException e) {
            return false;
        }
        String scheme = uri.getScheme();
        if (scheme == null || !(scheme.equalsIgnoreCase("http") || scheme.equalsIgnoreCase("https"))) {
            return false;
        }
        String host = uri.getHost();
        if (host == null || host.isBlank()) {
            return false;
        }
        try {
            // 이름 하나가 여러 주소로 풀릴 수 있으므로 전부 확인한다.
            for (InetAddress address : InetAddress.getAllByName(host)) {
                if (isBlockedAddress(address)) {
                    return false;
                }
            }
        } catch (UnknownHostException e) {
            return false;
        }
        return true;
    }

    private boolean isBlockedAddress(InetAddress address) {
        if (address.isAnyLocalAddress()      // 0.0.0.0, ::
                || address.isLoopbackAddress()   // 127.0.0.0/8, ::1
                || address.isLinkLocalAddress()  // 169.254.0.0/16 (클라우드 메타데이터 포함)
                || address.isSiteLocalAddress()  // 10/8, 172.16/12, 192.168/16
                || address.isMulticastAddress()) {
            return true;
        }
        byte[] octets = address.getAddress();
        if (octets.length == 4) {
            int first = octets[0] & 0xFF;
            int second = octets[1] & 0xFF;
            // 100.64.0.0/10 (통신사 NAT 대역) 과 192.0.0.0/24 (IETF 프로토콜 할당)
            return (first == 100 && second >= 64 && second <= 127)
                    || (first == 192 && second == 0 && (octets[2] & 0xFF) == 0);
        }
        // IPv6 유니크 로컬(fc00::/7)
        return (octets[0] & 0xFE) == 0xFC;
    }
}
