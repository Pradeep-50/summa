package com.nexbus.nexbus_backend.controller;

import com.nexbus.nexbus_backend.dto.LoginRequest;
import com.nexbus.nexbus_backend.dto.LoginResponse;
import com.nexbus.nexbus_backend.dto.RegisterRequest;
import com.nexbus.nexbus_backend.dto.UserDTO;
import com.nexbus.nexbus_backend.service.AuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private AuthService authService;

    @PostMapping("/register")
    public UserDTO register(@RequestBody RegisterRequest registerRequest) {
        logger.debug("Received registration request for email: {}, role: {}", 
            registerRequest.getEmail(), registerRequest.getRoleName());
        UserDTO userDTO = authService.register(registerRequest);
        logger.info("Registration successful for email: {} with role: {}", 
            registerRequest.getEmail(), registerRequest.getRoleName() != null ? registerRequest.getRoleName() : "CUSTOMER");
        return userDTO;
    }

    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest loginRequest) {
        logger.debug("Received login request for email: {}", loginRequest.getEmail());
        LoginResponse response = authService.login(loginRequest);
        logger.info("Login successful for email: {}", loginRequest.getEmail());
        return response;
    }
}