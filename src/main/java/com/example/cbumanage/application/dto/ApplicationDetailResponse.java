package com.example.cbumanage.application.dto;

import com.example.cbumanage.application.entity.enums.AcademicStatus;
import com.example.cbumanage.application.entity.enums.ApplicationField;
import com.example.cbumanage.application.entity.enums.RefSource;
import com.example.cbumanage.application.entity.enums.VoteResult;

import java.util.List;

/**
 * 신청서 상세 + 운영진 투표 현황.
 */
public record ApplicationDetailResponse(
        ApplicantInfo application,
        List<AnswerItem> answers,
        List<PortfolioItem> portfolios,
        List<VoteItem> votes,
        MyVote myVote
) {
    /** 신청서 내용 (자유서술은 answers로 분리) */
    public record ApplicantInfo(
            String applicationUuid,
            String name,
            String nickname,
            AcademicStatus grade,
            Long studentNumber,
            String major,
            String phoneNumber,
            ApplicationField applicationField,
            Boolean canOt,
            Boolean canWelcome,
            RefSource refSource,
            String refLinkEtc
    ) {
    }

    /** 자유서술 항목 (정규화된 질문/답변) */
    public record AnswerItem(
            String question,
            String answer
    ) {
    }

    /** 포트폴리오 링크 */
    public record PortfolioItem(
            String label,
            String url
    ) {
    }

    /** 운영진별 투표 (decision이 null이면 미투표) */
    public record VoteItem(
            String voterName,
            VoteResult decision,
            String reason
    ) {
    }

    /** 내 투표 (토글 초기값, decision이 null이면 미투표) */
    public record MyVote(
            VoteResult decision,
            String reason
    ) {
    }
}