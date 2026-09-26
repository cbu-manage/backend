package com.example.cbumanage.global.setting.service;

import com.example.cbumanage.global.setting.dto.OnboardingLinksRequest;
import com.example.cbumanage.global.setting.dto.OnboardingLinksResponse;
import com.example.cbumanage.global.setting.dto.PresidentInfoRequest;
import com.example.cbumanage.global.setting.dto.PresidentInfoResponse;
import com.example.cbumanage.global.setting.entity.SystemSetting;
import com.example.cbumanage.global.setting.repository.SystemSettingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SystemSettingService {

    public static final String FRONTEND_URL = "cbu.frontend.url";
    public static final String OPEN_CHAT_URL = "cbu.onboarding.open-chat-url";
    public static final String DISCORD_URL = "cbu.onboarding.discord-url";
    public static final String KAKAO_NOTI_URL = "cbu.onboarding.kakao-noti-url";
    public static final String KAKAO_CHAT_URL = "cbu.onboarding.kakao-chat-url";

    /** 활동 내역서(HWP) 하단 대표자. 회장이 바뀔 때 템플릿 대신 여기를 고친다. */
    public static final String PRESIDENT_NAME = "cbu.president.name";
    public static final String PRESIDENT_SIGNATURE_URL = "cbu.president.signature-url";

    @Value("${cbu.frontend.url:https://cbu-manage.com}")
    private String defaultFrontendUrl;
    @Value("${cbu.onboarding.open-chat-url:}")
    private String defaultOpenChatUrl;
    @Value("${cbu.onboarding.discord-url:}")
    private String defaultDiscordUrl;
    @Value("${cbu.onboarding.kakao-noti-url:}")
    private String defaultKakaoNotiUrl;
    @Value("${cbu.onboarding.kakao-chat-url:}")
    private String defaultKakaoChatUrl;
    @Value("${cbu.president.name:}")
    private String defaultPresidentName;
    @Value("${cbu.president.signature-url:}")
    private String defaultPresidentSignatureUrl;

    private final SystemSettingRepository systemSettingRepository;

    @Transactional(readOnly = true)
    public OnboardingLinksResponse getOnboardingLinks() {
        return new OnboardingLinksResponse(
                getValue(FRONTEND_URL, defaultFrontendUrl),
                getValue(OPEN_CHAT_URL, defaultOpenChatUrl),
                getValue(DISCORD_URL, defaultDiscordUrl),
                getValue(KAKAO_NOTI_URL, defaultKakaoNotiUrl),
                getValue(KAKAO_CHAT_URL, defaultKakaoChatUrl)
        );
    }

    @Transactional
    public OnboardingLinksResponse updateOnboardingLinks(OnboardingLinksRequest request) {
        upsert(FRONTEND_URL, request.frontendUrl());
        upsert(OPEN_CHAT_URL, request.openChatUrl());
        upsert(DISCORD_URL, request.discordUrl());
        upsert(KAKAO_NOTI_URL, request.kakaoNotiUrl());
        upsert(KAKAO_CHAT_URL, request.kakaoChatUrl());
        return getOnboardingLinks();
    }

    @Transactional(readOnly = true)
    public PresidentInfoResponse getPresidentInfo() {
        return new PresidentInfoResponse(
                getValue(PRESIDENT_NAME, defaultPresidentName),
                getValue(PRESIDENT_SIGNATURE_URL, defaultPresidentSignatureUrl)
        );
    }

    @Transactional
    public PresidentInfoResponse updatePresidentInfo(PresidentInfoRequest request) {
        upsert(PRESIDENT_NAME, request.presidentName());
        upsert(PRESIDENT_SIGNATURE_URL, request.signatureImageUrl());
        return getPresidentInfo();
    }

    @Transactional(readOnly = true)
    public String getValue(String key, String defaultValue) {
        return systemSettingRepository.findById(key)
                .map(SystemSetting::getValue)
                .filter(value -> value != null && !value.isBlank())
                .orElse(defaultValue);
    }

    /* 요청에 없는 항목(null)은 기존 값을 유지한다. 일부만 보내는 화면이 나머지를 지우면 안 된다. */
    private void upsert(String key, String value) {
        if (value == null) return;
        SystemSetting setting = systemSettingRepository.findById(key)
                .orElseGet(() -> new SystemSetting(key, value));
        setting.update(value);
        systemSettingRepository.save(setting);
    }
}
