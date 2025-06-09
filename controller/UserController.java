package com.nexbus.nexbus_backend.controller;

import com.nexbus.nexbus_backend.dto.UserDTO;
import com.nexbus.nexbus_backend.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserService userService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<UserDTO> getAllUsers() {
        logger.debug("Fetching all users");
        List<UserDTO> users = userService.findAll();
        logger.info("Retrieved {} users", users.size());
        return users;
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public UserDTO getUserById(@PathVariable Integer id) {
        logger.debug("Fetching user with ID: {}", id);
        UserDTO user = userService.findById(id);
        logger.info("Found user with ID: {}", id);
        return user;
    }
}