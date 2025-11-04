package com.example.cbumanage.model;

import com.example.cbumanage.model.enums.StudyType;
import jakarta.persistence.*;

@Entity
@Table(name = "cbu_study")
public class Study {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long studyId;

	private StudyType studyType;

	@Column(name = "study_name")
	private String studyName;

	private Long startGeneration;
	private Long endGeneration;
}
