package org.c4marathon.assignment.member.entity;

import org.c4marathon.assignment.common.entity.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

// TODO index -> memberid
@Entity
@NoArgsConstructor
@Getter
public class Member extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "member_pk", nullable = false, updatable = false)
	private long memberPk;

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

	@Builder
	public Member(String memberId, String password, String memberName, String phoneNumber, long mainAccountPk) {
		this.memberId = memberId;
		this.password = password;
		this.memberName = memberName;
		this.phoneNumber = phoneNumber;
		this.mainAccountPk = mainAccountPk;
	}

}
