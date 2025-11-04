package com.example.cbumanage.service;

import com.example.cbumanage.authentication.entity.LoginEntity;
import com.example.cbumanage.authentication.repository.LoginRepository;
import com.example.cbumanage.dto.EmailAuthResponseDTO;
import com.example.cbumanage.dto.MemberMailUpdateDTO;
import com.example.cbumanage.model.CbuMember;
import com.example.cbumanage.repository.CbuMemberRepository;
import com.example.cbumanage.utils.RedisUtil;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.AppendValuesResponse;
import com.google.api.services.sheets.v4.model.UpdateValuesResponse;
import com.google.api.services.sheets.v4.model.ValueRange;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import javax.annotation.PostConstruct;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class EmailService {

    @Value("${spring.mail.username}")
    private String senderEmail;

    private final JavaMailSender mailSender;
    private final RedisUtil redisUtil;

    @Autowired
    LoginRepository loginRepository;

    @Autowired
    CbuMemberRepository cbuMemberRepository;

    private Sheets sheetsService; // 구글 Sheets 클라이언트
    private static final String APPLICATION_NAME = "My Sheets App";

    @Value("${google.spreadSheet.id}")
    private String SHEET_ID;
    private static final String SHEET_NAME = "sheet"; // 시트 이름
    private static final String RANGE = "A1:I";        // append 범위 기준
    @Value("${google.spreadSheet.key}")
    private String SheetKey;

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

    public EmailAuthResponseDTO sendEmail(String toEmail) {
        if (redisUtil.existData(toEmail)) {
            redisUtil.deleteData(toEmail);
        }

        try {
            MimeMessage emailForm = createEmailForm(toEmail);
            mailSender.send(emailForm);
            return new EmailAuthResponseDTO(true, "인증번호가 메일로 전송되었습니다.");
        } catch (MessagingException | MailSendException e) {
            String errorr = e.getMessage();
            return new EmailAuthResponseDTO(false, errorr);
        }
    }

    private MimeMessage createEmailForm(String email) throws MessagingException {

        String authCode = String.valueOf(ThreadLocalRandom.current().nextInt(100000, 1000000));

        MimeMessage message = mailSender.createMimeMessage();
        message.setFrom(senderEmail);
        message.setRecipients(MimeMessage.RecipientType.TO, email);
        message.setSubject("인증코드입니다.");
        message.setText(setContext(authCode), "utf-8", "html");

        redisUtil.setDataExpire(email, authCode, 10 * 60L); // 10분

        return message;
    }

    private String setContext(String authCode) {
        String body = "";
        body += "<h4>" + "인증 코드를 입력하세요." + "</h4>";
        body += "<h2>" + "[" + authCode + "]" + "</h2>";
        return body;
    }

    public EmailAuthResponseDTO validateAuthCode(String email, String authCode) {
        String findAuthCode = redisUtil.getData(email);
        if (findAuthCode == null) {
            return new EmailAuthResponseDTO(false, "인증번호가 만료되었습니다. 다시 시도해주세요.");
        }

        if (findAuthCode.equals(authCode)) {
            return new EmailAuthResponseDTO(true, "인증에 성공했습니다.");

        } else {
            return new EmailAuthResponseDTO(false, "인증번호가 일치하지 않습니다.");
        }
    }

    @Transactional
    public void updateUserMail(MemberMailUpdateDTO memberMailUpdateDTO) throws IOException {
        LoginEntity loginEntity = loginRepository.findLoginEntityByStudentNumber(memberMailUpdateDTO.getStudentNumber());
        CbuMember cbuMember = cbuMemberRepository.findCbuMemberByStudentNumber(memberMailUpdateDTO.getStudentNumber());

        cbuMember.setEmail(memberMailUpdateDTO.getEmail());
        loginEntity.setEmail(memberMailUpdateDTO.getEmail());
        loginRepository.save(loginEntity);
        cbuMemberRepository.save(cbuMember);

        updateSheetEmail(cbuMember);
    }

    public void appendMemberEmail(CbuMember cbuMember) throws IOException {
        List<List<Object>> values = new ArrayList<>();
        List<Object> row = new ArrayList<>();
        row.add(cbuMember.getName());            // 이름
        row.add(cbuMember.getPhoneNumber());     // 연락처
        row.add(cbuMember.getMajor());           // 학과
        row.add(cbuMember.getGrade());           // 학년
        row.add(cbuMember.getStudentNumber());   // 학번
        row.add(cbuMember.getGeneration());      // 기수
        row.add(cbuMember.getNote());            // 비고
        row.add(cbuMember.getDue());             // 회비관리
        row.add(cbuMember.getEmail());           // 이메일
        values.add(row);

        ValueRange requestBody = new ValueRange().setValues(values);

        AppendValuesResponse result = sheetsService.spreadsheets().values()
                .append(SHEET_ID, SHEET_NAME + "!" + RANGE, requestBody)
                .setValueInputOption("USER_ENTERED")  // USER_ENTERED or RAW
                .setInsertDataOption("INSERT_ROWS")   // INSERT_ROWS or OVERWRITE
                .execute();

        System.out.println("구글 스프레드시트에 데이터를 성공적으로 추가했습니다. " +
                "UpdatedCells=" + result.getUpdates().getUpdatedCells());

    }

    @Transactional
    public void updateSheetEmail(CbuMember member) throws IOException {
        // 1. 스프레드시트의 모든 데이터를 읽어옵니다.
        List<List<Object>> sheetValues = getSheetValues();
        int targetRowNumber = -1;

        // 2. 헤더는 첫 행으로 가정하고, 데이터는 2행부터 시작합니다.
        //    학생 번호는 5번째 열(index 4)에 있다고 가정합니다.
        for (int i = 1; i < sheetValues.size(); i++) {
            List<Object> row = sheetValues.get(i);
            if (row.size() > 4) {
                try {
                    Long sheetStudentNumber = Long.parseLong(row.get(4).toString().trim());
                    if (sheetStudentNumber.equals(member.getStudentNumber())) {
                        // 실제 스프레드시트의 행 번호는 헤더가 1행이므로, 데이터 행의 인덱스 i는 실제 행 번호 i+1
                        targetRowNumber = i + 1;
                        break;
                    }
                } catch (NumberFormatException e) {
                    // 파싱 오류 발생 시 해당 행은 건너뜁니다.
                }
            }
        }

        if (targetRowNumber == -1) {
            System.out.println("학생 번호 " + member.getStudentNumber() + "에 해당하는 행을 찾지 못했습니다.");
            return;
        }

        // 3. 업데이트할 범위: 스프레드시트의 시트 이름 "sheet"의 I열(targetRowNumber 행)
        String updateRange = SHEET_NAME + "!I" + targetRowNumber + ":I" + targetRowNumber;

        // 4. 업데이트할 값 구성: 이메일 값을 단일 셀 업데이트로 준비 ([ [email] ] 형태)
        ValueRange body = new ValueRange().setValues(List.of(List.of(member.getEmail())));

        // 5. Sheets API의 update 메서드를 사용하여 해당 범위를 업데이트합니다.
        UpdateValuesResponse updateResponse = sheetsService.spreadsheets().values()
                .update(SHEET_ID, updateRange, body)
                .setValueInputOption("USER_ENTERED")
                .execute();

        System.out.println("스프레드시트에 업데이트된 셀 수: " + updateResponse.getUpdatedCells());
    }

    private List<List<Object>> getSheetValues() {
        URI sheetUri = UriComponentsBuilder
                .fromUriString("https://sheets.googleapis.com/v4/spreadsheets/{sheetId}/values/{sheetName}")
                .queryParam("key", SheetKey)
                .buildAndExpand(SHEET_ID, SHEET_NAME)
                .toUri();
        RestTemplate rt = new RestTemplate();
        ResponseEntity<GoogleSheetResponse> response = rt.exchange(sheetUri, HttpMethod.GET, null, GoogleSheetResponse.class);
        GoogleSheetResponse sheetResponse = response.getBody();
        return sheetResponse.getValues();
    }

}