package com.example.cbumanage.user.service;

import com.example.cbumanage.candidate.repository.SuccessCandidateRepository;
import com.example.cbumanage.global.common.JwtProvider;
import com.example.cbumanage.global.util.RedisUtil;
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

class LoginServiceTest {

    private final UserRepository userRepository = mock(UserRepository.class);
    private final RedisUtil redisUtil = mock(RedisUtil.class);
    private final LoginService loginService = new LoginService(
            userRepository,
            mock(JwtProvider.class),
            mock(SuccessCandidateRepository.class),
            redisUtil
    );

    @Test
    void deleteUserInvalidatesRefreshTokenAndSoftDeletesUser() {
        Long userId = 1L;
        User user = new User("user@example.com", 20240001L, "encoded-password");
        when(userRepository.findByUserIdAndDeletedAtIsNull(userId)).thenReturn(Optional.of(user));

        loginService.deleteUser(userId);

        verify(redisUtil).deleteData("refresh:" + userId);
        assertThat(user.getDeletedAt()).isNotNull();
        assertThat(user.getMemberStatus()).isEqualTo(MemberStatus.WITHDRAWN);
        verify(userRepository, never()).delete(any(User.class));
    }
}
