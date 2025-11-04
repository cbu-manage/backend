package com.example.cbumanage.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "cbu_success_member")
public class SuccessCandidate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long successMemberId;
    private String name;
    private String nickName;
    private String grade;
    private String major;
    private String phoneNumber;
    private Long studentNumber;

}
