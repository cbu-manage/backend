package com.example.cbumanage.news.repository;

import com.example.cbumanage.news.entity.enums.NewsCategory;
import com.example.cbumanage.news.entity.News;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

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
              AND (:category IS NULL
                   OR n.category = :category
                   OR (:includeDefaultCategory = true AND n.category IS NULL))
            """)
    Page<News> findRegularNews(
            @Param("category") NewsCategory category,
            @Param("includeDefaultCategory") boolean includeDefaultCategory,
            Pageable pageable
    );

    @EntityGraph(attributePaths = "post")
    @Query("""
            SELECT n
            FROM News n
            WHERE n.isPinned = true
              AND n.post.isDeleted = false
              AND (:category IS NULL
                   OR n.category = :category
                   OR (:includeDefaultCategory = true AND n.category IS NULL))
            ORDER BY n.pinnedAt DESC, n.newsId DESC
            """)
    List<News> findPinnedNews(
            @Param("category") NewsCategory category,
            @Param("includeDefaultCategory") boolean includeDefaultCategory
    );

    @Query(
            value = """
                    SELECT n.*
                    FROM news n
                    JOIN post p ON p.post_id = n.post_id
                    WHERE n.deleted_at IS NULL
                      AND p.is_deleted = false
                      AND n.is_pinned = false
                      AND (:categoryName IS NULL
                           OR n.news_category = :categoryName
                           OR (:includeDefaultCategory = true AND n.news_category IS NULL))
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
                      AND (:categoryName IS NULL
                           OR n.news_category = :categoryName
                           OR (:includeDefaultCategory = true AND n.news_category IS NULL))
                      AND MATCH(n.search_text) AGAINST (:searchQuery IN BOOLEAN MODE)
                    """,
            nativeQuery = true
    )
    Page<News> searchRegularNews(
            @Param("categoryName") String categoryName,
            @Param("includeDefaultCategory") boolean includeDefaultCategory,
            @Param("searchQuery") String searchQuery,
            Pageable pageable
    );

    @Query(
            value = """
                    SELECT n.*
                    FROM news n
                    JOIN post p ON p.post_id = n.post_id
                    WHERE n.deleted_at IS NULL
                      AND p.is_deleted = false
                      AND n.is_pinned = true
                      AND (:categoryName IS NULL
                           OR n.news_category = :categoryName
                           OR (:includeDefaultCategory = true AND n.news_category IS NULL))
                      AND MATCH(n.search_text) AGAINST (:searchQuery IN BOOLEAN MODE)
                    ORDER BY n.pinned_at DESC, n.news_id DESC
                    """,
            nativeQuery = true
    )
    List<News> searchPinnedNews(
            @Param("categoryName") String categoryName,
            @Param("includeDefaultCategory") boolean includeDefaultCategory,
            @Param("searchQuery") String searchQuery
    );

    @EntityGraph(attributePaths = "post")
    @Query("SELECT n FROM News n WHERE n.post.authorId = :authorId AND n.post.isDeleted = false")
    Page<News> findByAuthorId(@Param("authorId") Long authorId, Pageable pageable);
}
