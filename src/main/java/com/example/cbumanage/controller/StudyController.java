package com.example.cbumanage.controller;

import com.example.cbumanage.dto.GroupDTO;
import com.example.cbumanage.dto.PostDTO;
import com.example.cbumanage.dto.StudyApplyDTO;
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

import java.util.List;
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

    @Operation(
            summary = "스터디 게시글 생성",
            description = "스터디 모집 게시글을 생성합니다. 작성자가 팀장이 됩니다."
    )
    @PostMapping("/post/study")
    public ResponseEntity<ResultResponse<PostDTO.PostStudyCreateResponseDTO>> createPostStudy(
            @RequestBody PostDTO.PostStudyCreateRequestDTO req,
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
            description = "스터디 게시글의 상세 정보(스터디명, 태그, 최대 인원, 모집 여부 등)를 조회합니다."
    )
    @GetMapping("/post/study/{postId}")
    public ResponseEntity<ResultResponse<PostDTO.StudyInfoDetailDTO>> getPostStudy(
            @Parameter(description = "게시글 ID", example = "100") @PathVariable Long postId) {
        PostDTO.StudyInfoDetailDTO studyInfoDetailDTO = studyService.getStudyByPostId(postId);
        return ResultResponse.ok(SuccessCode.SUCCESS, studyInfoDetailDTO);
    }

    @Operation(
            summary = "스터디 게시글 수정",
            description = "제목, 내용, 태그, 모집여부를 수정합니다. 작성자 본인만 가능합니다."
    )
    @PatchMapping("/post/study/{postId}")
    public ResponseEntity<ResultResponse<Void>> updateStudy(
            @Parameter(description = "게시글 ID", example = "100") @PathVariable Long postId,
            @RequestBody PostDTO.PostStudyUpdateRequestDTO req,
            HttpServletRequest request) {
        Long userId = userIdFromCookie(request);
        studyService.updatePostStudy(req, postId, userId);
        return ResultResponse.ok(SuccessCode.UPDATED, null);
    }

    @Operation(
            summary = "스터디 게시글 삭제",
            description = "Soft Delete 처리합니다. 작성자 본인만 가능합니다."
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
            summary = "스터디 참가 신청",
            description = "모집 중인 스터디에 참가 신청합니다. 중복 신청, 본인 스터디 신청은 불가합니다."
    )
    @PostMapping("/post/study/{postId}/apply")
    public ResponseEntity<ResultResponse<StudyApplyDTO.StudyApplyInfoDTO>> applyStudy(
            @Parameter(description = "게시글 ID", example = "100") @PathVariable Long postId,
            HttpServletRequest request) {
        Long userId = userIdFromCookie(request);
        StudyApplyDTO.StudyApplyInfoDTO result = studyService.applyStudy(postId, userId);
        return ResultResponse.ok(SuccessCode.CREATED, result);
    }

    @Operation(
            summary = "스터디 신청 목록 조회",
            description = "스터디 신청자 목록을 조회합니다. 팀장만 조회 가능합니다."
    )
    @GetMapping("/post/study/{postId}/apply")
    public ResponseEntity<ResultResponse<List<StudyApplyDTO.StudyApplyInfoDTO>>> getApplicants(
            @Parameter(description = "게시글 ID", example = "100") @PathVariable Long postId,
            HttpServletRequest request) {
        Long userId = userIdFromCookie(request);
        List<StudyApplyDTO.StudyApplyInfoDTO> result = studyService.getApplicants(postId, userId);
        return ResultResponse.ok(SuccessCode.SUCCESS, result);
    }

    @Operation(
            summary = "스터디 신청 수락/거절",
            description = "팀장이 신청자를 수락(ACCEPTED) 또는 거절(REJECTED)합니다. 최대 인원 초과 시 수락 불가합니다."
    )
    @PatchMapping("/post/study/{postId}/apply/{applyId}")
    public ResponseEntity<ResultResponse<StudyApplyDTO.StudyApplyInfoDTO>> updateApplyStatus(
            @Parameter(description = "게시글 ID", example = "100") @PathVariable Long postId,
            @Parameter(description = "신청 ID", example = "5") @PathVariable Long applyId,
            @RequestBody StudyApplyDTO.StudyApplyStatusRequestDTO req,
            HttpServletRequest request) {
        Long userId = userIdFromCookie(request);
        StudyApplyDTO.StudyApplyInfoDTO result = studyService.updateApplyStatus(
                postId, applyId, req.getStatus(), userId);
        return ResultResponse.ok(SuccessCode.UPDATED, result);
    }

    @Operation(
            summary = "스터디 모집 마감",
            description = "모집을 마감하고 그룹을 자동 생성합니다. 수락된 신청자들이 그룹 멤버로 등록됩니다. " +
                    "팀장만 가능하며, 최소 1명 이상 수락자가 필요합니다."
    )
    @PostMapping("/post/study/{postId}/close")
    public ResponseEntity<ResultResponse<GroupDTO.GroupCreateResponseDTO>> closeStudyRecruitment(
            @Parameter(description = "게시글 ID", example = "100") @PathVariable Long postId,
            HttpServletRequest request) {
        Long userId = userIdFromCookie(request);
        GroupDTO.GroupCreateResponseDTO result = studyService.closeStudyRecruitment(postId, userId);
        return ResultResponse.ok(SuccessCode.CREATED, result);
    }
}
