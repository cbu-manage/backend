package com.example.cbumanage.service;

import com.example.cbumanage.dto.ProblemCreateRequestDTO;
import com.example.cbumanage.dto.ProblemResponseDTO;
import com.example.cbumanage.exception.MemberNotExistsException;
import com.example.cbumanage.model.Category;
import com.example.cbumanage.model.CbuMember;
import com.example.cbumanage.model.Platform;
import com.example.cbumanage.model.Problem;
import com.example.cbumanage.repository.CategoryRepository;
import com.example.cbumanage.repository.CbuMemberRepository;
import com.example.cbumanage.repository.PlatformRepository;
import com.example.cbumanage.repository.ProblemRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 코딩 테스트 페이지의 비즈니스 로직을 처리하는 서비스.
 */
@Service
public class ProblemService {

    private final ProblemRepository problemRepository;
    private final CbuMemberRepository cbuMemberRepository;
    private final CategoryRepository categoryRepository;
    private final PlatformRepository platformRepository;

    public ProblemService(ProblemRepository problemRepository, CbuMemberRepository cbuMemberRepository, CategoryRepository categoryRepository, PlatformRepository platformRepository) {
        this.problemRepository = problemRepository;
        this.cbuMemberRepository = cbuMemberRepository;
        this.categoryRepository = categoryRepository;
        this.platformRepository = platformRepository;
    }

    /**
     * 새로운 코딩 테스트 문제 생성.
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
}
