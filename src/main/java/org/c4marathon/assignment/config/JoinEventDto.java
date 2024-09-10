package org.c4marathon.assignment.config;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.c4marathon.assignment.user.domain.User;

@Getter
@AllArgsConstructor
public class JoinEventDto {
    private User user;
}
