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

    @EntityGraph(attributePaths = "post")
    @Query("SELECT n FROM News n WHERE n.post.authorId = :authorId AND n.post.isDeleted = false")
    Page<News> findByAuthorId(@Param("authorId") Long authorId, Pageable pageable);
}
