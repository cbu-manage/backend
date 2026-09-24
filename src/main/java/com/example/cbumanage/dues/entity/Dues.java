package com.example.cbumanage.dues.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.Date;

@Entity
@Getter
@Setter
@Table(name = "member_dues", indexes = {
		@Index(name = "user_id_index", columnList = "user_id")
})
// 이 리스너가 없으면 @CreatedDate 가 동작하지 않아 date 가 null 로 남고,
// NOT NULL 컬럼이라 저장이 실패한다. 다른 엔티티와 같은 방식으로 맞춘다.
@EntityListeners(AuditingEntityListener.class)
public class Dues {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long dueId;

	@Column(name = "user_id", nullable = false)
	private Long userId;

	@Column(nullable = false)
	private String term;

	@CreatedDate
	@DateTimeFormat(pattern = "yyyy-MM-dd/HH:mm:ss")
	@Column(nullable = false)
	private LocalDateTime date;

	public Dues() {

	}
}
