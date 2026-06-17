package com.example.cbumanage.application.service;

import com.example.cbumanage.application.entity.ApplicationQuestion;
import com.example.cbumanage.application.entity.Recruitment;
import com.example.cbumanage.application.entity.enums.RecruitmentStatus;
import com.example.cbumanage.application.repository.ApplicationQuestionRepository;
import com.example.cbumanage.application.repository.RecruitmentRepository;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

class ApplicationQuestionServiceTest {

    private final ApplicationQuestionRepository applicationQuestionRepository =
            mock(ApplicationQuestionRepository.class);
    private final RecruitmentRepository recruitmentRepository = mock(RecruitmentRepository.class);
    private final RecruitmentGenerationPolicy generationPolicy = new RecruitmentGenerationPolicy(
            Clock.fixed(Instant.parse("2026-06-17T00:00:00Z"), ZoneId.of("Asia/Seoul")),
            2026,
            RecruitmentGenerationPolicy.RecruitmentSeason.SUMMER_BREAK,
            29);
    private final ApplicationQuestionService applicationQuestionService =
            new ApplicationQuestionService(applicationQuestionRepository, recruitmentRepository, generationPolicy);

    @Test
    void getCurrentQuestionsReturnsExistingQuestionsWithoutCreatingDefaults() {
        Recruitment recruitment = Recruitment.open(40L, 3);
        List<ApplicationQuestion> existingQuestions = List.of(
                requiredQuestion("몰입 경험", 1),
                requiredQuestion("프로그래밍 시작 계기", 2),
                requiredQuestion("씨부엉 지원 목적", 3)
        );
        when(recruitmentRepository.findFirstByStatus(RecruitmentStatus.OPEN))
                .thenReturn(Optional.of(recruitment));
        when(applicationQuestionRepository.findByGenerationAndDeletedAtIsNullOrderBySortOrderAsc(40L))
                .thenReturn(existingQuestions);

        var questions = applicationQuestionService.getCurrentQuestions();

        assertThat(questions)
                .extracting(com.example.cbumanage.application.dto.ApplicationQuestionResponse::question)
                .containsExactly("몰입 경험", "프로그래밍 시작 계기", "씨부엉 지원 목적");
        verify(applicationQuestionRepository, never()).saveAll(org.mockito.ArgumentMatchers.anyList());
    }

    @Test
    void getCurrentQuestionsUsesSeasonGenerationWhenRecruitmentIsNotOpened() {
        List<ApplicationQuestion> existingQuestions = List.of(requiredQuestion("몰입 경험", 1));
        when(recruitmentRepository.findFirstByStatus(RecruitmentStatus.OPEN))
                .thenReturn(Optional.empty());
        when(applicationQuestionRepository.findByGenerationAndDeletedAtIsNullOrderBySortOrderAsc(29L))
                .thenReturn(existingQuestions);

        var questions = applicationQuestionService.getCurrentQuestions();

        assertThat(questions)
                .extracting(com.example.cbumanage.application.dto.ApplicationQuestionResponse::question)
                .containsExactly("몰입 경험");
    }

    private ApplicationQuestion requiredQuestion(String question, Integer sortOrder) {
        return ApplicationQuestion.builder()
                .generation(40L)
                .question(question)
                .isRequired(true)
                .sortOrder(sortOrder)
                .build();
    }
}
