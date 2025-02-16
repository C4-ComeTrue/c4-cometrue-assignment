package org.c4marathon.assignment.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Entity
@Table(name = "user")
public class User extends BaseEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "user_id", nullable = false)
	private Long id;

	@Size(max = 30)
	@NotNull
	@Column(name = "username", nullable = false, length = 30)
	private String username;

	@Size(max = 30)
	@NotNull
	@Column(name = "email", nullable = false, length = 30)
	private String email;

	@Size(max = 10)
	@NotNull
	@Column(name = "nickname", nullable = false, length = 10)
	private String nickname;

	@Column(name = "main_account_id")
	private Long mainAccountId;

	@Builder
	public User(String username, String email, String nickname) {
		this.username = username;
		this.email = email;
		this.nickname = nickname;
	}

	public void changeMainAccount(Long mainAccountId) {
		this.mainAccountId = mainAccountId;
	}
}
