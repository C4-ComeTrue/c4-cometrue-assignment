package org.c4marathon.assignment.domain;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "user")
@Getter
@NoArgsConstructor
public class User {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "user_id", nullable = false)
	private Integer id;

	@Column(name = "username", nullable = false, length = 30)
	private String username;

	@Column(name = "create_date", nullable = false)
	private LocalDateTime  createDate;

	@Column(name = "update_date", nullable = false)
	private LocalDateTime updateDate;
}
