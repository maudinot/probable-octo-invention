package com.github.maudinot.octo_invention.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.github.maudinot.octo_invention.domain.User;
import com.github.maudinot.octo_invention.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    
    public void deleteUser(String operatorName) {
        userRepository.delete(new User(operatorName, ""));
        log.info("User deleted - {}", operatorName);
    }

    public void registerUser(String name, String password) {
        //check is username is already taker
        if(userRepository.existsByName(name)) {
            return;
        }
        //Now we can register the new user
        User user = new User(name, 
            passwordEncoder.encode(password));
        userRepository.save(user);
        log.info("User registered - {}", name);
    }
}
