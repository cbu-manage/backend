package com.example.cbumanage.email.service;

import com.example.cbumanage.user.entity.User;
import com.example.cbumanage.user.repository.UserRepository;
import com.example.cbumanage.email.dto.EmailAuthResponseDTO;
import com.example.cbumanage.member.dto.MemberMailUpdateDTO;
import com.example.cbumanage.global.util.RedisUtil;
import com.example.cbumanage.member.service.GoogleSheetResponse;
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
    UserRepository userRepository;

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
        User user = userRepository.findByStudentNumber(memberMailUpdateDTO.getStudentNumber())
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        user.changeEmail(memberMailUpdateDTO.getEmail());
        userRepository.save(user);
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