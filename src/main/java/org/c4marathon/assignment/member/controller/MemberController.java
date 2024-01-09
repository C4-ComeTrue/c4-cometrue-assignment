package org.c4marathon.assignment.member.controller;

import org.c4marathon.assignment.member.dto.RequestDto;
import org.c4marathon.assignment.member.service.MemberService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/members")
public class MemberController {

    private final MemberService memberService;

    @Operation(summary = "회원가입")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping("/join")
    public void join (
        @Valid
        @RequestBody
        RequestDto.JoinDto joinDto
    ) {
        memberService.join(joinDto);
    }
}
