package com.example.cbumanage.controller;

import com.example.cbumanage.dto.PostDTO;
import com.example.cbumanage.response.ResultResponse;
import com.example.cbumanage.response.SuccessCode;
import com.example.cbumanage.service.StudyService;
import com.example.cbumanage.utils.JwtProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.json.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "스터디 모집 컨트롤러", description = "스터디 모집 게시글 CRUD 및 목록 조회 API")
public class StudyController {

    private final StudyService studyService;
    private final JwtProvider jwtProvider;

    @Autowired
    public StudyController(StudyService studyService, JwtProvider jwtProvider) {
        this.studyService = studyService;
        this.jwtProvider = jwtProvider;
    }

    private Long userIdFromCookie(HttpServletRequest request) {
        String token = null;

        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Cookie not found"
            );
        }

        for (Cookie cookie : cookies) {
            if ("ACCESS_TOKEN".equals(cookie.getName())) {
                token = cookie.getValue();
                break;
            }
        }

        if (token == null || token.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "ACCESS_TOKEN not found"
            );
        }

        Map<String, Object> tokenInfo;
        try {
            tokenInfo = jwtProvider.parseJwt(
                    token,
                    Map.of(
                            "user_id", Long.class,
                            "student_number", Long.class,
                            "role", JSONArray.class,
                            "permissions", JSONArray.class
                    )
            );
        } catch (Exception e) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Invalid JWT token"
            );
        }

        Long userId = (Long) tokenInfo.get("user_id");
        if (userId == null) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "user_id not found in token"
            );
        }
        return userId;
    }

    // 인증이 선택적인 경우 (로그인 안 해도 조회 가능). 로그인 미인증 시 null 반환.
    private Long userIdFromCookieOptional(HttpServletRequest request) {
        try {
            return userIdFromCookie(request);
        } catch (ResponseStatusException e) {
            return null;
        }
    }

    @Operation(
            summary = "스터디 게시글 생성",
            description = "스터디 모집 게시글을 생성합니다. 작성자가 팀장이 되며 게시글과 함께 그룹이 자동으로 생성됩니다."
    )
    @PostMapping("/post/study")
    public ResponseEntity<ResultResponse<PostDTO.PostStudyCreateResponseDTO>> createPostStudy(
            @RequestBody @Valid PostDTO.PostStudyCreateRequestDTO req,
            HttpServletRequest request) {
        Long userId = userIdFromCookie(request);
        PostDTO.PostStudyCreateResponseDTO responseDTO = studyService.createPostStudy(req, userId);
        return ResultResponse.ok(SuccessCode.CREATED, responseDTO);
    }

    @Operation(
            summary = "스터디 게시글 전체 목록 페이징 조회",
            description = "카테고리별 스터디 전체 목록을 페이징 조회합니다. 최신순 정렬."
    )
    @Parameters({
            @Parameter(name = "page", description = "페이지 번호 (0부터 시작)", example = "0"),
            @Parameter(name = "size", description = "한 페이지당 출력 개수", example = "10"),
            @Parameter(name = "category", description = "카테고리 번호", example = "1")
    })
    @GetMapping("/post/study")
    public ResponseEntity<ResultResponse<Page<PostDTO.StudyListDTO>>> getStudies(
            @RequestParam int page,
            @RequestParam int size,
            @RequestParam int category) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Order.desc("post.createdAt")));
        Page<PostDTO.StudyListDTO> studies = studyService.getPostsByCategory(pageable, category);
        return ResultResponse.ok(SuccessCode.SUCCESS, studies);
    }

    @Operation(
            summary = "내가 작성한 스터디 게시글 목록 조회",
            description = "로그인한 사용자가 작성한 스터디 게시글만 조회합니다."
    )
    @Parameters({
            @Parameter(name = "page", description = "페이지 번호 (0부터 시작)", example = "0"),
            @Parameter(name = "size", description = "한 페이지당 출력 개수", example = "10"),
            @Parameter(name = "category", description = "카테고리 번호", example = "1")
    })
    @GetMapping("/post/study/me")
    public ResponseEntity<ResultResponse<Page<PostDTO.StudyListDTO>>> getMyStudies(
            @RequestParam int page,
            @RequestParam int size,
            @RequestParam int category,
            HttpServletRequest request) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Order.desc("post.createdAt")));
        Long userId = userIdFromCookie(request);
        Page<PostDTO.StudyListDTO> studies = studyService.getMyStudiesByUserId(pageable, userId, category);
        return ResultResponse.ok(SuccessCode.SUCCESS, studies);
    }

    @Operation(
            summary = "스터디 게시글 상세 조회",
            description = "스터디 게시글의 상세 정보를 조회합니다. 응답 데이터의 **isLeader**와 **hasApplied** 값에 따라 " +
                    "프론트엔드에서 '신청 인원 확인', '신청하기', '취소하기', '가입완료' 버튼을 분기 처리합니다. " +
                    "비로그인 사용자도 조회 가능합니다."
    )
    @GetMapping("/post/study/{postId}")
    public ResponseEntity<ResultResponse<PostDTO.StudyInfoDetailDTO>> getPostStudy(
            @Parameter(description = "게시글 ID", example = "100") @PathVariable Long postId,
            HttpServletRequest request) {
        Long userId = userIdFromCookieOptional(request);
        PostDTO.StudyInfoDetailDTO studyInfoDetailDTO = studyService.getStudyByPostId(postId, userId);
        return ResultResponse.ok(SuccessCode.SUCCESS, studyInfoDetailDTO);
    }

    @Operation(
            summary = "스터디 게시글 수정",
            description = "제목, 내용, 태그, 스터디명, 최대 인원을 수정합니다. 작성자 본인만 가능합니다. " +
                    "보내지 않은 필드는 기존 값을 유지합니다. 모집 상태는 마감 API를 이용하세요."
    )
    @PatchMapping("/post/study/{postId}")
    public ResponseEntity<ResultResponse<Void>> updateStudy(
            @Parameter(description = "게시글 ID", example = "100") @PathVariable Long postId,
            @RequestBody @Valid PostDTO.PostStudyUpdateRequestDTO req,
            HttpServletRequest request) {
        Long userId = userIdFromCookie(request);
        studyService.updatePostStudy(req, postId, userId);
        return ResultResponse.ok(SuccessCode.UPDATED, null);
    }

    @Operation(
            summary = "스터디 게시글 삭제",
            description = "Soft Delete 처리합니다. 게시글과 함께 생성된 그룹도 함께 삭제됩니다. 작성자 본인만 가능합니다."
    )
    @DeleteMapping("/post/study/{postId}")
    public ResponseEntity<ResultResponse<Void>> deletePost(
            @Parameter(description = "게시글 ID", example = "100") @PathVariable Long postId,
            HttpServletRequest request) {
        Long userId = userIdFromCookie(request);
        studyService.softDeletePost(postId, userId);
        return ResultResponse.ok(SuccessCode.DELETED, null);
    }

    @Operation(
            summary = "스터디 태그별 목록 페이징 조회",
            description = "태그명이 정확히 일치하는 스터디 목록을 조회합니다."
    )
    @Parameters({
            @Parameter(name = "page", description = "페이지 번호 (0부터 시작)", example = "0"),
            @Parameter(name = "size", description = "한 페이지당 출력 개수", example = "10"),
            @Parameter(name = "tag", description = "검색할 태그 문자열 (정확히 일치하는 태그명)", example = "Spring")
    })
    @GetMapping("/post/study/filter")
    public ResponseEntity<ResultResponse<Page<PostDTO.StudyListDTO>>> filterStudiesByTag(
            @RequestParam int page,
            @RequestParam int size,
            @RequestParam(name = "tag") String tag) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Order.desc("id")));
        Page<PostDTO.StudyListDTO> result = studyService.searchByTag(tag, pageable);
        return ResultResponse.ok(SuccessCode.SUCCESS, result);
    }

    @Operation(
            summary = "스터디 모집 마감",
            description = "모집을 마감합니다. PENDING 신청자는 일괄 거절되고 그룹 모집이 종료됩니다. " +
                    "팀장만 가능하며, 최소 1명 이상 수락자(ACTIVE)가 필요합니다."
    )
    @PostMapping("/post/study/{postId}/close")
    public ResponseEntity<ResultResponse<Void>> closeStudyRecruitment(
            @Parameter(description = "게시글 ID", example = "100") @PathVariable Long postId,
            HttpServletRequest request) {
        Long userId = userIdFromCookie(request);
        studyService.closeStudyRecruitment(postId, userId);
        return ResultResponse.ok(SuccessCode.UPDATED, null);
    }
}
