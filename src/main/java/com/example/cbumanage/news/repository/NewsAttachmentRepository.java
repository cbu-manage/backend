package com.example.cbumanage.news.repository;

import com.example.cbumanage.news.entity.NewsAttachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NewsAttachmentRepository extends JpaRepository<NewsAttachment, Long> {

    List<NewsAttachment> findByNews_NewsIdOrderByAttachmentIdAsc(Long newsId);
}
