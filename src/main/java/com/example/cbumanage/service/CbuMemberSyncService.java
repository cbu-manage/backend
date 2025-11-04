package com.example.cbumanage.service;

import com.example.cbumanage.model.CbuMember;
import com.example.cbumanage.repository.CbuMemberRepository;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CbuMemberSyncService {
    @Autowired
    CbuMemberRepository cbuMemberRepository;

    @Value("${google.spreadSheet.key}")
    private String SheetKey;                               //구글 스프레드 시트 사용 api 키 값

    @Value("${google.spreadSheet.id}")
    private String SheetId;                                //구글 스프레드 시트 아이디 값

    private static final String SheetName = "sheet";       //구글 스프레드 시트 시트 이름

    @Transactional
    public void syncMembersFromGoogleSheet() {                        //스프레드 시트 -> 데이터베이스 유저 데이터 주입 함수
        List<CbuMember> sheetMembers = getMembersFromGoogleSheet();   //스프레드시트와 데이터베이스에서 현재 데이터를 조회
        List<CbuMember> dbMembers = cbuMemberRepository.findAll();    //스프레드시트와 데이터베이스에서 현재 데이터를 조회

        Map<Long, CbuMember> dbMemberMap = new HashMap<>();           //데이터베이스에 존재하는 회원들을 학번을 키로 이용한 Map으로 변환
        for (CbuMember member : dbMembers) {
            dbMemberMap.put(member.getStudentNumber(), member);
        }
        List<CbuMember> toUpdate = new ArrayList<>();                 //수정할 멤버 리스트 초기화
        List<CbuMember> toCreate = new ArrayList<>();                 //추가할 멤버 리스트 초기화

        for (CbuMember sheetMember : sheetMembers) {                  //스프레드시트의 각 멤버에 대해 처리
            CbuMember existingMember = dbMemberMap.get(sheetMember.getStudentNumber());

            if (existingMember == null) {                             //데이터베이스에 없는 새로운 멤버인 경우
                toCreate.add(sheetMember);                            //데이터베이스에 새로 추가
            } else if (hasChanged(existingMember, sheetMember)) {     //기존 멤버의 정보가 변경된 경우
                updateMemberFields(existingMember, sheetMember);      //데이터베이스에 값 변경
                toUpdate.add(existingMember);
            }
        }  //변경이 없을 경우 작업 X

        List<CbuMember> toDelete = findDeletedMembers(dbMemberMap, sheetMembers);  //스프레드시트에서 삭제된 멤버 탐색

        //스프레드시트에서 변경된 부분만 데이터베이스에 저장
        if (!toCreate.isEmpty()) {
            cbuMemberRepository.saveAll(toCreate);
        }
        if (!toUpdate.isEmpty()) {
            cbuMemberRepository.saveAll(toUpdate);
        }
        if (!toDelete.isEmpty()) {
            cbuMemberRepository.deleteAll(toDelete);
        }
    }

    private boolean hasChanged(CbuMember dbMember, CbuMember sheetMember) {    //데이터베이스의 멤버와 스프레드시트의 멤버 정보를 비교해 변경 여부 확인
        return !Objects.equals(dbMember.getName(), sheetMember.getName()) ||
                !Objects.equals(dbMember.getPhoneNumber(), sheetMember.getPhoneNumber()) ||
                !Objects.equals(dbMember.getMajor(), sheetMember.getMajor()) ||
                !Objects.equals(dbMember.getGrade(), sheetMember.getGrade()) ||
                !Objects.equals(dbMember.getGeneration(), sheetMember.getGeneration()) ||
                !Objects.equals(dbMember.getNote(), sheetMember.getNote()) ||
                !Objects.equals(dbMember.getDue(), sheetMember.getDue());
    }

    private void updateMemberFields(CbuMember existing, CbuMember updated) {   //기존 멤버의 정보를 새로운 정보로 업데이트
        existing.setName(updated.getName());
        existing.setPhoneNumber(updated.getPhoneNumber());
        existing.setMajor(updated.getMajor());
        existing.setGrade(updated.getGrade());
        existing.setGeneration(updated.getGeneration());
        existing.setNote(updated.getNote());
        existing.setDue(updated.getDue());
    }

    private List<CbuMember> findDeletedMembers(Map<Long, CbuMember> dbMembers, List<CbuMember> sheetMembers) {  //스프레드시트에서 삭제된 멤버 찾기
        Set<Long> sheetStudentNumbers = sheetMembers.stream()
                .map(CbuMember::getStudentNumber)
                .collect(Collectors.toSet());

        return dbMembers.values().stream()
                .filter(dbMember -> !sheetStudentNumbers.contains(dbMember.getStudentNumber()))
                .collect(Collectors.toList());
    }

    private List<CbuMember> getMembersFromGoogleSheet() {               //스프레드 시트 데이터 가져오는 함수
        URI sheetUrl = getSheetUri();                                   //커스텀 URI를 생성
        RestTemplate rt = new RestTemplate();
        ResponseEntity<GoogleSheetResponse> response = rt.exchange(     //Get 메소드를 이용해 구글에 요청 전송
                sheetUrl,
                HttpMethod.GET,
                null,
                GoogleSheetResponse.class
        );

        GoogleSheetResponse sheetResponse = response.getBody();         //응답에서 body값을 가져와서
        List<List<Object>> values = sheetResponse.getValues();

        List<CbuMember> members = new ArrayList<>();                    //arrayList로 매핑
        for (int i = 1; i < values.size(); i++) {
            List<Object> row = values.get(i);
            CbuMember member = mapRowToMember(row);
            members.add(member);
        }

        return members;
    }

    private URI getSheetUri() {       //api key, 스프레드 시트 아이디와 이름을 URI에 주입
        return UriComponentsBuilder
                .fromUriString("https://sheets.googleapis.com/v4/spreadsheets/{sheetId}/values/{sheetName}")
                .queryParam("key", SheetKey)
                .buildAndExpand(SheetId, SheetName)
                .toUri();
    }

    private CbuMember mapRowToMember(List<Object> row) {
        CbuMember member = new CbuMember();
        member.setName(getStringValue(row, 0));
        member.setRole(List.of());
        member.setPhoneNumber(getStringValue(row, 1));
        member.setMajor(getStringValue(row, 2));
        member.setGrade(getStringValue(row, 3));
        member.setStudentNumber(getLongValue(row, 4));
        member.setGeneration(getLongValue(row, 5));
        member.setNote(getStringValue(row, 6));

        if(Objects.equals(getStringValue(row, 7), "O")){
            member.setDue(true);
        }else{
            member.setDue(false);
        }
        return member;
    }

    private String getStringValue(List<Object> row, int index) {
        if (index >= row.size() || row.get(index) == null || row.get(index).toString().trim().isEmpty()) {
            return null;
        }
        return row.get(index).toString();
    }

    private Long getLongValue(List<Object> row, int index) {
        String value = getStringValue(row, index);
        return value != null ? Long.parseLong(value) : null;
    }
}

@Getter
class GoogleSheetResponse {
    private List<List<Object>> values;

    public void setValues(List<List<Object>> values) {
        this.values = values;
    }
}

