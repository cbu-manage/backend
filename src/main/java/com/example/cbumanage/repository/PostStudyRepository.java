package com.example.cbumanage.repository;

import com.example.cbumanage.model.PostStudy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PostStudyRepository extends JpaRepository<PostStudy, Long> {
    PostStudy findByPostId(Long postId);
}

