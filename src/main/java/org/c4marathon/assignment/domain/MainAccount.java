package org.c4marathon.assignment.domain;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "main_account")
@Getter
@NoArgsConstructor
public class MainAccount {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "main_account_id", nullable = false)
	private Integer id;

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@Column(name = "account_number", nullable = false, length = 15)
	private String accountNumber;

	@Column(name = "balance", nullable = false)
	private int balance;

	@Column(name = "limit", nullable = false)
	private int limit;

	@Column(name = "create_date", nullable = false)
	private LocalDateTime createDate;
}
