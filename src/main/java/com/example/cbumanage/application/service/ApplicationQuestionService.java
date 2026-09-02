package com.example.cbumanage.application.service;

import com.example.cbumanage.application.dto.ApplicationQuestionCreateRequest;
import com.example.cbumanage.application.dto.ApplicationQuestionResponse;
import com.example.cbumanage.application.dto.ApplicationQuestionUpdateRequest;
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
    private final RecruitmentGenerationPolicy generationPolicy;

    @Transactional(readOnly = true)
    public List<ApplicationQuestionResponse> getCurrentQuestions() {
        Long generation = recruitmentRepository.findFirstByStatus(RecruitmentStatus.OPEN)
                .map(Recruitment::getGeneration)
                .orElseGet(generationPolicy::currentGeneration);
        return getQuestions(generation).stream()
                .map(ApplicationQuestionResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ApplicationQuestionResponse> getQuestionsInRecruitment(String recruitmentUuid) {
        return getQuestions(resolveGeneration(recruitmentUuid)).stream()
                .map(ApplicationQuestionResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ApplicationQuestion> getQuestions(Long generation) {
        return applicationQuestionRepository.findByGenerationAndDeletedAtIsNullOrderBySortOrderAsc(generation);
    }

    /**
     * 모집 회차에 새 질문 추가. sortOrder 미지정 시 해당 기수 마지막 순번 다음으로 자동 배정.
     */
    @Transactional
    public ApplicationQuestionResponse createQuestion(String recruitmentUuid, ApplicationQuestionCreateRequest request) {
        Long generation = resolveGeneration(recruitmentUuid);
        if (applicationQuestionRepository.existsByGenerationAndType(generation, request.type())) {
            throw new BaseException(ErrorCode.QUESTION_TYPE_DUPLICATED);
        }
        int sortOrder = request.sortOrder() != null
                ? request.sortOrder()
                : applicationQuestionRepository.findMaxSortOrderByGeneration(generation).orElse(0) + 1;
        assertSortOrderFree(generation, sortOrder, null);

        ApplicationQuestion question = ApplicationQuestion.builder()
                .generation(generation)
                .type(request.type())
                .question(request.question())
                .description(request.description())
                .isRequired(request.isRequired())
                .sortOrder(sortOrder)
                .build();
        return ApplicationQuestionResponse.from(applicationQuestionRepository.save(question));
    }

    /**
     * 질문 수정. 전달한 필드만 갱신.
     */
    @Transactional
    public ApplicationQuestionResponse updateQuestion(String recruitmentUuid, String questionUuid,
                                                       ApplicationQuestionUpdateRequest request) {
        ApplicationQuestion question = getQuestionInRecruitment(recruitmentUuid, questionUuid);
        // 화면에서 불러온 뒤 남이 먼저 저장했으면 덮어쓰지 않고 되돌려준다
        if (request.version() != null && !request.version().equals(question.getVersion())) {
            throw new BaseException(ErrorCode.CONCURRENT_MODIFICATION);
        }
        if (request.sortOrder() != null) {
            assertSortOrderFree(question.getGeneration(), request.sortOrder(), question.getQuestionUuid());
        }
        question.update(request.question(), request.description(), request.isRequired(), request.sortOrder());
        // flush 전에는 version이 증가 전 값이라, 응답을 그대로 들고 다음 저장을 하면 바로 409가 난다
        applicationQuestionRepository.flush();
        return ApplicationQuestionResponse.from(question);
    }

    /**
     * 질문 삭제 (soft delete). 이미 제출된 지원서의 답변(questionSnapshot)에는 영향 없음.
     */
    @Transactional
    public void deleteQuestion(String recruitmentUuid, String questionUuid) {
        ApplicationQuestion question = getQuestionInRecruitment(recruitmentUuid, questionUuid);
        question.softDelete();
    }

    /**
     * 같은 순서를 둘이 쓰면 지원자 화면의 문항 차례가 매번 달라질 수 있다.
     * 화면에는 이미 막아뒀지만 API를 직접 부르면 통과했다.
     */
    private void assertSortOrderFree(Long generation, int sortOrder, String selfUuid) {
        boolean taken = getQuestions(generation).stream()
                .anyMatch(other -> other.getSortOrder() == sortOrder
                        && !other.getQuestionUuid().equals(selfUuid));
        if (taken) {
            throw new BaseException(ErrorCode.QUESTION_SORT_ORDER_DUPLICATED);
        }
    }

    private Long resolveGeneration(String recruitmentUuid) {
        return recruitmentRepository.findByRecruitmentUuid(recruitmentUuid)
                .orElseThrow(() -> new BaseException(ErrorCode.RECRUITMENT_NOT_FOUND))
                .getGeneration();
    }

    // recruitmentUuid가 가리키는 기수 소속의 질문인지까지 확인 (다른 회차 질문을 잘못된 uuid 조합으로 수정/삭제하는 것 방지)
    private ApplicationQuestion getQuestionInRecruitment(String recruitmentUuid, String questionUuid) {
        Long generation = resolveGeneration(recruitmentUuid);
        ApplicationQuestion question = applicationQuestionRepository.findByQuestionUuid(questionUuid)
                .orElseThrow(() -> new BaseException(ErrorCode.QUESTION_NOT_FOUND));
        if (question.isDeleted() || !question.getGeneration().equals(generation)) {
            throw new BaseException(ErrorCode.QUESTION_NOT_FOUND);
        }
        return question;
    }
}
