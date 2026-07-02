package com.github.maudinot.octo_invention.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.github.maudinot.octo_invention.domain.User;
import com.github.maudinot.octo_invention.repository.UserRepository;

public class UserServiceTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

     @BeforeEach
    void setUp() {
        reset(userRepository);
    }

    @Test
    void testRegisterUser_ShouldSaveUser() {
        // Given
        String name = "testuser";
        String password = "password123";

        // When
        userService.registerUser(name, password);

        // Then
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void testRegisterUser_ShouldHashPassword() {
        String name = "testuser";
        String password = "password123";
        String mockedHashedPassword =  "hashed";
        doAnswer(invocation -> mockedHashedPassword).when(passwordEncoder).encode(any());

        userService.registerUser(name, password);

        // Verify the repository was called with hashed user
        verify(userRepository, times(1)).save(argThat(user -> 
            user.getName().equals(name) && user.getPassword().equals(mockedHashedPassword)
        ));
    }
}
