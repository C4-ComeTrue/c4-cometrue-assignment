package org.c4marathon.assignment.user.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.c4marathon.assignment.accounts.entity.Account;
import org.c4marathon.assignment.accounts.entity.AccountType;
import org.c4marathon.assignment.accounts.service.AccountServiceImpl;
import org.c4marathon.assignment.user.service.UserServiceImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.c4marathon.assignment.user.entity.UserEntity;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
@Slf4j
public class UserController {

    private final UserServiceImpl userService;
    private final AccountServiceImpl accountService;

    @PostMapping("/join")
    public ResponseEntity<?> save(@RequestBody UserEntity userEntity) {
        System.out.println(userEntity);
        Account result = userService.join(userEntity);

        return new ResponseEntity<Account>(result,HttpStatus.OK);
    }

    @GetMapping("/add")
    public ResponseEntity<?> addAcount(@RequestParam(name="id") String id, @RequestParam(name="type")AccountType type) {

        Account result = accountService.addAcount(id,type);

        return new ResponseEntity<Account>(result,HttpStatus.OK);
    }

    @GetMapping("/charge")
    public ResponseEntity<?> chargeBalance(@RequestParam Map<String,String> map) throws Exception {

        Account result = accountService.chargeBalance(map);


        return new ResponseEntity<Account>(result,HttpStatus.OK);
    }

    @PostMapping("/send")
    public ResponseEntity<?> send(@RequestParam Map<String,String> map) throws Exception {

        Account result = accountService.transfer(map);

        return new ResponseEntity<Account>(result,HttpStatus.OK);
    }
}
