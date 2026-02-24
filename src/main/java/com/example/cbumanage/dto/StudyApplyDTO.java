package com.example.cbumanage.dto;

import com.example.cbumanage.model.enums.StudyApplyStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
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
    @Schema(description = "스터디 신청 정보 (응답용)")
    public static class StudyApplyInfoDTO {
        @Schema(description = "신청 ID", example = "5")
        private Long applyId;

        @Schema(description = "스터디 ID", example = "10")
        private Long studyId;

        @Schema(description = "신청자 회원 ID", example = "20")
        private Long applicantId;

        @Schema(description = "신청자 이름", example = "홍길동")
        private String applicantName;

        @Schema(description = "신청자 학과", example = "컴퓨터공학과")
        private String major;

        @Schema(description = "신청자 학년", example = "3학년")
        private String grade;

        @Schema(description = "신청 상태 (PENDING: 대기, ACCEPTED: 수락, REJECTED: 거절)", example = "PENDING")
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
        @NotNull(message = "상태 값은 필수입니다.")
        @Schema(description = "변경할 상태 (ACCEPTED: 수락, REJECTED: 거절)",
                example = "ACCEPTED",
                allowableValues = {"ACCEPTED", "REJECTED"})
        private StudyApplyStatus status;
    }

}
