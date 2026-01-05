package com.example.cbumanage.service;

import com.example.cbumanage.dto.*;
import com.example.cbumanage.exception.MemberNotExistsException;
import com.example.cbumanage.model.Category;
import com.example.cbumanage.model.CbuMember;
import com.example.cbumanage.model.Platform;
import com.example.cbumanage.model.Problem;
import com.example.cbumanage.repository.*;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
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

    public ProblemService(ProblemRepository problemRepository, CbuMemberRepository cbuMemberRepository, CategoryRepository categoryRepository, PlatformRepository platformRepository, LanguageRepository languageRepository) {
        this.problemRepository = problemRepository;
        this.cbuMemberRepository = cbuMemberRepository;
        this.categoryRepository = categoryRepository;
        this.platformRepository = platformRepository;
        this.languageRepository = languageRepository;
    }

    /**
     * 새로운 코딩 테스트 문제를 생성합니다.
     *
     * @param request 문제 생성에 필요한 데이터 DTO
     * @param memberId 문제를 등록하는 회원 ID
     * @return 생성된 문제 정보 DTO
     * @throws MemberNotExistsException 회원을 찾을 수 없는 경우
     * @throws EntityNotFoundException 카테고리 또는 플랫폼을 찾을 수 없는 경우
     */
    @Transactional
    public ProblemResponseDTO createProblem(ProblemCreateRequestDTO request, Long memberId) {
        CbuMember member = cbuMemberRepository.findById(memberId)
                .orElseThrow(() -> new MemberNotExistsException("ID가 " + memberId + "인 회원을 찾을 수 없습니다."));

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new EntityNotFoundException("ID가 " + request.getCategoryId() + "인 카테고리를 찾을 수 없습니다."));

        Platform platform = platformRepository.findById(request.getPlatformId())
                .orElseThrow(() -> new EntityNotFoundException("ID가 " + request.getPlatformId() + "인 플랫폼을 찾을 수 없습니다."));

        Problem problem = Problem.builder()
                .member(member)
                .category(category)
                .platform(platform)
                .title(request.getTitle())
                .content(request.getContent())
                .inputDescription(request.getInputDescription())
                .outputDescription(request.getOutputDescription())
                .grade(request.getGrade())
                .build();

        Problem savedProblem = problemRepository.save(problem);

        return ProblemResponseDTO.from(savedProblem);
    }

    /**
     * 전체 문제 목록을 페이지네이션하여 조회합니다.
     *
     * @param pageable 페이지네이션 정보
     * @return 페이지네이션된 문제 목록 DTO
     */
    public Page<ProblemListItemDTO> getProblems(Pageable pageable) {
        return problemRepository.findAll(pageable).map(ProblemListItemDTO::from);
    }

    /**
     * 특정 ID의 문제 상세 정보를 조회합니다.
     *
     * @param problemId 조회할 문제의 ID
     * @return 문제 상세 정보 DTO
     * @throws EntityNotFoundException 해당 ID의 문제를 찾을 수 없는 경우
     */
    public ProblemResponseDTO getProblem(Integer problemId) {
        Problem problem = problemRepository.findById(problemId)
                .orElseThrow(() -> new EntityNotFoundException("ID가 " + problemId + "인 문제를 찾을 수 없습니다."));
        return ProblemResponseDTO.from(problem);
    }

    /**
     * 모든 카테고리 목록을 조회합니다.
     *
     * @return 카테고리 목록 DTO
     */
    public List<CategoryResponseDTO> getAllCategories() {
        return categoryRepository.findAll().stream()
                .map(CategoryResponseDTO::from)
                .collect(Collectors.toList());
    }

    /**
     * 모든 플랫폼 목록을 조회합니다.
     *
     * @return 플랫폼 목록 DTO
     */
    public List<PlatformResponseDTO> getAllPlatforms() {
        return platformRepository.findAll().stream()
                .map(PlatformResponseDTO::from)
                .collect(Collectors.toList());
    }

    /**
     * 모든 언어 목록을 조회합니다.
     *
     * @return 언어 목록 DTO
     */
    public List<LanguageResponseDTO> getAllLanguages() {
        return languageRepository.findAll().stream()
                .map(LanguageResponseDTO::from)
                .collect(Collectors.toList());
    }
}
