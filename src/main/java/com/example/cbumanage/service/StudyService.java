package com.example.cbumanage.service;

import com.example.cbumanage.dto.StudyCreateDTO;
import com.example.cbumanage.repository.StudyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class StudyService {
	private StudyRepository studyRepository;

	@Autowired
	public StudyService(StudyRepository studyRepository) {
		this.studyRepository = studyRepository;
	}

	public void createStudy(StudyCreateDTO createDTO) {

	}

}
