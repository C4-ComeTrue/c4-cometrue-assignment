package org.c4marathon.assignment.domain;

import java.time.LocalDate;
import java.util.List;

import org.hibernate.annotations.ColumnDefault;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Setter
@Getter
@NoArgsConstructor
public class Member {

	@Column
	@Id @GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long memberPk;

	@NotBlank
	private String userId; // 아이디, 비밀번호 중 아이디에 해당함.

	@NotNull
	@Enumerated(EnumType.STRING)
	private MemberType memberType;

	@NotBlank
	private String password;

	@NotBlank
	private String username;

	@NotBlank
	private String postalCode;

	@NotBlank
	private String address;

	@NotBlank
	private String phone;

	@NotNull
	private LocalDate registerDate;

	@OneToMany
	private List<CartItem> cartItem;

	@ColumnDefault("false")
	@Column(columnDefinition = "TINYINT(1)")
	private boolean isValid; // 회원탈퇴 여부를 저장합니다.

}
