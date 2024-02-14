package org.c4marathon.assignment.user.dto;

import java.util.ArrayList;
import java.util.List;

import org.c4marathon.assignment.user.entity.Authority;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
public class LoginResponse {

	private Long id;
	private String userEmail;
	private String userName;

	@Builder.Default
	private List<Authority> roles = new ArrayList<>();
	private String token;

	@Builder
	public LoginResponse(Long id, String userEmail, String userName, List<Authority> roles, String token) {
		this.id = id;
		this.userEmail = userEmail;
		this.userName = userName;
		this.roles = roles;
		this.token = token;
	}

}
