package com.example.cbumanage.repository;

import com.example.cbumanage.model.Study;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudyRepository extends JpaRepository<Study, Long> {
}
