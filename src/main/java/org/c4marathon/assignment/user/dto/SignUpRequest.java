package org.c4marathon.assignment.user.dto;

public record SignUpRequest(String userName, String userEmail, String password) {

    public static SignUpRequest create(String userName, String userEmail, String password) {
        return new SignUpRequest(userName, userEmail, password);
    }
}
