package org.c4marathon.assignment.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.hibernate.annotations.ColumnDefault;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Member {

	@Id @GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long memberPk;

	private String userId;

	@Enumerated(EnumType.STRING)
	private UserType userType;

	private String password;

	private String username;

	private String postalCode;

	private String address;

	private String phone;

	private LocalDate registerDate;

	@ColumnDefault("false")
	@Column(columnDefinition = "TINYINT(1)")
	private boolean isValid; // 회원탈퇴 여부를 저장합니다.

}
