package com.example.cbumanage.news.util;

import org.jsoup.Jsoup;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public final class NewsSearchTextNormalizer {

    private NewsSearchTextNormalizer() {
    }

    public static String toSearchText(String title, String content) {
        return normalizeWhitespace(stripMarkup(nullToBlank(title) + " " + nullToBlank(content)));
    }

    public static List<String> toSearchTokens(String keyword) {
        String normalized = stripMarkup(keyword)
                .replaceAll("[^\\p{L}\\p{N}]+", " ");

        Set<String> tokens = new LinkedHashSet<>();
        Arrays.stream(normalizeWhitespace(normalized).split(" "))
                .map(String::trim)
                .filter(token -> !token.isBlank())
                .forEach(tokens::add);

        return List.copyOf(tokens);
    }

    public static String toRequiredBooleanQuery(List<String> tokens) {
        return tokens.stream()
                .map(token -> "+" + token)
                .reduce((left, right) -> left + " " + right)
                .orElse("");
    }

    public static String toOptionalBooleanQuery(List<String> tokens) {
        return String.join(" ", tokens);
    }

    private static String stripMarkup(String value) {
        String text = Jsoup.parse(nullToBlank(value)).text();
        text = text.replaceAll("!\\[([^]]*)]\\([^)]*\\)", "$1");
        text = text.replaceAll("\\[([^]]+)]\\([^)]*\\)", "$1");
        text = text.replaceAll("(?m)^\\s*```[\\w+-]*\\s*$", " ");
        text = text.replaceAll("https?://\\S+", " ");
        text = text.replaceAll("(?m)^\\s{0,3}#{1,6}\\s*", " ");
        text = text.replaceAll("(?m)^\\s*>\\s?", " ");
        text = text.replaceAll("[*_~`]+", " ");
        return text;
    }

    private static String normalizeWhitespace(String value) {
        return nullToBlank(value).replaceAll("\\s+", " ").trim();
    }

    private static String nullToBlank(String value) {
        return value == null ? "" : value;
    }
}
