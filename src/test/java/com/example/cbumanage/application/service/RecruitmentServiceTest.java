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

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
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
    private final RecruitmentGenerationPolicy generationPolicy = new RecruitmentGenerationPolicy(
            Clock.fixed(Instant.parse("2026-06-17T00:00:00Z"), ZoneId.of("Asia/Seoul")),
            2026,
            RecruitmentGenerationPolicy.RecruitmentSeason.SUMMER_BREAK,
            29);
    private final RecruitmentService recruitmentService =
            new RecruitmentService(recruitmentRepository, userRepository, generationPolicy);

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
    void openGeneratesGenerationFromCurrentRecruitmentSeasonWhenRequestGenerationIsNull() {
        when(recruitmentRepository.findFirstByStatus(RecruitmentStatus.OPEN)).thenReturn(Optional.empty());
        when(recruitmentRepository.findByGeneration(29L)).thenReturn(Optional.empty());
        when(userRepository.countByRoleInAndDeletedAtIsNull(Role.applicationVoterRoles())).thenReturn(3L);
        when(recruitmentRepository.save(any(Recruitment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = recruitmentService.open(new RecruitmentCreateRequest(null));

        assertThat(response.generation()).isEqualTo(29L);
    }

    @Test
    void generationPolicyIncrementsGenerationByRecruitmentSeason() {
        assertThat(generationPolicy.generationFor(LocalDate.of(2026, 6, 1))).isEqualTo(29L);
        assertThat(generationPolicy.generationFor(LocalDate.of(2026, 9, 1))).isEqualTo(30L);
        assertThat(generationPolicy.generationFor(LocalDate.of(2026, 12, 1))).isEqualTo(31L);
        assertThat(generationPolicy.generationFor(LocalDate.of(2027, 1, 15))).isEqualTo(31L);
        assertThat(generationPolicy.generationFor(LocalDate.of(2027, 3, 1))).isEqualTo(32L);
    }

    @Test
    void getCurrentApplicationGenerationReturnsOpenRecruitmentGeneration() {
        when(recruitmentRepository.findFirstByStatus(RecruitmentStatus.OPEN))
                .thenReturn(Optional.of(Recruitment.open(29L, 3)));

        var response = recruitmentService.getCurrentApplicationGeneration();

        assertThat(response.generation()).isEqualTo(29L);
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
