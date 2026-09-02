package com.example.cbumanage.member.service;

import com.example.cbumanage.dues.repository.DuesRepository;
import com.example.cbumanage.application.repository.ApplicationNotificationRepository;
import com.example.cbumanage.application.service.RecruitmentService;
import com.example.cbumanage.email.service.EmailService;
import com.example.cbumanage.global.error.BaseException;
import com.example.cbumanage.global.error.ErrorCode;
import com.example.cbumanage.member.dto.MemberCreateDTO;
import com.example.cbumanage.member.dto.MemberUpdateDTO;
import com.example.cbumanage.member.util.MemberMapper;
import com.example.cbumanage.user.entity.MemberStatus;
import com.example.cbumanage.user.entity.Role;
import com.example.cbumanage.user.entity.User;
import com.example.cbumanage.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MemberManageServiceTest {

    private final UserRepository userRepository = mock(UserRepository.class);
    private final MemberManageService memberManageService = new MemberManageService(
            userRepository,
            mock(DuesRepository.class),
            mock(MemberMapper.class),
            mock(EmailService.class),
            mock(ApplicationNotificationRepository.class),
            mock(RecruitmentService.class)
    );

    private static Authentication authWith(Role role) {
        return new UsernamePasswordAuthenticationToken(
                "caller", null, List.of(new SimpleGrantedAuthority(role.name())));
    }

    private static MemberUpdateDTO roleChangeTo(Long userId, Role role) {
        return new MemberUpdateDTO(userId, role, null, null, null, null, null, null, null, null, null);
    }

    private User targetWithRole(Long userId, Role role) {
        User user = new User("target@example.com", 20240002L, "encoded-password");
        user.changeRole(role);
        when(userRepository.findByUserIdAndDeletedAtIsNull(userId)).thenReturn(Optional.of(user));
        return user;
    }

    @Test
    void 인원관리는_역할을_바꿀_수_없다() {
        User target = targetWithRole(1L, Role.ROLE_USER);

        assertThatThrownBy(() -> memberManageService.updateUser(
                roleChangeTo(1L, Role.ROLE_PRESIDENT), authWith(Role.ROLE_MEMBER_MANAGER)))
                .isInstanceOf(BaseException.class)
                .extracting(e -> ((BaseException) e).getErrorCode())
                .isEqualTo(ErrorCode.FORBIDDEN);

        assertThat(target.getRole()).isEqualTo(Role.ROLE_USER);
    }

    @Test
    void 인원관리도_역할을_그대로_재전송하면_통과한다() {
        targetWithRole(2L, Role.ROLE_USER);

        // 이름·연락처만 고치는 정상 요청은 role을 그대로 실어 보낸다. 이걸 막으면 명단 관리가 안 된다.
        memberManageService.updateUser(
                roleChangeTo(2L, Role.ROLE_USER), authWith(Role.ROLE_MEMBER_MANAGER));

        verify(userRepository).findByUserIdAndDeletedAtIsNull(2L);
    }

    @Test
    void 회장은_일반_운영진은_임명하지만_부회장은_임명할_수_없다() {
        targetWithRole(3L, Role.ROLE_USER);
        memberManageService.updateUser(
                roleChangeTo(3L, Role.ROLE_MANAGER), authWith(Role.ROLE_PRESIDENT));

        targetWithRole(4L, Role.ROLE_USER);
        assertThatThrownBy(() -> memberManageService.updateUser(
                roleChangeTo(4L, Role.ROLE_VICE_PRESIDENT), authWith(Role.ROLE_PRESIDENT)))
                .isInstanceOf(BaseException.class);
    }

    @Test
    void 개발자_계정은_회장까지_임명할_수_있다() {
        targetWithRole(5L, Role.ROLE_USER);

        memberManageService.updateUser(
                roleChangeTo(5L, Role.ROLE_PRESIDENT), authWith(Role.ROLE_ADMIN));

        verify(userRepository).findByUserIdAndDeletedAtIsNull(5L);
    }

    @Test
    void 회원_등록으로도_역할을_올릴_수_없다() {
        MemberCreateDTO create = mock(MemberCreateDTO.class);
        when(create.getRole()).thenReturn(Role.ROLE_PRESIDENT);

        assertThatThrownBy(() -> memberManageService.createMember(
                create, authWith(Role.ROLE_MEMBER_MANAGER)))
                .isInstanceOf(BaseException.class);

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void deleteMemberMarksUserAsDeletedWithoutHardDeleting() {
        Long studentNumber = 20240001L;
        User user = new User("user@example.com", studentNumber, "encoded-password");
        when(userRepository.findByStudentNumberAndDeletedAtIsNull(studentNumber)).thenReturn(Optional.of(user));

        memberManageService.deleteMember(studentNumber);

        assertThat(user.getDeletedAt()).isNotNull();
        assertThat(user.getMemberStatus()).isEqualTo(MemberStatus.WITHDRAWN);
        verify(userRepository, never()).delete(any(User.class));
    }
}
