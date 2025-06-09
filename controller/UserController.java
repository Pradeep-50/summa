package com.nexbus.frontend.controller;

import com.nexbus.frontend.dto.RegisterRequest;
import com.nexbus.frontend.service.UserService;
import com.nexbus.frontend.exception.ApiException; // Import the ApiException class
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ModelAttribute;

@Controller
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("user", new RegisterRequest());
        return "register"; // Thymeleaf template name for registration
    }

    @PostMapping("/register")
    public String registerUser (@ModelAttribute("user") RegisterRequest registerRequest, Model model) {
        try {
            userService.registerUser (registerRequest);
            return "redirect:/login"; // Redirect to login page after successful registration
        } catch (ApiException e) {
            model.addAttribute("error", e.getMessage());
            return "register"; // Redirect back to the registration page with an error
        }
    }
}
