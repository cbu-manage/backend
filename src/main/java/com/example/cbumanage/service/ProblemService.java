package com.example.cbumanage.service;

import com.example.cbumanage.dto.*;
import com.example.cbumanage.exception.MemberDoesntHavePermissionException;
import com.example.cbumanage.exception.MemberNotExistsException;
import com.example.cbumanage.model.*;
import com.example.cbumanage.repository.*;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import jakarta.persistence.criteria.Predicate;
import java.util.stream.Collectors;

/**
 * 코딩 테스트 문제 관련 비즈니스 로직을 처리하는 서비스입니다.
 */
@Service
@Transactional(readOnly = true)
public class ProblemService {

    private final ProblemRepository problemRepository;
    private final CbuMemberRepository cbuMemberRepository;
    private final CategoryRepository categoryRepository;
    private final PlatformRepository platformRepository;
    private final LanguageRepository languageRepository;
    private final CommentRepository commentRepository;

    public ProblemService(ProblemRepository problemRepository, CbuMemberRepository cbuMemberRepository,
                          CategoryRepository categoryRepository, PlatformRepository platformRepository,
                          LanguageRepository languageRepository, CommentRepository commentRepository) {
        this.problemRepository = problemRepository;
        this.cbuMemberRepository = cbuMemberRepository;
        this.categoryRepository = categoryRepository;
        this.platformRepository = platformRepository;
        this.languageRepository = languageRepository;
        this.commentRepository = commentRepository;
    }

    /**
     * 새로운 코딩 테스트 문제를 생성합니다.
     *
     * @param request  문제 생성에 필요한 데이터 DTO
     * @param memberId 문제를 등록하는 회원 ID
     * @return 생성된 문제 정보 DTO
     */
    @Transactional
    public ProblemResponseDTO createProblem(ProblemCreateRequestDTO request, Long memberId) {
        CbuMember member = cbuMemberRepository.findById(memberId)
                .orElseThrow(() -> new MemberNotExistsException("ID가 " + memberId + "인 회원을 찾을 수 없습니다."));

        List<Category> categories = categoryRepository.findAllById(request.getCategoryIds());
        if (categories.isEmpty()) {
            throw new EntityNotFoundException("유효한 카테고리를 찾을 수 없습니다.");
        }

        Platform platform = platformRepository.findById(request.getPlatformId())
                .orElseThrow(() -> new EntityNotFoundException("ID가 " + request.getPlatformId() + "인 플랫폼을 찾을 수 없습니다."));

        Language language = languageRepository.findById(request.getLanguageId())
                .orElseThrow(() -> new EntityNotFoundException("ID가 " + request.getLanguageId() + "인 언어를 찾을 수 없습니다."));

        Problem problem = Problem.builder()
                .member(member)
                .categories(categories)
                .platform(platform)
                .language(language)
                .title(request.getTitle())
                .content(request.getContent())
                .grade(request.getGrade())
                .problemUrl(request.getProblemUrl())
                .problemStatus(request.getProblemStatus())
                .build();

        Problem savedProblem = problemRepository.save(problem);

        return ProblemResponseDTO.from(savedProblem, 0L);
    }

    /**
     * 특정 ID의 문제를 수정합니다.
     *
     * @param problemId 수정할 문제의 ID
     * @param memberId  수정 요청을 한 회원의 ID
     * @param request   수정할 내용이 담긴 DTO
     * @return 수정된 문제 정보 DTO
     */
    @Transactional
    public ProblemResponseDTO updateProblem(Long problemId, Long memberId, ProblemUpdateRequestDTO request) {
        Problem problem = problemRepository.findById(problemId)
                .orElseThrow(() -> new EntityNotFoundException("ID가 " + problemId + "인 문제를 찾을 수 없습니다."));

        if (!Objects.equals(problem.getMember().getCbuMemberId(), memberId)) {
            throw new MemberDoesntHavePermissionException("이 문제를 수정할 권한이 없습니다.");
        }

        List<Category> categories = (request.getCategoryIds() != null && !request.getCategoryIds().isEmpty())
                ? categoryRepository.findAllById(request.getCategoryIds())
                : null;

        Platform platform = (request.getPlatformId() != null)
                ? platformRepository.findById(request.getPlatformId())
                .orElseThrow(() -> new EntityNotFoundException("ID가 " + request.getPlatformId() + "인 플랫폼을 찾을 수 없습니다."))
                : null;

        Language language = (request.getLanguageId() != null)
                ? languageRepository.findById(request.getLanguageId())
                .orElseThrow(() -> new EntityNotFoundException("ID가 " + request.getLanguageId() + "인 언어를 찾을 수 없습니다."))
                : null;

        problem.update(
                categories,
                platform,
                language,
                request.getTitle(),
                request.getContent(),
                null,
                null,
                request.getGrade(),
                request.getProblemUrl(),
                request.getProblemStatus()
        );

        Long commentCount = commentRepository.countByProblemId(problemId);
        return ProblemResponseDTO.from(problem, commentCount);
    }

    /**
     * 특정 ID의 문제를 삭제합니다.
     *
     * @param problemId 삭제할 문제의 ID
     * @param memberId  삭제 요청을 한 회원의 ID
     */
    @Transactional
    public void deleteProblem(Long problemId, Long memberId) {
        Problem problem = problemRepository.findById(problemId)
                .orElseThrow(() -> new EntityNotFoundException("ID가 " + problemId + "인 문제를 찾을 수 없습니다."));

        if (!Objects.equals(problem.getMember().getCbuMemberId(), memberId)) {
            throw new MemberDoesntHavePermissionException("이 문제를 삭제할 권한이 없습니다.");
        }

        problemRepository.delete(problem);
    }

    /**
     * 전체 문제 목록을 페이지네이션하여 조회합니다.
     * categoryId, platformId로 필터링 가능합니다.
     *
     * @param pageable   페이지네이션 정보
     * @param categoryId 필터링할 카테고리 ID 목록
     * @param platformId 필터링할 플랫폼 ID 목록
     * @return 페이지네이션된 문제 목록 DTO
     */
    public Page<ProblemListItemDTO> getProblems(Pageable pageable, List<Integer> categoryId, List<Integer> platformId) {
        Specification<Problem> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (categoryId != null && !categoryId.isEmpty()) {
                // categories는 ManyToMany이므로 JOIN 후 필터링, distinct로 중복 제거
                Join<Problem, Category> categoryJoin = root.join("categories", JoinType.INNER);
                predicates.add(categoryJoin.get("categoryId").in(categoryId));
                query.distinct(true);
            }
            if (platformId != null && !platformId.isEmpty()) {
                predicates.add(root.get("platform").get("platformId").in(platformId));
            }
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        return problemRepository.findAll(spec, pageable)
                .map(p -> ProblemListItemDTO.from(p, commentRepository.countByProblemId(p.getProblemId())));
    }

    /**
     * 특정 ID의 문제 상세 정보를 조회합니다.
     * 조회 시 viewCount를 1 증가시킵니다.
     *
     * @param problemId 조회할 문제의 ID
     * @return 문제 상세 정보 DTO
     */
    @Transactional
    public ProblemResponseDTO getProblem(Long problemId) {
        Problem problem = problemRepository.findById(problemId)
                .orElseThrow(() -> new EntityNotFoundException("ID가 " + problemId + "인 문제를 찾을 수 없습니다."));

        problemRepository.incrementViewCount(problemId);

        Long commentCount = commentRepository.countByProblemId(problemId);
        return ProblemResponseDTO.from(problem, commentCount);
    }

    /**
     * 모든 카테고리 목록을 조회합니다.
     */
    public List<CategoryResponseDTO> getAllCategories() {
        return categoryRepository.findAll().stream()
                .map(CategoryResponseDTO::from)
                .collect(Collectors.toList());
    }

    /**
     * 모든 플랫폼 목록을 조회합니다.
     */
    public List<PlatformResponseDTO> getAllPlatforms() {
        return platformRepository.findAll().stream()
                .map(PlatformResponseDTO::from)
                .collect(Collectors.toList());
    }

    /**
     * 모든 언어 목록을 조회합니다.
     */
    public List<LanguageResponseDTO> getAllLanguages() {
        return languageRepository.findAll().stream()
                .map(LanguageResponseDTO::from)
                .collect(Collectors.toList());
    }
}
