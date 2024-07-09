package org.c4marathon.assignment.global.exception;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;


@Getter
@Builder
@RequiredArgsConstructor
public class ErrorResponse {

    private final String code;
    private final String message;

}
