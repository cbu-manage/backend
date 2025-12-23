package com.example.cbumanage.repository;

import com.example.cbumanage.model.PostReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PostReportRepository extends JpaRepository<PostReport, Long> {
    PostReport findByPostId(Long postId);
}
