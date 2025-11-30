package com.example.cbumanage.repository;


import com.example.cbumanage.model.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {


    Page<Post> findByCategory(int category, Pageable pageable);

    Page<Post> findByTitleContaining(String title,Pageable pageable);

    Page<Post> findByContentContaining(String content,Pageable pageable);


}
