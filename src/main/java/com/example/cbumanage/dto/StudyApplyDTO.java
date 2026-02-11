package com.example.cbumanage.dto;

import com.example.cbumanage.model.enums.StudyApplyStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 스터디 신청 관련 DTO
 */
public class StudyApplyDTO {

    /**
     * 스터디 신청 정보 DTO (응답용)
     */
    @Getter
    @NoArgsConstructor
    @Schema(description = "스터디 신청 정보")
    public static class StudyApplyInfoDTO {
        @Schema(description = "신청 ID")
        private Long applyId;

        @Schema(description = "스터디 ID")
        private Long studyId;

        @Schema(description = "신청자 ID")
        private Long applicantId;

        @Schema(description = "신청자 이름")
        private String applicantName;

        @Schema(description = "신청자 학과")
        private String major;

        @Schema(description = "신청자 학년")
        private String grade;

        @Schema(description = "신청 상태")
        private StudyApplyStatus status;

        @Schema(description = "신청 일시")
        private LocalDateTime createdAt;

        @Builder
        public StudyApplyInfoDTO(Long applyId, Long studyId, Long applicantId,
                                  String applicantName, String major, String grade,
                                  StudyApplyStatus status, LocalDateTime createdAt) {
            this.applyId = applyId;
            this.studyId = studyId;
            this.applicantId = applicantId;
            this.applicantName = applicantName;
            this.major = major;
            this.grade = grade;
            this.status = status;
            this.createdAt = createdAt;
        }
    }

    /**
     * 스터디 신청 상태 변경 요청 DTO
     */
    @Getter
    @NoArgsConstructor
    @Schema(description = "스터디 신청 상태 변경 요청")
    public static class StudyApplyStatusRequestDTO {
        @Schema(description = "변경할 상태 (ACCEPTED, REJECTED)")
        private StudyApplyStatus status;
    }

}
