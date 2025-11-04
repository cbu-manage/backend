package com.example.cbumanage.service;

import com.example.cbumanage.model.SuccessCandidate;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.sheets.v4.*;
import com.google.api.services.sheets.v4.model.*;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.*;
import java.security.GeneralSecurityException;
import java.util.*;

@Service
public class CandidateAppendService {

    private Sheets sheetsService; // 구글 Sheets 클라이언트
    private static final String APPLICATION_NAME = "My Sheets App";

    // 스프레드시트 ID와 시트 이름
    @Value("${google.spreadSheet.id}")
    private String SHEET_ID;
    private static final String SHEET_NAME = "sheet"; // 시트 이름
    private static final String RANGE = "A1:H";        // append 범위 기준

    /**
     * 1) 서비스 계정 JSON 파일 읽고,
     * 2) 구글 인증 객체 생성 후,
     * 3) Sheets 서비스 클라이언트 생성
     */
    @PostConstruct
    public void init() throws IOException, GeneralSecurityException {
        // 1. service_account.json 을 resources 폴더에서 읽어오기
        InputStream in = getClass().getResourceAsStream("/cbumanage-464308-f2bb67f5d410.json");
        if (in == null) {
            throw new FileNotFoundException("service_account.json 파일을 찾을 수 없습니다.");
        }

        // 2. 구글 Credentials 생성 (SheetsScopes.SPREADSHEETS 권한)
        GoogleCredentials credentials = GoogleCredentials.fromStream(in)
                .createScoped(Collections.singletonList("https://www.googleapis.com/auth/spreadsheets"));

        // 3. HTTP_TRANSPORT 생성
        var httpTransport = GoogleNetHttpTransport.newTrustedTransport();

        // 4. Sheets 클라이언트 생성
        sheetsService = new Sheets.Builder(
                httpTransport,
                JacksonFactory.getDefaultInstance(),
                new HttpCredentialsAdapter(credentials)
        )
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

    /**
     * 스프레드시트에 단일 SuccessCandidate 정보를 마지막 행 아래에 추가
     */
    public void appendSuccessCandidateToGoogleSheet(SuccessCandidate candidate) throws IOException {
        // 1. 시트에 추가할 데이터 구성
        List<List<Object>> values = new ArrayList<>();
        List<Object> row = new ArrayList<>();
        row.add(candidate.getName());            // 이름
        row.add(candidate.getPhoneNumber());     // 연락처
        row.add(candidate.getMajor());           // 학과
        row.add(candidate.getGrade());           // 학년
        row.add(candidate.getStudentNumber());   // 학번
        row.add("23");                           // 기수
        row.add("");                             // 비고
        row.add("X");                            // 회비관리
        values.add(row);

        ValueRange requestBody = new ValueRange().setValues(values);

        // 2. Append 요청
        AppendValuesResponse result = sheetsService.spreadsheets().values()
                .append(SHEET_ID, SHEET_NAME + "!" + RANGE, requestBody)
                .setValueInputOption("USER_ENTERED")  // USER_ENTERED or RAW
                .setInsertDataOption("INSERT_ROWS")   // INSERT_ROWS or OVERWRITE
                .execute();

        System.out.println("구글 스프레드시트에 데이터를 성공적으로 추가했습니다. " +
                "UpdatedCells=" + result.getUpdates().getUpdatedCells());
    }
}
