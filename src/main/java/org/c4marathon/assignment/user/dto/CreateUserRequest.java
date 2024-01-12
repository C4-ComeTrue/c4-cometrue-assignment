package org.c4marathon.assignment.user.dto;

public record CreateUserRequest(String userName, String userEmail, String password) {

    public static CreateUserRequest create(String userName, String userEmail, String password) {
        return new CreateUserRequest(userName, userEmail, password);
    }
}
