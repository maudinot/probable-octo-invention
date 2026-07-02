package com.github.maudinot.octo_invention.rest;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import com.github.maudinot.octo_invention.domain.User;
import com.github.maudinot.octo_invention.service.UserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequestMapping("/api")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PutMapping(value = "/users/{operatorid}", produces = "application/json")
    public ResponseEntity<?> deleteUser(@PathVariable("operatorid") String operatorId) {
        userService.deleteUser(operatorId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping(value = "/users/register", produces = "application/json")
    public ResponseEntity<?> registerUser(@RequestBody User user) {
        log.info("User tried to register: {}", user.getName());
        userService.registerUser(user.getName(), user.getPassword());
        return ResponseEntity.ok().build();
    }
}
