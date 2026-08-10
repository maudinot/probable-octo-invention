package com.github.maudinot.octo_invention.rest;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.github.maudinot.octo_invention.domain.User;
import com.github.maudinot.octo_invention.service.UserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequestMapping("/api")
@RequiredArgsConstructor
@RestController
public class UserController {

    private final UserService userService;
    @DeleteMapping(value = "/users/{operatorid}", produces = "application/json")
    public ResponseEntity<?> deleteUser(@PathVariable("operatorid") String operatorId) {
        userService.deleteUser(operatorId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping(value = "/users", produces = "application/json")
    public ResponseEntity<?> registerUser(@RequestBody User user) {
        log.info("User tried to register: {}", user.getName());
        userService.registerUser(user.getName(), user.getPassword());
        return ResponseEntity.ok().build();
    }
}
