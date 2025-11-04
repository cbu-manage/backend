package com.example.cbumanage.model;


import com.example.cbumanage.model.converter.MemberRoleConverter;
import com.example.cbumanage.model.enums.Role;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;

import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "cbu_member")
public class CbuMember {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long cbuMemberId;

    @Convert(converter = MemberRoleConverter.class)
    @Column(name = "role", nullable = false)
    @ColumnDefault("0")
    private List<Role> role;                      // 권한
    @Column(name = "name", length = 32)
    private String name;                          //이름

    @Column(name = "phone_number", length = 32)
    private String  phoneNumber;                  //전화번호
    private String  major;                        //학과
    private String  grade;                        //학년
    private Long    studentNumber;                //학번
    private Long    generation;                   //기수
    private String  note;                         //비고
    private Boolean  due;
    private String email;
}
