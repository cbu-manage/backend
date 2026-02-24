package com.example.cbumanage.controller;

import com.example.cbumanage.authentication.dto.AccessToken;
import com.example.cbumanage.authentication.exceptions.AuthenticationException;
import com.example.cbumanage.authentication.authorization.Permission;
import com.example.cbumanage.dto.MemberCreateDTO;
import com.example.cbumanage.dto.MemberDTO;
import com.example.cbumanage.dto.MemberUpdateDTO;
import com.example.cbumanage.model.CbuMember;
import com.example.cbumanage.model.enums.Role;
import com.example.cbumanage.repository.CbuMemberRepository;
import com.example.cbumanage.service.CbuMemberManageService;
import com.example.cbumanage.service.CbuMemberSyncService;
import com.example.cbumanage.service.CreateLoginEntity;
import com.example.cbumanage.utils.CbuMemberMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClientException;

import java.util.HashSet;
import java.util.List;

@RestController
@RequestMapping("/api/v1/")
@Tag(name = "동아리 회원 관리 컨트롤러", description = "회원 정보를 다루는 컨트롤러입니다.")
public class CbuMemberController {
    private final CbuMemberSyncService cbuMemberSyncService;
    private final CbuMemberManageService cbuMemberManageService;
    private final CbuMemberRepository cbuMemberRepository;
    private final CbuMemberMapper cbuMemberMapper;
    private final CreateLoginEntity createLoginEntity;

    public CbuMemberController(CbuMemberSyncService cbuMemberSyncService, CbuMemberManageService cbuMemberManageService, CbuMemberRepository cbuMemberRepository, CbuMemberMapper cbuMemberMapper, CreateLoginEntity createLoginEntity) {
        this.cbuMemberSyncService = cbuMemberSyncService;         //서비스 참조 선언
        this.cbuMemberManageService = cbuMemberManageService;     //서비스 참조 선언
        this.cbuMemberRepository = cbuMemberRepository;           //레포지토리 참조 선언
        this.cbuMemberMapper = cbuMemberMapper;
        this.createLoginEntity = createLoginEntity;
    }

    @PostMapping("members/sync")
    @Operation(summary = "스프레드시트 -> 데이터베이스 데이터 연동", description = "스프레드시트의 데이터를 데이터베이스에 주입합니다.")
    public String memberSync(final AccessToken accessToken) {
        // 멤버 동기화는 관리자만 수행 가능
        validateAdminAccess(accessToken);
        cbuMemberSyncService.syncMembersFromGoogleSheet();      //스프레드시트에서 데이터베이스로 데이터 값 주입
        return "멤버 저장 성공!";
    }

    @GetMapping("member/{id}")
    @Operation(summary = "원하는 id에 따른 회원정보 취득", description = "id 하나하나에 따른 회원정보를 받아옵니다.")
    @ResponseStatus(HttpStatus.OK)
    public MemberDTO getMember(@PathVariable Long id, AccessToken accessToken) {
        CbuMember cbuMember = cbuMemberRepository.findById(id).orElseThrow();
        return cbuMemberMapper.map(cbuMember);
    }

    @PostMapping("member")
    @Operation(summary = "회원 추가", description = "회원 정보를 데이터베이스에 추가합니다.(데이터베이스 -> 스프레드시트 연동 기능 추가 예정)")
    @ResponseStatus(HttpStatus.CREATED)
    public Long postMember(@RequestBody @Valid MemberCreateDTO memberCreateDTO, AccessToken accessToken){
        // 회원 수동 추가도 관리자만 가능
        validateAdminAccess(accessToken);
        CbuMember member = cbuMemberManageService.createMember(memberCreateDTO);
        return member.getCbuMemberId();
    }

    // TODO : MemberUpdateDTO validation 추가
    @PatchMapping("member")
    @Operation(summary = "데이터베이스의 회원 정보를 변경", description = "데이터베이스의 회원 정보를 변경합니다.(데이터베이스 -> 스프레드시트 연동 기능 추가 예정)")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void patchMember(@RequestBody MemberUpdateDTO memberDTO, AccessToken accessToken) {
        validateAdminAccess(accessToken);
        cbuMemberManageService.updateUser(memberDTO);
    }

    @DeleteMapping("member/{studentNumber}")
    @Operation(summary = "회원 정보 삭제", description = "데이터베이스의 회원 정보를 삭제합니다.(데이터베이스 -> 스프레드시트 연동 기능 추가 예정)")
    public void deleteMember(@PathVariable Long studentNumber, final AccessToken accessToken) {
        // 회원 삭제는 관리자만 가능
        validateAdminAccess(accessToken);
        cbuMemberManageService.deleteMember(studentNumber);
    }

    @GetMapping("members")
    @Operation(summary = "전체 회원 정보 취득", description = "데이터베이스의 모든 회원정보를 받아옵니다.")
    public ResponseEntity<List<MemberDTO>> getMembers(@RequestParam(name = "page", required = false) Integer page, final AccessToken accessToken) {
        validateAdminAccess(accessToken);
        if (page == null) page = 0;
        return ResponseEntity.ok(cbuMemberMapper.map(cbuMemberManageService.getMembers(page)));
    }

    @ExceptionHandler(RestClientException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String restClientException(RestClientException e) {
        return "Fail to request data while RestTemplate";
    }

    @PostMapping("member/createAccount")
    @Operation(summary = "임의 계정 생성", description = "데이터베이스를 기반으로 모든 회원에 대한 임시 계정을 생성합니다.")
    public String createLoginEntity(final AccessToken accessToken){
        // 대량 계정 생성도 관리자만 가능
        validateAdminAccess(accessToken);
        createLoginEntity.initializeLoginEntities();
        return ("임시 계정 생성 성공!");
    }
    private void validateAdminAccess(AccessToken accessToken) {
        if (accessToken == null) {
            throw new AuthenticationException("You don't have permission");
        }

        boolean hasAdminRole = accessToken.getRole() != null && accessToken.getRole().contains(Role.ADMIN);
        boolean hasAdminPermission = accessToken.getPermission() != null
                && Permission.isSatisfiedBy(new HashSet<>(accessToken.getPermission()), Permission.ADMIN);

        if (!hasAdminRole && !hasAdminPermission) {
            throw new AuthenticationException("You don't have permission");
        }
    }
}
