package com.nexbus.frontend.controller;

import com.nexbus.frontend.dto.LoginRequest;
import com.nexbus.frontend.dto.LoginResponse;
import com.nexbus.frontend.exception.ApiException;
import com.nexbus.frontend.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import jakarta.servlet.http.HttpSession;

@Controller
public class AdminAuthController {

    @Autowired
    private AuthService authService;
    
    @GetMapping("/")
    public String redirectToLogin() {
        return "redirect:/Admin/Login";
    }

    @GetMapping("/admin/login")
    public String showLoginForm(Model model, HttpSession session) {
        if (session.getAttribute("authToken") != null) {
            return "redirect:/Admin/Dashboard";
        }
        model.addAttribute("loginRequest", new LoginRequest());
        model.addAttribute("role", "admin");
        return "Admin/dashboard";
    }

    @PostMapping("/admin/login")
    public String login(@ModelAttribute("loginRequest") LoginRequest loginRequest, 
                        Model model, HttpSession session) {
        try {
            LoginResponse loginResponse = authService.login(loginRequest);
            session.setAttribute("authToken", loginResponse.getToken());
            session.setAttribute("adminRole", loginResponse.getRole());
            session.setAttribute("adminName", loginResponse.getName());
            return "redirect:/admin/dashboard";
        } catch (ApiException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("loginRequest", loginRequest);
            model.addAttribute("role", "admin");
            return "Admin/index";
        }
    }

    @GetMapping("/api/audit")
    public String showAudit(Model model, HttpSession session) {
        return authenticateAndRender("Audit", model, session);
    }

    @GetMapping("/api/profile")
    public String showProfile(Model model, HttpSession session) {
        return authenticateAndRender("profile", model, session);
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/Admin/Login";
    }

    

    private String authenticateAndRender(String viewName, Model model, HttpSession session) {
        String authToken = (String) session.getAttribute("authToken");
        if (authToken == null) {
            return "redirect:/Admin/Login";
        }
        model.addAttribute("adminName", session.getAttribute("adminName"));
        model.addAttribute("adminRole", session.getAttribute("adminRole"));
        model.addAttribute("authToken", authToken);
        return viewName;
    }
}