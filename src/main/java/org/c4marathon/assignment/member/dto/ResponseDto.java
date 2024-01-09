package org.c4marathon.assignment.member.dto;

public class ResponseDto {

    public record LoginDto (
        String token
    ) {}
}
