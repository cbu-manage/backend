package com.example.cbumanage.api.v2.dto;

import com.example.cbumanage.post.dto.PostDTO;
import com.example.cbumanage.report.entity.enums.PostReportGroupType;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class PostReportV2DTO {

    public record CreateRequest(
            String title,
            String content,
            String location,
            LocalDateTime date,
            String reportImage,
            String reportFile,
            int category,
            long groupId,
            List<UUID> memberUuids,
            PostReportGroupType type,
            String reflection,
            String nextPlan
    ) {
    }

    public record UpdateRequest(
            String title,
            String content,
            String location,
            String reportImage,
            String reportFile,
            LocalDateTime date,
            long groupId,
            PostReportGroupType type,
            List<UUID> memberUuids,
            String reflection,
            String nextPlan
    ) {
    }

    public static PostDTO.PostReportCreateRequestDTO toV1(CreateRequest request, List<Long> memberIds) {
        return new PostDTO.PostReportCreateRequestDTO(
                request.title(),
                request.content(),
                request.location(),
                request.date(),
                request.reportImage(),
                request.reportFile(),
                request.category(),
                request.groupId(),
                memberIds,
                request.type(),
                request.reflection(),
                request.nextPlan()
        );
    }

    public static PostDTO.PostReportUpdateRequestDTO toV1(UpdateRequest request, List<Long> memberIds) {
        return new PostDTO.PostReportUpdateRequestDTO(
                request.title(),
                request.content(),
                request.location(),
                request.reportImage(),
                request.reportFile(),
                request.date(),
                request.groupId(),
                request.type(),
                memberIds,
                request.reflection(),
                request.nextPlan()
        );
    }
}
