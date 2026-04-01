package com.example.cbumanage.user.entity;

import com.example.cbumanage.user.entity.Role;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

@Entity
@Getter
@Table(name = "users")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    @UuidGenerator
    private UUID userUuid;

    @Column(unique = true, nullable = false)
    private Long studentNumber;

    @Column(nullable = false)
    private String password;

    @Column(unique = true)
    private String email;

    @Enumerated(EnumType.STRING)
    private com.example.cbumanage.user.entity.Role role;

    private Long generation;

    public User(String email, Long studentNumber, String password) {
        this.email = email;
        this.studentNumber = studentNumber;
        this.password = password;
        this.role = Role.ROLE_USER;
    }

    public void changePassword(String encodedPassword) {
        this.password = encodedPassword;
    }

    public void changeEmail(String email) {
        this.email = email;
    }
}
