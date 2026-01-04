package com.example.cbumanage.service;

import com.example.cbumanage.model.Category;
import com.example.cbumanage.model.Language;
import com.example.cbumanage.model.Platform;
import com.example.cbumanage.repository.CategoryRepository;
import com.example.cbumanage.repository.LanguageRepository;
import com.example.cbumanage.repository.PlatformRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 애플리케이션 시작 시 초기 마스터 데이터를 생성
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private final CategoryRepository categoryRepository;
    private final PlatformRepository platformRepository;
    private final LanguageRepository languageRepository;

    public DataInitializer(CategoryRepository categoryRepository, PlatformRepository platformRepository, LanguageRepository languageRepository) {
        this.categoryRepository = categoryRepository;
        this.platformRepository = platformRepository;
        this.languageRepository = languageRepository;
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        // 카테고리 데이터 초기화
        if (categoryRepository.count() == 0) {
            List<Category> categories = List.of(
                    new Category("DP"),
                    new Category("BFS"),
                    new Category("DFS"),
                    new Category("구현"),
                    new Category("그리디"),
                    new Category("정렬")
            );
            categoryRepository.saveAll(categories);
        }

        // 플랫폼 데이터 초기화
        if (platformRepository.count() == 0) {
            List<Platform> platforms = List.of(
                    new Platform("백준"),
                    new Platform("프로그래머스"),
                    new Platform("leetcode")
            );
            platformRepository.saveAll(platforms);
        }

        // 언어 데이터 초기화
        if (languageRepository.count() == 0) {
            List<Language> languages = List.of(
                    new Language("Java"),
                    new Language("Python"),
                    new Language("C++"),
                    new Language("JavaScript")
            );
            languageRepository.saveAll(languages);
        }
    }
}
