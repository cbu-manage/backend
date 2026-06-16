package com.example.cbumanage.application.service;

import com.example.cbumanage.application.dto.RecruitmentCreateRequest;
import com.example.cbumanage.application.entity.Recruitment;
import com.example.cbumanage.application.entity.enums.RecruitmentStatus;
import com.example.cbumanage.application.repository.RecruitmentRepository;
import com.example.cbumanage.global.error.BaseException;
import com.example.cbumanage.global.error.ErrorCode;
import com.example.cbumanage.user.entity.Role;
import com.example.cbumanage.user.repository.UserRepository;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RecruitmentServiceTest {

    private final RecruitmentRepository recruitmentRepository = mock(RecruitmentRepository.class);
    private final UserRepository userRepository = mock(UserRepository.class);
    private final RecruitmentService recruitmentService =
            new RecruitmentService(recruitmentRepository, userRepository);

    @Test
    void openUsesOfficialVoterRolesWithoutDeveloperAdminForVoterCount() {
        when(recruitmentRepository.findFirstByStatus(RecruitmentStatus.OPEN)).thenReturn(Optional.empty());
        when(recruitmentRepository.findByGeneration(40L)).thenReturn(Optional.empty());
        when(userRepository.countByRoleInAndDeletedAtIsNull(Role.applicationVoterRoles())).thenReturn(3L);
        when(recruitmentRepository.save(any(Recruitment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = recruitmentService.open(new RecruitmentCreateRequest(40L));

        assertThat(response.generation()).isEqualTo(40L);
        assertThat(response.voterCount()).isEqualTo(3);
        verify(userRepository).countByRoleInAndDeletedAtIsNull(Role.applicationVoterRoles());
    }

    @Test
    void openRejectsDuplicatedGenerationBeforeSave() {
        when(recruitmentRepository.findFirstByStatus(RecruitmentStatus.OPEN)).thenReturn(Optional.empty());
        when(recruitmentRepository.findByGeneration(40L)).thenReturn(Optional.of(Recruitment.open(40L, 3)));

        assertThatThrownBy(() -> recruitmentService.open(new RecruitmentCreateRequest(40L)))
                .isInstanceOfSatisfying(BaseException.class,
                        e -> assertThat(e.getErrorCode()).isEqualTo(ErrorCode.RECRUITMENT_DUPLICATED));
    }

    @Test
    void closeRejectsAlreadyClosedRecruitment() {
        Recruitment recruitment = Recruitment.open(40L, 3);
        recruitment.close();
        when(recruitmentRepository.findByRecruitmentUuid(recruitment.getRecruitmentUuid()))
                .thenReturn(Optional.of(recruitment));

        assertThatThrownBy(() -> recruitmentService.close(recruitment.getRecruitmentUuid()))
                .isInstanceOfSatisfying(BaseException.class,
                        e -> assertThat(e.getErrorCode()).isEqualTo(ErrorCode.RECRUITMENT_ALREADY_CLOSED));
    }
}
