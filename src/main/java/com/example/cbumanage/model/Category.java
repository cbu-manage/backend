package com.example.cbumanage.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 문제 유형(카테고리) 정보를 저장하는 엔티티입니다. (예: DP, BFS)
 */
@Entity
@Getter
@NoArgsConstructor
@Table(name = "category")
public class Category {

    /**
     * 카테고리 고유 식별자
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer categoryId;

    /**
     * 카테고리 이름
     */
    @Column(nullable = false, length = 20)
    private String name;

    public Category(String name) {
        this.name = name;
    }
}
