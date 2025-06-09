package com.nexbus.frontend.controller;

import com.nexbus.frontend.dto.BusDTO;
import com.nexbus.frontend.dto.RouteDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;

@Controller
@RequestMapping("/routes")
public class OperatorRouteController {

    @Autowired
    private RestTemplate restTemplate;

    @Value("${api.base.url:http://localhost:9090/api}")
    private String apiBaseUrl;

    @Value("${operator.base.url:http://localhost:9090/bus-operators}")
    private String operatorBaseUrl;

    @GetMapping
    public String getRoutesPage(Model model, Authentication authentication) {
        String operatorId = authentication.getName();
        HttpHeaders headers = createHeaders(authentication);
        HttpEntity<?> entity = new HttpEntity<>(headers);

        // Fetch routes
        String routesUrl = apiBaseUrl + "/routes";
        ResponseEntity<RouteDTO[]> routesResponse = restTemplate.exchange(routesUrl, HttpMethod.GET, entity, RouteDTO[].class);
        model.addAttribute("routes", Arrays.asList(routesResponse.getBody()));

        // Fetch operator buses
        String busesUrl = operatorBaseUrl + "/" + operatorId + "/buses";
        ResponseEntity<BusDTO[]> busesResponse = restTemplate.exchange(busesUrl, HttpMethod.GET, entity, BusDTO[].class);
        model.addAttribute("buses", Arrays.asList(busesResponse.getBody()));
        model.addAttribute("activePage", "routes");
        return "Operator/OperatorRoutes";
    }

    @PostMapping("/assign")
    public String assignRoute(@RequestParam Long routeId, @RequestParam Long busId, Authentication authentication) {
        HttpHeaders headers = createHeaders(authentication);
        HttpEntity<?> entity = new HttpEntity<>(headers);
        String url = apiBaseUrl + "/buses/" + busId + "/route?routeId=" + routeId;
        restTemplate.exchange(url, HttpMethod.PUT, entity, Void.class);
        return "redirect:/Operators/OperatorsRoutes";
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