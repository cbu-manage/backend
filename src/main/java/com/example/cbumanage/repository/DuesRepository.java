package com.example.cbumanage.repository;

import com.example.cbumanage.model.Dues;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DuesRepository extends JpaRepository<Dues, Long> {
	Optional<Dues> findByMemberIdAndTerm(Long memberId, String term);
	List<Dues> findAllByMemberId(Long memberId);
}
