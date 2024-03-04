package org.c4marathon.assignment.member.entity;

import java.util.ArrayList;
import java.util.List;

import org.c4marathon.assignment.bankaccount.entity.SavingAccount;
import org.c4marathon.assignment.common.entity.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
@Getter
public class Member extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "member_pk", nullable = false, updatable = false)
	private long memberPk;

	// 유니크 인덱스 자동 생성 됨. 아이디는 unique해야 하므로 자동 생성 인덱스를 사용해야 함.
	@Column(name = "member_id", unique = true, length = 50, nullable = false, updatable = false)
	private String memberId;

	@Column(name = "password", length = 255, nullable = false)
	private String password;

	@Column(name = "member_name", length = 50, nullable = false)
	private String memberName;

	@Column(name = "phone_number", length = 11, nullable = false)
	private String phoneNumber;

	@Column(name = "main_account_pk", nullable = false)
	private long mainAccountPk;

	@OneToMany(mappedBy = "member")
	List<SavingAccount> savingAccounts = new ArrayList<>();

	@Builder
	public Member(String memberId, String password, String memberName, String phoneNumber, long mainAccountPk) {
		this.memberId = memberId;
		this.password = password;
		this.memberName = memberName;
		this.phoneNumber = phoneNumber;
		this.mainAccountPk = mainAccountPk;
	}

}
