package com.example.cbumanage.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 문제 풀이에 사용된 프로그래밍 언어 정보를 저장하는 엔티티입니다. (예: Java, Python)
 */
@Entity
@Getter
@NoArgsConstructor
@Table(name = "language")
public class Language {

    /**
     * 언어 고유 식별자
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer languageId;

    /**
     * 언어 이름
     */
    @Column(nullable = false, length = 50)
    private String name;

    public Language(String name) {
        this.name = name;
    }
}
