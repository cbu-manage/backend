package com.example.cbumanage.user.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Table(name = "user")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    @UuidGenerator
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "user_uuid", length = 36, nullable = false, unique = true)
    private UUID userUuid;

    @Column(name = "application_id", unique = true)
    private Long applicationId;

    @Column(unique = true, nullable = false)
    private Long studentNumber;

    @Column(nullable = false)
    private String password;

    @Column(unique = true)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Column(length = 32)
    private String name;

    @Column(length = 32)
    private String phoneNumber;

    private String major;

    private String grade;

    private Long generation;

    private String note;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MemberStatus memberStatus;

    private LocalDateTime joinedAt;

    private LocalDateTime approvedAt;

    private LocalDateTime deletedAt;

    public User(String email, Long studentNumber, String password) {
        this.email = email;
        this.studentNumber = studentNumber;
        this.password = password;
        this.role = Role.ROLE_USER;
        this.memberStatus = MemberStatus.PENDING_PAYMENT_CONFIRMATION;
        this.joinedAt = LocalDateTime.now();
    }

    public User(
            Long applicationId,
            Long studentNumber,
            String password,
            String email,
            String name,
            String phoneNumber,
            String major,
            String grade,
            Long generation
    ) {
        this(email, studentNumber, password);
        this.applicationId = applicationId;
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.major = major;
        this.grade = grade;
        this.generation = generation;
    }

    public void changePassword(String encodedPassword) {
        this.password = encodedPassword;
    }

    public void changeEmail(String email) {
        this.email = email;
    }

    public void updateProfile(String name, String phoneNumber, String major, String grade, Long generation) {
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.major = major;
        this.grade = grade;
        this.generation = generation;
    }

    public void updateMemberInfo(String name, String phoneNumber, String major, String grade, Long studentNumber, Long generation, String note) {
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.major = major;
        this.grade = grade;
        this.studentNumber = studentNumber;
        this.generation = generation;
        this.note = note;
    }

    public void changeRole(Role role) {
        this.role = role;
    }

    public void changeMemberStatus(MemberStatus memberStatus) {
        this.memberStatus = memberStatus;
        if (memberStatus == MemberStatus.ACTIVE) {
            this.approvedAt = LocalDateTime.now();
        }
    }

    public void delete() {
        this.deletedAt = LocalDateTime.now();
        this.memberStatus = MemberStatus.WITHDRAWN;
    }
}
