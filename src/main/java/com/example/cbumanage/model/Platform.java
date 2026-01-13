package com.example.cbumanage.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 문제의 출처 플랫폼 정보를 저장하는 엔티티입니다. (예: 백준, 프로그래머스)
 */
@Entity
@Getter
@NoArgsConstructor
@Table(name = "platform")
public class Platform {

    /**
     * 플랫폼 고유 식별자
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer platformId;

    /**
     * 플랫폼 이름
     */
    @Column(nullable = false, length = 50)
    private String name;

    public Platform(String name) {
        this.name = name;
    }
}
