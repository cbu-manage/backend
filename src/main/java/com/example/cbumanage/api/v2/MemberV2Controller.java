package com.example.cbumanage.api.v2;

import com.example.cbumanage.api.v2.dto.MemberUpdateV2DTO;
import com.example.cbumanage.api.v2.dto.MemberV2DTO;
import com.example.cbumanage.global.common.ApiResponse;
import com.example.cbumanage.member.dto.MemberCreateDTO;
import com.example.cbumanage.member.dto.MemberUpdateDTO;
import com.example.cbumanage.member.service.MemberManageService;
import com.example.cbumanage.member.exception.MemberNotExistsException;
import com.example.cbumanage.user.entity.User;
import com.example.cbumanage.user.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v2")
@RequiredArgsConstructor
public class MemberV2Controller {

    private final MemberManageService memberManageService;
    private final UserRepository userRepository;

    @GetMapping("member/{userUuid}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_PRESIDENT', 'ROLE_VICE_PRESIDENT', 'ROLE_MEMBER_MANAGER', 'ROLE_TREASURER')")
    public ApiResponse<MemberV2DTO> getMember(@PathVariable UUID userUuid) {
        User user = userRepository.findByUserUuidAndDeletedAtIsNull(userUuid).orElseThrow(MemberNotExistsException::new);
        return ApiResponse.success(MemberV2DTO.from(user));
    }

    @PostMapping("member")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_PRESIDENT', 'ROLE_VICE_PRESIDENT', 'ROLE_MEMBER_MANAGER')")
    public ApiResponse<UUID> postMember(@RequestBody @Valid MemberCreateDTO memberCreateDTO) {
        User member = memberManageService.createMember(memberCreateDTO);
        return ApiResponse.success(member.getUserUuid());
    }

    @PatchMapping("member")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_PRESIDENT', 'ROLE_VICE_PRESIDENT', 'ROLE_MEMBER_MANAGER')")
    public ApiResponse<Void> patchMember(@RequestBody MemberUpdateV2DTO memberDTO) {
        User user = userRepository.findByUserUuidAndDeletedAtIsNull(memberDTO.userUuid())
                .orElseThrow(MemberNotExistsException::new);
        MemberUpdateDTO v1 = new MemberUpdateDTO(
                user.getUserId(),
                memberDTO.role(),
                memberDTO.name(),
                memberDTO.phoneNumber(),
                memberDTO.major(),
                memberDTO.grade(),
                memberDTO.studentNumber(),
                memberDTO.generation(),
                memberDTO.note(),
                memberDTO.kakaoNoti(),
                memberDTO.kakaoChat()
        );
        memberManageService.updateUser(v1);
        return ApiResponse.success();
    }

    @PatchMapping("member/{userUuid}/approve-payment")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_PRESIDENT', 'ROLE_VICE_PRESIDENT', 'ROLE_TREASURER')")
    public ApiResponse<Void> approvePayment(@PathVariable UUID userUuid) {
        User user = userRepository.findByUserUuidAndDeletedAtIsNull(userUuid).orElseThrow(MemberNotExistsException::new);
        memberManageService.approvePayment(user.getUserId());
        return ApiResponse.success();
    }

    @GetMapping("members")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_PRESIDENT', 'ROLE_VICE_PRESIDENT', 'ROLE_MEMBER_MANAGER', 'ROLE_TREASURER')")
    public ApiResponse<List<MemberV2DTO>> getMembers(@RequestParam(name = "page", required = false) Integer page) {
        if (page == null) page = 0;
        return ApiResponse.success(memberManageService.getMembers(page).stream().map(MemberV2DTO::from).toList());
    }
}
