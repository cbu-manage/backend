package com.example.cbumanage.user.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Schema(description = """
        회원 권한:
        - ROLE_ADMIN: 개발자용 슈퍼 계정
        - ROLE_PRESIDENT: 회장
        - ROLE_VICE_PRESIDENT: 부회장
        - ROLE_MANAGER: 운영진 공용
        - ROLE_TREASURER: 총무
        - ROLE_MEMBER_MANAGER: 인원 관리
        - ROLE_EVENT_MANAGER: 행사 관리
        - ROLE_PROMOTION_MANAGER: 홍보
        - ROLE_SECRETARY: 서기
        - ROLE_USER: 일반 부원
        """)
@RequiredArgsConstructor
public enum Role {
    ROLE_USER(1),
    ROLE_MANAGER(2),
    ROLE_PRESIDENT(3),
    ROLE_VICE_PRESIDENT(4),
    ROLE_TREASURER(5),
    ROLE_MEMBER_MANAGER(6),
    ROLE_EVENT_MANAGER(7),
    ROLE_PROMOTION_MANAGER(8),
    ROLE_SECRETARY(9),
    ROLE_ADMIN(99);

    public final int value;

    private static final List<Role> APPLICATION_VOTER_ROLES = List.of(
            ROLE_PRESIDENT,
            ROLE_VICE_PRESIDENT,
            ROLE_MANAGER,
            ROLE_TREASURER,
            ROLE_MEMBER_MANAGER,
            ROLE_EVENT_MANAGER,
            ROLE_PROMOTION_MANAGER,
            ROLE_SECRETARY
    );

    public static List<Role> applicationVoterRoles() {
        return APPLICATION_VOTER_ROLES;
    }

    public boolean isDeveloperAdmin() {
        return this == ROLE_ADMIN;
    }

    public boolean isPresidentOrVicePresidentOrAdmin() {
        return this == ROLE_ADMIN || this == ROLE_PRESIDENT || this == ROLE_VICE_PRESIDENT;
    }

    public boolean canViewAllReports() {
        return this == ROLE_ADMIN || this == ROLE_PRESIDENT || this == ROLE_VICE_PRESIDENT || this == ROLE_SECRETARY;
    }

    public boolean canReviewApplications() {
        return isDeveloperAdmin() || APPLICATION_VOTER_ROLES.contains(this);
    }

    public boolean canVoteApplications() {
        return APPLICATION_VOTER_ROLES.contains(this);
    }
}
