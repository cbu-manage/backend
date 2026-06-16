package com.example.cbumanage.user.entity;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RoleTest {

    @Test
    void adminIsDeveloperSuperAccountAndPresidentVicePresidentAreBelowAdmin() {
        assertThat(Role.ROLE_ADMIN.isDeveloperAdmin()).isTrue();
        assertThat(Role.ROLE_ADMIN.isPresidentOrVicePresidentOrAdmin()).isTrue();
        assertThat(Role.ROLE_PRESIDENT.isPresidentOrVicePresidentOrAdmin()).isTrue();
        assertThat(Role.ROLE_VICE_PRESIDENT.isPresidentOrVicePresidentOrAdmin()).isTrue();
        assertThat(Role.ROLE_MANAGER.isPresidentOrVicePresidentOrAdmin()).isFalse();
    }

    @Test
    void applicationVotersExcludeDeveloperAdminFromOfficialVoterCount() {
        assertThat(Role.applicationVoterRoles())
                .contains(Role.ROLE_PRESIDENT, Role.ROLE_VICE_PRESIDENT, Role.ROLE_MANAGER,
                        Role.ROLE_TREASURER, Role.ROLE_MEMBER_MANAGER,
                        Role.ROLE_EVENT_MANAGER, Role.ROLE_PROMOTION_MANAGER,
                        Role.ROLE_SECRETARY)
                .doesNotContain(Role.ROLE_ADMIN, Role.ROLE_USER);
        assertThat(Role.ROLE_ADMIN.canVoteApplications()).isFalse();
        assertThat(Role.ROLE_PRESIDENT.canVoteApplications()).isTrue();
        assertThat(Role.ROLE_VICE_PRESIDENT.canVoteApplications()).isTrue();
    }
}
