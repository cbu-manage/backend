package com.example.cbumanage.email.service;

import com.example.cbumanage.email.dto.EmailAuthResponseDTO;
import com.example.cbumanage.global.setting.dto.OnboardingLinksResponse;
import com.example.cbumanage.global.setting.service.SystemSettingService;
import com.example.cbumanage.global.util.RedisUtil;
import com.example.cbumanage.member.dto.MemberMailUpdateDTO;
import com.example.cbumanage.user.entity.User;
import com.example.cbumanage.user.repository.UserRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class EmailService {

    @Value("${spring.mail.username}")
    private String senderEmail;
    @Value("${cbu.frontend.url:https://cbu-manage.com}")
    private String frontendUrl;
    @Value("${cbu.onboarding.open-chat-url:}")
    private String openChatUrl;
    @Value("${cbu.onboarding.discord-url:}")
    private String discordUrl;

    private final JavaMailSender mailSender;
    private final RedisUtil redisUtil;
    private final UserRepository userRepository;
    private final SystemSettingService systemSettingService;

    public EmailAuthResponseDTO sendEmail(String toEmail) {
        if (redisUtil.existData(toEmail)) {
            redisUtil.deleteData(toEmail);
        }

        try {
            MimeMessage emailForm = createEmailForm(toEmail);
            mailSender.send(emailForm);
            return new EmailAuthResponseDTO(true, "인증번호가 메일로 전송되었습니다.");
        } catch (MessagingException | MailSendException e) {
            return new EmailAuthResponseDTO(false, e.getMessage());
        }
    }

    private MimeMessage createEmailForm(String email) throws MessagingException {
        String authCode = String.valueOf(ThreadLocalRandom.current().nextInt(100000, 1000000));

        MimeMessage message = mailSender.createMimeMessage();
        message.setFrom(senderEmail);
        message.setRecipients(MimeMessage.RecipientType.TO, email);
        message.setSubject("인증코드입니다.");
        message.setText(setContext(authCode), "utf-8", "html");

        redisUtil.setDataExpire(email, authCode, 10 * 60L);

        return message;
    }

    private String setContext(String authCode) {
        return "<h4>인증 코드를 입력하세요.</h4>" + "<h2>[" + authCode + "]</h2>";
    }

    public EmailAuthResponseDTO sendApplicationResultEmail(String toEmail, String applicantName, boolean accepted) {
        OnboardingLinksResponse links = systemSettingService.getOnboardingLinks();
        String subject = accepted ? "CBU 신규 부원 합격 안내" : "CBU 신규 부원 선발 결과 안내";
        String content = accepted
                ? """
                <h3>%s님, CBU 신규 부원 합격을 축하드립니다.</h3>
                <p>아래 링크에서 회원가입을 진행한 뒤 회비를 납부해 주세요.</p>
                <p><a href="%s">%s</a></p>
                <p>회비 확인 후 홈페이지 사용 권한이 활성화됩니다.</p>
                """.formatted(applicantName, links.frontendUrl(), links.frontendUrl())
                : """
                <h3>%s님, CBU 신규 부원 선발 결과를 안내드립니다.</h3>
                <p>아쉽게도 이번 모집에서는 함께하지 못하게 되었습니다.</p>
                """.formatted(applicantName);
        return sendHtmlEmail(toEmail, subject, content);
    }

    public EmailAuthResponseDTO sendOnboardingEmail(String toEmail, String name) {
        OnboardingLinksResponse links = systemSettingService.getOnboardingLinks();
        String content = """
                <h3>%s님, CBU 홈페이지 사용 권한이 활성화되었습니다.</h3>
                <p>아래 링크를 통해 공지방 및 디스코드에 참여해 주세요.</p>
                <ul>
                    <li>오픈채팅: <a href="%s">%s</a></li>
                    <li>디스코드: <a href="%s">%s</a></li>
                </ul>
                <p>홈페이지: <a href="%s">%s</a></p>
                """.formatted(name, links.openChatUrl(), links.openChatUrl(),
                links.discordUrl(), links.discordUrl(), links.frontendUrl(), links.frontendUrl());
        return sendHtmlEmail(toEmail, "CBU 가입 승인 및 커뮤니티 링크 안내", content);
    }

    private EmailAuthResponseDTO sendHtmlEmail(String toEmail, String subject, String htmlContent) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            message.setFrom(senderEmail);
            message.setRecipients(MimeMessage.RecipientType.TO, toEmail);
            message.setSubject(subject);
            message.setText(htmlContent, "utf-8", "html");
            mailSender.send(message);
            return new EmailAuthResponseDTO(true, "메일이 전송되었습니다.");
        } catch (MessagingException | MailSendException e) {
            return new EmailAuthResponseDTO(false, e.getMessage());
        }
    }

    public EmailAuthResponseDTO validateAuthCode(String email, String authCode) {
        String findAuthCode = redisUtil.getData(email);
        if (findAuthCode == null) {
            return new EmailAuthResponseDTO(false, "인증번호가 만료되었습니다. 다시 시도해주세요.");
        }

        if (findAuthCode.equals(authCode)) {
            return new EmailAuthResponseDTO(true, "인증에 성공했습니다.");
        }
        return new EmailAuthResponseDTO(false, "인증번호가 일치하지 않습니다.");
    }

    @Transactional
    public void updateUserMail(MemberMailUpdateDTO memberMailUpdateDTO) {
        User user = userRepository.findByStudentNumber(memberMailUpdateDTO.getStudentNumber())
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        user.changeEmail(memberMailUpdateDTO.getEmail());
    }
}
