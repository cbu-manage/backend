package com.example.cbumanage.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.Date;

@Entity
@Getter
@Setter
@Table(name = "member_dues", indexes = {
		@Index(name = "member_id_index", columnList = "member_id")
})
public class Dues {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long dueId;

	@Column(name = "member_id", nullable = false)
	private Long memberId;

	@Column(nullable = false)
	private String term;

	@CreatedDate
	@DateTimeFormat(pattern = "yyyy-MM-dd/HH:mm:ss")
	@Column(nullable = false)
	private LocalDateTime date;

	public Dues() {

	}
}
