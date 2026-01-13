package com.example.cbumanage.repository;

import com.example.cbumanage.model.Solution;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface SolutionRepository extends JpaRepository<Solution, Integer> {
}
