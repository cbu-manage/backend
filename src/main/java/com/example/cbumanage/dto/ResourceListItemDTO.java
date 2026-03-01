package com.example.cbumanage.dto;

import com.example.cbumanage.model.Resource;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 자료방 목록 조회 응답 DTO.
 * 자료방 목록에서 보여줄 정보(제목, 작성자, 작성 시간, 링크)만 포함합니다.
 */
@Getter
public class ResourceListItemDTO {

    private final Long resourceId;
    private final String title;

    /** 작성자 이름 */
    private final String authorName;

    /** 작성자 기수 */
    private final Long generation;

    /** 클릭 시 이동할 외부 링크 */
    private final String link;

    private final LocalDateTime createdAt;

    @Builder
    public ResourceListItemDTO(Long resourceId, String title, String authorName,
                               Long generation, String link, LocalDateTime createdAt) {
        this.resourceId = resourceId;
        this.title = title;
        this.authorName = authorName;
        this.generation = generation;
        this.link = link;
        this.createdAt = createdAt;
    }

    /**
     * Resource 엔티티를 ResourceListItemDTO로 변환합니다.
     */
    public static ResourceListItemDTO from(Resource resource) {
        return ResourceListItemDTO.builder()
                .resourceId(resource.getResourceId())
                .title(resource.getTitle())
                .authorName(resource.getMember().getName())
                .generation(resource.getMember().getGeneration())
                .link(resource.getLink())
                .createdAt(resource.getCreatedAt())
                .build();
    }
}
