package com.example.cbumanage.dto;

import com.example.cbumanage.model.enums.Role;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
@Data
@JsonNaming
public class MemberUpdateDTO {
	private Long cbuMemberId = null;
	private List<Role> role = null;
	private String 	name = null;            //이름
	private String  phoneNumber = null;     //전화번호
	private String  major = null;           //학과
	private String  grade = null;           //학년
	private Long    studentNumber = null;   //학번
	private Long    generation = null;      //기수
	private String  note = null;            //비고
	private String  kakaoNoti = null; 		//공지방 가입 유무
	private String  kakaoChat = null; 		//수다방 가입 유무
}
