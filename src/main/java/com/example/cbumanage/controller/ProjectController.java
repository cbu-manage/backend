package com.example.cbumanage.controller;

import com.example.cbumanage.dto.PostDTO;
import com.example.cbumanage.model.enums.ProjectFieldType;
import com.example.cbumanage.response.ResultResponse;
import com.example.cbumanage.response.SuccessCode;
import com.example.cbumanage.service.ProjectService;
import com.example.cbumanage.utils.JwtProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.json.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "프로젝트 관리 컨트롤러")
public class ProjectController {
    private final ProjectService projectService;
    private final JwtProvider jwtProvider;


    @Autowired
    public ProjectController(ProjectService projectService, JwtProvider jwtProvider) {
        this.projectService = projectService;
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
            summary = "프로젝트 게시글 생성",
            description = "프로젝트 관련 정보를 입력받아 새로운 프로젝트 게시글을 생성합니다."
    )
    @PostMapping("/post/project")
    public ResponseEntity<ResultResponse<PostDTO.PostProjectCreateResponseDTO>> createPostProject(
            @RequestBody PostDTO.PostProjectCreateRequestDTO req, HttpServletRequest request) {
        Long userId = userIdFromCookie(request);
        PostDTO.PostProjectCreateResponseDTO responseDTO =
                projectService.createPostProject(req, userId);
        return ResultResponse.ok(SuccessCode.CREATED, responseDTO);
    }

    @Operation(
            summary = "프로젝트 게시글 전체 목록 페이징 조회",
            description = "프로젝트 전체 목록을 페이징으로 불러옵니다"
    )
    @GetMapping("/post/project")
    public ResponseEntity<ResultResponse<Page<PostDTO.ProjectListDTO>>> getProjects(@io.swagger.v3.oas.annotations.Parameter(description = "페이지번호") @RequestParam int page,
                                                                              @io.swagger.v3.oas.annotations.Parameter(description = "페이지당 project 게시글 갯수") @RequestParam int size,
                                                                              @Parameter(description = "카테고리") @RequestParam int category,
                                                                                    @Parameter(description = "모집여부") @RequestParam(required = false) Boolean recruiting){

        Pageable pageable= PageRequest.of(
                page,size, Sort.by(Sort.Order.desc("post.createdAt"))
        );
        Page<PostDTO.ProjectListDTO> posts=projectService.getPostsByCategory(pageable,recruiting,category);
        return ResultResponse.ok(SuccessCode.SUCCESS, posts);
    }

    @Operation(
            summary = "내가 작성한 프로젝트 게시글 목록 조회",
            description = "쿠키에서 userId를 가져와서 자신이 작성한 프로젝트 게시글만 조회 합니다."
    )
    @GetMapping("/post/project/me")
    public ResponseEntity<ResultResponse<Page<PostDTO.ProjectListDTO>>> getMyProjects(@io.swagger.v3.oas.annotations.Parameter(description = "페이지번호") @RequestParam int page,
                                                                                    @io.swagger.v3.oas.annotations.Parameter(description = "페이지당 project 게시글 갯수") @RequestParam int size,
                                                                                    @Parameter(description = "카테고리") @RequestParam int category,
    HttpServletRequest request){
        Pageable pageable= PageRequest.of(
                page,size, Sort.by(Sort.Order.desc("post.createdAt"))
        );

        Long userId = userIdFromCookie(request);
        Page<PostDTO.ProjectListDTO> posts=projectService.getMyProjectsByUserId(pageable, userId,category);
        return ResultResponse.ok(SuccessCode.SUCCESS, posts);
    }

    @Operation(
            summary = "프로젝트 게시글 상세 조회",
            description = "post id를 이용하여 프로젝트 게시글 상세 정보를 조회합니다."
    )
    @GetMapping("/post/project/{postId}")
    public ResponseEntity<ResultResponse<PostDTO.ProjectInfoDetailDTO>> getPostProject(@PathVariable Long postId) {
        PostDTO.ProjectInfoDetailDTO projectInfoDetailDTO = projectService.getProjectByPostId(postId);
        return ResultResponse.ok(SuccessCode.SUCCESS, projectInfoDetailDTO);
    }

    @Operation(
            summary = "프로젝트 게시글 수정",
            description = "프로젝트 게시글 ID를 기준으로 메인 테이블인 post와 서브테이블인 프로젝트 정보를 수정합니다."
    )
    @PatchMapping("/post/project/{postId}")
    public ResponseEntity<ResultResponse<Void>> updateProject(
            @PathVariable Long postId,
            @RequestBody PostDTO.PostProjectUpdateRequestDTO req,
            HttpServletRequest request) {

        Long userId = userIdFromCookie(request);

        projectService.updatePostProject(req, postId, userId);

        return ResultResponse.ok(SuccessCode.UPDATED, null);
    }

    @Operation(
            summary = "프로젝트 게시글 삭제",
            description = "프로젝트 게시글을 softDelete해 포스트 읽어오기에서 필터링 되도록 합니다"
    )
    @DeleteMapping("/post/project/{postId}")
    public ResponseEntity<ResultResponse<Void>> deletePost(
            @PathVariable Long postId,
            HttpServletRequest request) {

        Long userId = userIdFromCookie(request);

        projectService.softDeletePost(postId, userId);

        return ResultResponse.ok(SuccessCode.DELETED, null);
    }

    @Operation(
            summary = "프로젝트 모집분야 카테고리별로 목록 페이징 조회",
            description = "프로젝트 모집분야 카테고리별로 조회가 가능하며 모집분야가 여러개일 경우 그중에 하나라도" +
                    "포함이 된다면 조회가 가능합니다."
    )
    @GetMapping("/post/project/filter")
    public ResponseEntity<ResultResponse<Page<PostDTO.ProjectListDTO>>> filterProjectsByFields(
            @io.swagger.v3.oas.annotations.Parameter(description = "페이지번호") @RequestParam int page,
            @io.swagger.v3.oas.annotations.Parameter(description = "페이지당 project 게시글 갯수") @RequestParam int size,
            @RequestParam(name = "fields") ProjectFieldType fields,
            @RequestParam(required = false) Boolean recruiting) {
        Pageable pageable= PageRequest.of(page,size, Sort.by(Sort.Order.desc("id")));
        Page<PostDTO.ProjectListDTO> result = projectService.searchByField(fields, recruiting, pageable);

        return ResultResponse.ok(SuccessCode.SUCCESS,result);
    }
}
