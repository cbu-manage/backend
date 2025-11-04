package com.example.cbumanage.service;

import com.example.cbumanage.model.CbuMember;
import com.example.cbumanage.model.Dues;
import com.example.cbumanage.repository.CbuMemberRepository;
import com.example.cbumanage.repository.DuesRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.atomic.AtomicReference;

@Service
public class DuesService {
	private CbuMemberRepository memberRepository;
	private DuesRepository duesRepository;

	public DuesService(CbuMemberRepository memberRepository, DuesRepository duesRepository) {
		this.memberRepository = memberRepository;
		this.duesRepository = duesRepository;
	}


	@Transactional
	public boolean addDues(long memberId, String term) {
		return addDues(memberRepository.findById(memberId).orElseGet(() -> null), term);
	}

	@Transactional
	public boolean addDues(CbuMember member, String term) {
		if (member == null) {
			return false;
		}

		Dues dues = new Dues();
		dues.setMemberId(member.getCbuMemberId());
		dues.setTerm(term);
		duesRepository.save(dues);
		return true;
	}

	@Transactional
	public boolean removeDues(long memberId, String term) {
		AtomicReference<Boolean> success = new AtomicReference<>(true);
		memberRepository.findById(memberId).ifPresentOrElse(
				member -> duesRepository.findByMemberIdAndTerm(member.getCbuMemberId(), term).ifPresentOrElse(duesRepository::delete,() -> success.set(false))
				, () -> success.set(false));
		return success.get();
	}
}
