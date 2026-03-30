package com.example.cbumanage.problem.repository;

import com.example.cbumanage.problem.entity.Language;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface LanguageRepository extends JpaRepository<Language, Integer> {
}
