package org.c4marathon.assignment.adjustment.controller;

import org.c4marathon.assignment.adjustment.dto.res.AdjustResponseDto;
import org.c4marathon.assignment.adjustment.service.AdjustService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/adjust")
public class AdjustController {

    private final AdjustService adjustService;

    // 정산 조회(PK)
    @GetMapping("{id}")
    public AdjustResponseDto getAdjust(@PathVariable Long id) {
        return adjustService.getAdjust(id);
    }

    // 정산 하기 생성 요청
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping("")
    public void postAdjust() {

    }
    
    // 정산 완료 요청
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PatchMapping("")
    public void patchAdjust() {

    }
    
    // 정산 삭제 요청
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("")
    public void deleteAdjust() {

    }

    // 정산 받기 요청
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping("")
    public void postAdjustAndAdjustTarget() {

    }
}
