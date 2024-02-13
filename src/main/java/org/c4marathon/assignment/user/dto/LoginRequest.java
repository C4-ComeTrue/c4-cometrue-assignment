package org.c4marathon.assignment.user.dto;
public record LoginRequest(String userEmail, String password) {
	public static LoginRequest create(String userEmail, String password) {
		return new LoginRequest(userEmail, password);
	}
}