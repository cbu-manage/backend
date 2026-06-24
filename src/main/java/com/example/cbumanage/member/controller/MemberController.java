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
@Tag(name = "회원 관리", description = "동아리 회원 정보를 조회·등록·수정·삭제하고 회비 승인 상태를 관리합니다.")
@RequiredArgsConstructor
public class MemberController {
    private final MemberManageService memberManageService;
    private final UserRepository userRepository;
    private final MemberMapper memberMapper;

    @GetMapping("member/{id}")
    @Operation(summary = "회원 상세 조회", description = "회원 ID로 회원 상세 정보를 조회합니다.")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_PRESIDENT', 'ROLE_VICE_PRESIDENT', 'ROLE_MEMBER_MANAGER', 'ROLE_TREASURER')")
    public ApiResponse<MemberDTO> getMember(@PathVariable Long id) {
        User user = userRepository.findByUserIdAndDeletedAtIsNull(id).orElseThrow();
        return ApiResponse.success(memberMapper.map(user));
    }

    @PostMapping("member")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_PRESIDENT', 'ROLE_VICE_PRESIDENT', 'ROLE_MEMBER_MANAGER')")
    @Operation(summary = "회원 등록", description = "관리자가 회원 정보를 등록합니다.")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<Long> postMember(@RequestBody @Valid MemberCreateDTO memberCreateDTO) {
        User member = memberManageService.createMember(memberCreateDTO);
        return ApiResponse.success(member.getUserId());
    }

    @PatchMapping("member")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_PRESIDENT', 'ROLE_VICE_PRESIDENT', 'ROLE_MEMBER_MANAGER')")
    @Operation(summary = "회원 정보 수정", description = "관리자가 회원 정보를 수정합니다.")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ApiResponse<Void> patchMember(@RequestBody MemberUpdateDTO memberDTO) {
        memberManageService.updateUser(memberDTO);
        return ApiResponse.success();
    }

    @PatchMapping("member/{id}/approve-payment")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_PRESIDENT', 'ROLE_VICE_PRESIDENT', 'ROLE_TREASURER')")
    @Operation(summary = "회비 확인 후 회원 승인", description = "회비 납부 확인 후 회원 상태를 ACTIVE로 변경하고 오픈채팅/디스코드 링크 안내 메일을 발송합니다.")
    public ApiResponse<Void> approvePayment(@PathVariable Long id) {
        memberManageService.approvePayment(id);
        return ApiResponse.success();
    }

    @DeleteMapping("member/{studentNumber}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_PRESIDENT', 'ROLE_VICE_PRESIDENT', 'ROLE_MEMBER_MANAGER')")
    @Operation(summary = "회원 삭제", description = "회원 정보를 소프트 삭제합니다.")
    public ApiResponse<Void> deleteMember(@PathVariable Long studentNumber) {
        memberManageService.deleteMember(studentNumber);
        return ApiResponse.success();
    }

    @GetMapping("members")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_PRESIDENT', 'ROLE_VICE_PRESIDENT', 'ROLE_MEMBER_MANAGER', 'ROLE_TREASURER')")
    @Operation(summary = "회원 목록 조회", description = "회원 목록을 페이지 단위로 조회합니다.")
    public ApiResponse<List<MemberDTO>> getMembers(@RequestParam(name = "page", required = false) Integer page) {
        if (page == null) page = 0;
        return ApiResponse.success(memberMapper.map(memberManageService.getMembers(page)));
    }
}
