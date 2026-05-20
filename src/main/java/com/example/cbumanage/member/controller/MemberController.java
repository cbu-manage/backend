package com.example.cbumanage.member.controller;

import com.example.cbumanage.global.common.ApiResponse;
import com.example.cbumanage.member.dto.MemberCreateDTO;
import com.example.cbumanage.member.dto.MemberDTO;
import com.example.cbumanage.member.dto.MemberUpdateDTO;
import com.example.cbumanage.member.service.MemberManageService;
import com.example.cbumanage.member.util.MemberMapper;
import com.example.cbumanage.user.entity.User;
import com.example.cbumanage.user.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/")
@Tag(name = "동아리 회원 관리 컨트롤러", description = "회원 정보를 다루는 컨트롤러입니다.")
@RequiredArgsConstructor
public class MemberController {
    private final MemberManageService memberManageService;
    private final UserRepository userRepository;
    private final MemberMapper memberMapper;

    @GetMapping("member/{id}")
    @Operation(summary = "원하는 id에 따른 회원정보 취득", description = "id 하나하나에 따른 회원정보를 받아옵니다.")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ApiResponse<MemberDTO> getMember(@PathVariable Long id) {
        User user = userRepository.findByUserIdAndDeletedAtIsNull(id).orElseThrow();
        return ApiResponse.success(memberMapper.map(user));
    }

    @PostMapping("member")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(summary = "회원 추가", description = "회원 정보를 데이터베이스에 추가합니다.")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<Long> postMember(@RequestBody @Valid MemberCreateDTO memberCreateDTO) {
        User member = memberManageService.createMember(memberCreateDTO);
        return ApiResponse.success(member.getUserId());
    }

    @PatchMapping("member")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(summary = "데이터베이스의 회원 정보를 변경", description = "데이터베이스의 회원 정보를 변경합니다.")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ApiResponse<Void> patchMember(@RequestBody MemberUpdateDTO memberDTO) {
        memberManageService.updateUser(memberDTO);
        return ApiResponse.success();
    }

    @DeleteMapping("member/{studentNumber}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(summary = "회원 정보 삭제", description = "데이터베이스의 회원 정보를 삭제합니다.")
    public ApiResponse<Void> deleteMember(@PathVariable Long studentNumber) {
        memberManageService.deleteMember(studentNumber);
        return ApiResponse.success();
    }

    @GetMapping("members")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(summary = "전체 회원 정보 취득", description = "데이터베이스의 모든 회원정보를 받아옵니다.")
    public ApiResponse<List<MemberDTO>> getMembers(@RequestParam(name = "page", required = false) Integer page) {
        if (page == null) page = 0;
        return ApiResponse.success(memberMapper.map(memberManageService.getMembers(page)));
    }
}
