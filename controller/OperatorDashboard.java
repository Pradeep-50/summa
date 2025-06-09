package com.nexbus.frontend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.client.RestTemplate;

import com.nexbus.frontend.dto.DashboardMetricsDTO;

@Controller
@RequestMapping("/operator/dashboard")
public class OperatorDashboard {

    @Autowired
    private RestTemplate restTemplate;

    @Value("${api.base.url:http://localhost:9090/api}")
    private String apiBaseUrl;

    @GetMapping
    public String getOperatorDashboardPage(Model model, Authentication authentication) {
        HttpHeaders headers = createHeaders(authentication);
        HttpEntity<?> entity = new HttpEntity<>(headers);

        // Fetch operator-specific metrics
        String metricsUrl = apiBaseUrl + "/operator/dashboard/metrics";
        try {
            DashboardMetricsDTO metrics = restTemplate.exchange(metricsUrl, HttpMethod.GET, entity, DashboardMetricsDTO.class).getBody();
            model.addAttribute("metrics", metrics);
        } catch (Exception e) {
            model.addAttribute("error", "Failed to load dashboard metrics");
        }

        model.addAttribute("activePage", "dashboard");
        return "Operator/OperatorDashboard";
    }

    private HttpHeaders createHeaders(Authentication authentication) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        String token = (String) authentication.getCredentials();
        if (token != null) {
            headers.set("Authorization", "Bearer " + token);
        }
        return headers;
    }
}