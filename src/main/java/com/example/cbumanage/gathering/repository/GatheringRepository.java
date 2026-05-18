package com.example.cbumanage.gathering.repository;

import com.example.cbumanage.gathering.entity.Gathering;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GatheringRepository extends JpaRepository<Gathering, Long> {

    List<Gathering> findAllByIsDeletedFalseOrderByGatheringDateDesc();

    Optional<Gathering> findByIdAndIsDeletedFalse(Long id);
}
