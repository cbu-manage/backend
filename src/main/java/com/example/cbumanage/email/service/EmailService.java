package com.example.cbumanage.email.service;

import com.example.cbumanage.email.dto.EmailAuthResponseDTO;
import com.example.cbumanage.global.error.BaseException;
import com.example.cbumanage.global.error.ErrorCode;
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

    /* 같은 주소로 연속 발송을 막는 쿨다운(초) */
    private static final long SEND_COOLDOWN_SECONDS = 60L;
    /* 같은 주소의 시간당 발송 한도 */
    private static final long SEND_LIMIT_PER_HOUR = 10L;
    private static final long SEND_LIMIT_WINDOW_SECONDS = 60 * 60L;
    /* 합격 안내 메일이 여는 화면 */
    private static final String APPLICATION_PASSED_PATH = "/apply/passed";
    /* 같은 요청자의 시간당 발송 한도. 주소만 바꿔가며 대량 발송하는 것을 막는다 */
    private static final long SEND_LIMIT_PER_IP_HOURLY = 20L;
    private static final String COOLDOWN_KEY_PREFIX = "mail:cooldown:";
    private static final String SEND_COUNT_KEY_PREFIX = "mail:count:";
    private static final String IP_COUNT_KEY_PREFIX = "mail:ip:";

    private final JavaMailSender mailSender;
    private final RedisUtil redisUtil;
    private final UserRepository userRepository;
    private final SystemSettingService systemSettingService;
    private final EmailManager emailManager;

    public EmailAuthResponseDTO sendEmail(String toEmail, String clientIp) {
        if (!emailManager.validEmail(toEmail)) {
            throw new BaseException(ErrorCode.INVALID_EMAIL_DOMAIN);
        }
        // 요청자 한도를 먼저 본다. 쿨다운은 여기서 선점하므로, 못 보낼 요청이 먼저 걸러져야 헛되이 잠기지 않는다
        checkClientLimit(clientIp);
        checkSendLimit(toEmail);

        if (redisUtil.existData(toEmail)) {
            redisUtil.deleteData(toEmail);
        }

        try {
            MimeMessage emailForm = createEmailForm(toEmail);
            mailSender.send(emailForm);
            return new EmailAuthResponseDTO(true, "인증번호가 메일로 전송되었습니다.");
        } catch (MessagingException | MailSendException e) {
            // 실제로 못 보냈으면 쿨다운을 풀어 60초를 기다리지 않고 다시 시도할 수 있게 한다
            redisUtil.deleteData(COOLDOWN_KEY_PREFIX + toEmail);
            return new EmailAuthResponseDTO(false, e.getMessage());
        }
    }

    /*
     * 인증이 필요 없는 API라 주소 단위로 쿨다운과 시간당 한도를 확인한다.
     * 있는지 보고 발송 뒤에 거는 방식이면 동시 요청이 모두 통과해 한 번에 시간당 한도까지 나가고,
     * 저장되는 인증번호도 서로 덮어써 대부분 못 쓰게 된다. 그래서 보내기 전에 SET NX로 선점한다.
     */
    private void checkSendLimit(String toEmail) {
        String cooldownKey = COOLDOWN_KEY_PREFIX + toEmail;
        if (!redisUtil.setIfAbsentExpire(cooldownKey, "1", SEND_COOLDOWN_SECONDS)) {
            throw new BaseException(ErrorCode.EMAIL_SEND_LIMIT_EXCEEDED);
        }
        long sentCount = redisUtil.increaseWithExpire(SEND_COUNT_KEY_PREFIX + toEmail, SEND_LIMIT_WINDOW_SECONDS);
        if (sentCount > SEND_LIMIT_PER_HOUR) {
            redisUtil.deleteData(cooldownKey);
            throw new BaseException(ErrorCode.EMAIL_SEND_LIMIT_EXCEEDED);
        }
    }

    /* 주소별 제한만으로는 주소를 바꿔가며 보내는 것을 막지 못해 요청자 단위로도 센다. */
    private void checkClientLimit(String clientIp) {
        if (clientIp == null || clientIp.isBlank()) {
            return;
        }
        long sentCount = redisUtil.increaseWithExpire(IP_COUNT_KEY_PREFIX + clientIp, SEND_LIMIT_WINDOW_SECONDS);
        if (sentCount > SEND_LIMIT_PER_IP_HOURLY) {
            throw new BaseException(ErrorCode.EMAIL_SEND_LIMIT_EXCEEDED);
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

    private String joinUrl(String baseUrl, String path) {
        if (baseUrl == null || baseUrl.isBlank()) return path;
        return baseUrl.endsWith("/")
                ? baseUrl.substring(0, baseUrl.length() - 1) + path
                : baseUrl + path;
    }

    private String setContext(String authCode) {
        return "<h4>인증 코드를 입력하세요.</h4>" + "<h2>[" + authCode + "]</h2>";
    }

    public EmailAuthResponseDTO sendApplicationResultEmail(String toEmail, String applicantName, boolean accepted,
                                                           String applicationUuid) {
        OnboardingLinksResponse links = systemSettingService.getOnboardingLinks();
        // 링크로 여는 화면이라 본인 확인 정보를 채우려면 지원서를 식별할 값이 필요하다
        String passedUrl = joinUrl(links.frontendUrl(), APPLICATION_PASSED_PATH) + "?a=" + applicationUuid;
        String subject = accepted ? "CBU 신규 부원 합격 안내" : "CBU 신규 부원 선발 결과 안내";
        String content = accepted
                ? """
                <h3>%s님, CBU 신규 부원 합격을 축하드립니다.</h3>
                <p>아래 링크에서 회원가입을 진행한 뒤 회비를 납부해 주세요.</p>
                <p><a href="%s">%s</a></p>
                <p>회비 확인 후 홈페이지 사용 권한이 활성화됩니다.</p>
                """.formatted(applicantName, passedUrl, passedUrl)
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
                <p>아래 링크를 통해 공지방·수다방 및 디스코드에 참여해 주세요.</p>
                <ul>
                    <li>공지방: <a href="%s">%s</a></li>
                    <li>수다방: <a href="%s">%s</a></li>
                    <li>회비 확인 및 문의 방: <a href="%s">%s</a></li>
                    <li>디스코드: <a href="%s">%s</a></li>
                </ul>
                <p>홈페이지: <a href="%s">%s</a></p>
                """.formatted(name, links.kakaoNotiUrl(), links.kakaoNotiUrl(),
                links.kakaoChatUrl(), links.kakaoChatUrl(),
                links.openChatUrl(), links.openChatUrl(),
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

    /* 발송에만 제한이 있어 검증은 무제한으로 찍어볼 수 있었다 */
    private static final long VERIFY_WINDOW_SECONDS = 10 * 60L;
    private static final long VERIFY_MAX_PER_WINDOW = 10L;

    public EmailAuthResponseDTO validateAuthCode(String email, String authCode) {
        // 실패·성공 여부와 무관하게 문구를 하나로 둔다. 갈리면 코드가 살아 있는지 알려주는 셈이 된다
        String invalidMessage = "인증번호가 올바르지 않거나 만료되었습니다. 다시 시도해주세요.";

        if (redisUtil.increaseWithExpire("mail:verify:" + email, VERIFY_WINDOW_SECONDS) > VERIFY_MAX_PER_WINDOW) {
            return new EmailAuthResponseDTO(false, "인증 시도가 너무 많습니다. 잠시 후 다시 시도해주세요.");
        }

        String findAuthCode = redisUtil.getData(email);
        if (findAuthCode == null || !findAuthCode.equals(authCode)) {
            return new EmailAuthResponseDTO(false, invalidMessage);
        }
        return new EmailAuthResponseDTO(true, "인증에 성공했습니다.");
    }

    @Transactional
    public void updateUserMail(MemberMailUpdateDTO memberMailUpdateDTO) {
        User user = userRepository.findByStudentNumber(memberMailUpdateDTO.getStudentNumber())
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        user.changeEmail(memberMailUpdateDTO.getEmail());
    }
}
