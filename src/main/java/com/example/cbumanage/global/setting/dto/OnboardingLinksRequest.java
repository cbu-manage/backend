package com.example.cbumanage.global.setting.dto;

public record OnboardingLinksRequest(
        String frontendUrl,
        String openChatUrl,
        String discordUrl
) {
}
