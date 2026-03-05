package com.example.cbumanage.utils;

import com.example.cbumanage.dto.StudyApplyDTO;
import com.example.cbumanage.model.StudyApply;
import org.springframework.stereotype.Component;

@Component
public class StudyApplyMapper {

    public StudyApplyDTO.StudyApplyInfoDTO toStudyApplyInfoDTO(StudyApply apply) {
        return StudyApplyDTO.StudyApplyInfoDTO.builder()
                .applyId(apply.getId())
                .studyId(apply.getStudy().getId())
                .applicantId(apply.getApplicant().getCbuMemberId())
                .applicantName(apply.getApplicant().getName())
                .major(apply.getApplicant().getMajor())
                .grade(apply.getApplicant().getGrade())
                .status(apply.getStatus())
                .createdAt(apply.getCreatedAt())
                .build();
    }
}
