package com.example.cbumanage.dues.repository;

import com.example.cbumanage.dues.entity.Dues;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DuesRepository extends JpaRepository<Dues, Long> {
	Optional<Dues> findByUserIdAndTerm(Long userId, String term);
	List<Dues> findAllByUserId(Long userId);
}
