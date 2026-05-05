package com.example.cbumanage.dues.service;

import com.example.cbumanage.dues.entity.Dues;
import com.example.cbumanage.dues.repository.DuesRepository;
import com.example.cbumanage.user.entity.User;
import com.example.cbumanage.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.atomic.AtomicReference;

@Service
public class DuesService {
	private UserRepository userRepository;
	private DuesRepository duesRepository;

	public DuesService(UserRepository userRepository, DuesRepository duesRepository) {
		this.userRepository = userRepository;
		this.duesRepository = duesRepository;
	}


	@Transactional
	public boolean addDues(long userId, String term) {
		return addDues(userRepository.findById(userId).orElseGet(() -> null), term);
	}

	@Transactional
	public boolean addDues(User user, String term) {
		if (user == null) {
			return false;
		}

		Dues dues = new Dues();
		dues.setUserId(user.getUserId());
		dues.setTerm(term);
		duesRepository.save(dues);
		return true;
	}

	@Transactional
	public boolean removeDues(long userId, String term) {
		AtomicReference<Boolean> success = new AtomicReference<>(true);
		userRepository.findById(userId).ifPresentOrElse(
				user -> duesRepository.findByUserIdAndTerm(user.getUserId(), term).ifPresentOrElse(duesRepository::delete,() -> success.set(false))
				, () -> success.set(false));
		return success.get();
	}
}
