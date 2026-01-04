package com.example.cbumanage.controller;

import com.example.cbumanage.authentication.dto.AccessToken;
import com.example.cbumanage.dto.ProblemCreateRequestDTO;
import com.example.cbumanage.dto.ProblemResponseDTO;
import com.example.cbumanage.service.ProblemService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 코딩 테스트 문제 관련 컨트롤러입니다.
 */
@RestController
@RequestMapping("/api/v1/problems")
public class ProblemController {

    private final ProblemService problemService;

    public ProblemController(ProblemService problemService) {
        this.problemService = problemService;
    }

    /**
     * 새로운 코딩 테스트 문제를 등록합니다.
     *
     * @param accessToken 인증된 사용자의 토큰 정보
     * @param request 문제 생성 요청 데이터
     * @return 생성된 문제 정보
     */
    @PostMapping
    public ResponseEntity<ProblemResponseDTO> createProblem(AccessToken accessToken, @Valid @RequestBody ProblemCreateRequestDTO request) {
        Long memberId = accessToken.getUserId();
        ProblemResponseDTO response = problemService.createProblem(request, memberId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
