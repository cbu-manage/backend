package com.example.cbumanage.member.service;

import com.example.cbumanage.dues.repository.DuesRepository;
import com.example.cbumanage.application.repository.ApplicationNotificationRepository;
import com.example.cbumanage.email.service.EmailService;
import com.example.cbumanage.member.util.MemberMapper;
import com.example.cbumanage.user.entity.MemberStatus;
import com.example.cbumanage.user.entity.User;
import com.example.cbumanage.user.repository.UserRepository;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
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
            mock(ApplicationNotificationRepository.class)
    );

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
