package com.example.cbumanage.news.repository;

import com.example.cbumanage.news.entity.News;
import com.example.cbumanage.news.entity.enums.NewsCategory;
import com.example.cbumanage.news.entity.enums.NewsletterType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface NewsRepository extends JpaRepository<News, Long> {

    @Override
    @EntityGraph(attributePaths = "post")
    @Query("SELECT n FROM News n WHERE n.newsId = :id AND n.post.isDeleted = false")
    Optional<News> findById(@Param("id") Long id);

    @EntityGraph(attributePaths = "post")
    @Query("""
            SELECT n
            FROM News n
            WHERE n.isPinned = false
              AND n.post.isDeleted = false
              AND (n.category IN :categories
                   OR (:includeNullCategory = true AND n.category IS NULL))
              AND (:filterByNewsletterType = false
                   OR n.newsletterType IN :newsletterTypes)
            """)
    Page<News> findRegularNews(
            @Param("categories") Collection<NewsCategory> categories,
            @Param("includeNullCategory") boolean includeNullCategory,
            @Param("filterByNewsletterType") boolean filterByNewsletterType,
            @Param("newsletterTypes") Collection<NewsletterType> newsletterTypes,
            Pageable pageable
    );

    @EntityGraph(attributePaths = "post")
    @Query("""
            SELECT n
            FROM News n
            WHERE n.isPinned = true
              AND n.post.isDeleted = false
              AND (n.category IN :categories
                   OR (:includeNullCategory = true AND n.category IS NULL))
              AND (:filterByNewsletterType = false
                   OR n.newsletterType IN :newsletterTypes)
            ORDER BY n.pinnedAt DESC, n.newsId DESC
            """)
    List<News> findPinnedNews(
            @Param("categories") Collection<NewsCategory> categories,
            @Param("includeNullCategory") boolean includeNullCategory,
            @Param("filterByNewsletterType") boolean filterByNewsletterType,
            @Param("newsletterTypes") Collection<NewsletterType> newsletterTypes
    );

    @Query(
            value = """
                    SELECT n.news_id
                    FROM news n
                    JOIN post p ON p.post_id = n.post_id
                    WHERE n.deleted_at IS NULL
                      AND p.is_deleted = false
                      AND n.is_pinned = false
                      AND (n.news_category IN (:categoryNames)
                           OR (:includeNullCategory = true AND n.news_category IS NULL))
                      AND (:filterByNewsletterType = false
                           OR n.newsletter_type IN (:newsletterTypeNames))
                      AND MATCH(n.search_text) AGAINST (:searchQuery IN BOOLEAN MODE)
                    ORDER BY p.created_at DESC, n.news_id DESC
                    """,
            countQuery = """
                    SELECT COUNT(*)
                    FROM news n
                    JOIN post p ON p.post_id = n.post_id
                    WHERE n.deleted_at IS NULL
                      AND p.is_deleted = false
                      AND n.is_pinned = false
                      AND (n.news_category IN (:categoryNames)
                           OR (:includeNullCategory = true AND n.news_category IS NULL))
                      AND (:filterByNewsletterType = false
                           OR n.newsletter_type IN (:newsletterTypeNames))
                      AND MATCH(n.search_text) AGAINST (:searchQuery IN BOOLEAN MODE)
            """,
            nativeQuery = true
    )
    Page<Long> searchRegularNewsIds(
            @Param("categoryNames") Collection<String> categoryNames,
            @Param("includeNullCategory") boolean includeNullCategory,
            @Param("filterByNewsletterType") boolean filterByNewsletterType,
            @Param("newsletterTypeNames") Collection<String> newsletterTypeNames,
            @Param("searchQuery") String searchQuery,
            Pageable pageable
    );

    @Query(
            value = """
                    SELECT n.news_id
                    FROM news n
                    JOIN post p ON p.post_id = n.post_id
                    WHERE n.deleted_at IS NULL
                      AND p.is_deleted = false
                      AND n.is_pinned = true
                      AND (n.news_category IN (:categoryNames)
                           OR (:includeNullCategory = true AND n.news_category IS NULL))
                      AND (:filterByNewsletterType = false
                           OR n.newsletter_type IN (:newsletterTypeNames))
                      AND MATCH(n.search_text) AGAINST (:searchQuery IN BOOLEAN MODE)
                    ORDER BY n.pinned_at DESC, n.news_id DESC
            """,
            nativeQuery = true
    )
    List<Long> searchPinnedNewsIds(
            @Param("categoryNames") Collection<String> categoryNames,
            @Param("includeNullCategory") boolean includeNullCategory,
            @Param("filterByNewsletterType") boolean filterByNewsletterType,
            @Param("newsletterTypeNames") Collection<String> newsletterTypeNames,
            @Param("searchQuery") String searchQuery
    );

    @EntityGraph(attributePaths = "post")
    @Query("SELECT n FROM News n WHERE n.newsId IN :newsIds AND n.post.isDeleted = false")
    List<News> findAllByNewsIdInWithPost(@Param("newsIds") List<Long> newsIds);

    @EntityGraph(attributePaths = "post")
    @Query("SELECT n FROM News n WHERE n.post.authorId = :authorId AND n.post.isDeleted = false")
    Page<News> findByAuthorId(@Param("authorId") Long authorId, Pageable pageable);
}
