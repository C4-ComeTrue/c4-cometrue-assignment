package org.c4marathon.assignment.domain;

import java.time.LocalDate;
import java.util.List;

import org.checkerframework.checker.units.qual.C;
import org.hibernate.annotations.ColumnDefault;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
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

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "member_pk")
	private Long memberPk;

	@Column(name = "user_id")
	@NotBlank
	private String userId; // 아이디, 비밀번호 중 아이디에 해당함.

	@Column(name = "member_type")
	@NotNull
	@Enumerated(EnumType.STRING)
	private MemberType memberType;

	@Column(name = "password")
	@NotBlank
	private String password;

	@Column(name = "username")
	@NotBlank
	private String username;

	@Column(name = "postal_code")
	@NotBlank
	private String postalCode;

	@Column(name = "address")
	@NotBlank
	private String address;

	@Column(name = "phone")
	@NotBlank
	private String phone;

	@Column(name = "register_date")
	@NotNull
	private LocalDate registerDate;

	@OneToMany(mappedBy = "customer")
	private List<CartItem> cartItem;

	@ColumnDefault("false")
	@Column(name = "is_valid", columnDefinition = "TINYINT(1)")
	private boolean isValid; // 회원탈퇴 여부를 저장합니다.

}
