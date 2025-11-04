package com.example.cbumanage.repository;

import com.example.cbumanage.model.CbuMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CbuMemberRepository extends JpaRepository<CbuMember, Long> {
    @Override
    void deleteAll();

    @Query("SELECT m FROM CbuMember m WHERE m.cbuMemberId NOT IN (SELECT d.memberId FROM Dues d WHERE d.term = :term)")
    List<CbuMember> findAllWithoutDues(@Param("term") String term);

    Optional<CbuMember> findByStudentNumber(Long studentNumber);

    CbuMember findCbuMemberByStudentNumber(Long studentNumber);

    @Query("select cbuMember.name from CbuMember cbuMember WHERE cbuMember.studentNumber = :studentNumber")
    String findNameByStudentNumber(@Param("studentNumber") Long studentNumber);

    void deleteByStudentNumber(Long studentNumber);
}





