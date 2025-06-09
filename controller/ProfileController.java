package com.nexbus.frontend.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ProfileController {
    @GetMapping("/profile")
    public String showProfile(Model model) {
        model.addAttribute("pageTitle", "Profile");
        // Fetch profile data from backend if available
        model.addAttribute("profileData", "Admin Profile");
        return "Admin/profile";
    }
}