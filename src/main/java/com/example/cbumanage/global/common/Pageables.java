package com.example.cbumanage.global.common;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

/**
 * 목록 API 의 page·size 를 안전한 범위로 맞춰 Pageable 을 만든다.
 *
 * 컨트롤러가 PageRequest.of 를 그대로 부르면 page=-1 이나 size=0 에
 * IllegalArgumentException 이 나고 그게 500 으로 나갔다. 잘못 누른 값 하나에
 * 장애 응답을 주는 대신, 볼 수 있는 범위로 맞춰서 보여준다.
 */
public final class Pageables {

    public static final int DEFAULT_SIZE = 20;
    public static final int MAX_SIZE = 200;

    private Pageables() {
    }

    public static Pageable of(int page, int size, Sort sort) {
        return PageRequest.of(normalizePage(page), normalizeSize(size), sort);
    }

    public static Pageable of(int page, int size) {
        return PageRequest.of(normalizePage(page), normalizeSize(size));
    }

    private static int normalizePage(int page) {
        return Math.max(page, 0);
    }

    private static int normalizeSize(int size) {
        if (size < 1) {
            return DEFAULT_SIZE;
        }
        return Math.min(size, MAX_SIZE);
    }
}
