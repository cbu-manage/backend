package com.example.cbumanage.application.service;

import com.example.cbumanage.application.dto.ApplicationQuestionResponse;
import com.example.cbumanage.application.entity.ApplicationQuestion;
import com.example.cbumanage.application.entity.Recruitment;
import com.example.cbumanage.application.entity.enums.RecruitmentStatus;
import com.example.cbumanage.application.repository.ApplicationQuestionRepository;
import com.example.cbumanage.application.repository.RecruitmentRepository;
import com.example.cbumanage.global.error.BaseException;
import com.example.cbumanage.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ApplicationQuestionService {

    private final ApplicationQuestionRepository applicationQuestionRepository;
    private final RecruitmentRepository recruitmentRepository;

    @Transactional(readOnly = true)
    public List<ApplicationQuestionResponse> getCurrentQuestions() {
        Recruitment recruitment = recruitmentRepository.findFirstByStatus(RecruitmentStatus.OPEN)
                .orElseThrow(() -> new BaseException(ErrorCode.RECRUITMENT_NOT_FOUND));
        return getQuestions(recruitment.getGeneration()).stream()
                .map(ApplicationQuestionResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ApplicationQuestion> getQuestions(Long generation) {
        return applicationQuestionRepository.findByGenerationAndDeletedAtIsNullOrderBySortOrderAsc(generation);
    }
}
