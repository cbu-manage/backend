package com.example.cbumanage.member.controller;

import com.example.cbumanage.member.dto.MemberCreateDTO;
import com.example.cbumanage.member.dto.MemberDTO;
import com.example.cbumanage.member.dto.MemberUpdateDTO;
import com.example.cbumanage.member.entity.CbuMember;
import com.example.cbumanage.member.repository.CbuMemberRepository;
import com.example.cbumanage.member.service.CbuMemberManageService;
import com.example.cbumanage.member.service.CbuMemberSyncService;
import com.example.cbumanage.member.util.CbuMemberMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClientException;

import java.util.List;

@RestController
@RequestMapping("/api/v1/")
@Tag(name = "동아리 회원 관리 컨트롤러", description = "회원 정보를 다루는 컨트롤러입니다.")
@RequiredArgsConstructor
public class CbuMemberController {
    private final CbuMemberSyncService cbuMemberSyncService;
    private final CbuMemberManageService cbuMemberManageService;
    private final CbuMemberRepository cbuMemberRepository;
    private final CbuMemberMapper cbuMemberMapper;

    @PostMapping("members/sync")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(summary = "스프레드시트 -> 데이터베이스 데이터 연동", description = "스프레드시트의 데이터를 데이터베이스에 주입합니다.")
    public String memberSync() {
        cbuMemberSyncService.syncMembersFromGoogleSheet();
        return "멤버 저장 성공!";
    }

    @GetMapping("member/{id}")
    @Operation(summary = "원하는 id에 따른 회원정보 취득", description = "id 하나하나에 따른 회원정보를 받아옵니다.")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public MemberDTO getMember(@PathVariable Long id) {
        CbuMember cbuMember = cbuMemberRepository.findById(id).orElseThrow();
        return cbuMemberMapper.map(cbuMember);
    }

    @PostMapping("member")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(summary = "회원 추가", description = "회원 정보를 데이터베이스에 추가합니다.")
    @ResponseStatus(HttpStatus.CREATED)
    public Long postMember(@RequestBody @Valid MemberCreateDTO memberCreateDTO) {
        CbuMember member = cbuMemberManageService.createMember(memberCreateDTO);
        return member.getCbuMemberId();
    }

    @PatchMapping("member")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(summary = "데이터베이스의 회원 정보를 변경", description = "데이터베이스의 회원 정보를 변경합니다.")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void patchMember(@RequestBody MemberUpdateDTO memberDTO) {
        cbuMemberManageService.updateUser(memberDTO);
    }

    @DeleteMapping("member/{studentNumber}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(summary = "회원 정보 삭제", description = "데이터베이스의 회원 정보를 삭제합니다.")
    public void deleteMember(@PathVariable Long studentNumber) {
        cbuMemberManageService.deleteMember(studentNumber);
    }

    @GetMapping("members")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(summary = "전체 회원 정보 취득", description = "데이터베이스의 모든 회원정보를 받아옵니다.")
    public ResponseEntity<List<MemberDTO>> getMembers(@RequestParam(name = "page", required = false) Integer page) {
        if (page == null) page = 0;
        return ResponseEntity.ok(cbuMemberMapper.map(cbuMemberManageService.getMembers(page)));
    }

    @ExceptionHandler(RestClientException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String restClientException(RestClientException e) {
        return "Fail to request data while RestTemplate";
    }
}
