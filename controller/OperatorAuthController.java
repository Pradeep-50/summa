package com.nexbus.frontend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

@Controller
public class OperatorAuthController {

    @Autowired
    private RestTemplate restTemplate;

    @Value("${api.base.url:http://localhost:9090/api}")
    private String apiBaseUrl;

    @GetMapping("/operator/login")
    public String getLoginPage(Model model) {
        model.addAttribute("error", null);
        model.addAttribute("role", "operator");
        return "Operator/index1";
    }

    @PostMapping("/operator/login")
    public String login(@RequestParam String email, @RequestParam String password, Model model) {
        try {
            String url = apiBaseUrl + "/auth/login";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<?> entity = new HttpEntity<>(new LoginRequest(email, password), headers);
            ResponseEntity<LoginResponse> response = restTemplate.exchange(url, HttpMethod.POST, entity, LoginResponse.class);
            // Token handled by frontend (scripts.js)
            return "redirect:/Operator/Dashboard";
        } catch (Exception e) {
            model.addAttribute("error", "Login failed: " + e.getMessage());
            model.addAttribute("role", "operator");
            return "Operator/index1";
        }
    }

    private static class LoginRequest {
        private String email;
        private String password;

        public LoginRequest(String email, String password) {
            this.email = email;
            this.password = password;
        }

        public String getEmail() {
            return email;
        }

        public String getPassword() {
            return password;
        }
    }

    private static class LoginResponse {
        private String token;

        public String getToken() {
            return token;
        }

        public void setToken(String token) {
            this.token = token;
        }
    }
}