package com.nexbus.frontend.controller;

import com.nexbus.frontend.dto.AmenitiesDTO;
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
@RequestMapping("/buses")
public class OperatorBusController {

    @Autowired
    private RestTemplate restTemplate;

    @Value("${api.base.url:http://localhost:9090/api}")
    private String apiBaseUrl;

    @Value("${operator.base.url:http://localhost:9090/bus-operators}")
    private String operatorBaseUrl;

    @GetMapping
    public String getBusesPage(Model model, Authentication authentication) {
        String operatorId = authentication.getName();
        HttpHeaders headers = createHeaders(authentication);
        HttpEntity<?> entity = new HttpEntity<>(headers);

        // Fetch buses
        String busesUrl = operatorBaseUrl + "/" + operatorId + "/buses";
        ResponseEntity<BusDTO[]> busesResponse = restTemplate.exchange(busesUrl, HttpMethod.GET, entity, BusDTO[].class);
        model.addAttribute("buses", Arrays.asList(busesResponse.getBody()));

        // Fetch routes
        String routesUrl = apiBaseUrl + "/routes";
        ResponseEntity<RouteDTO[]> routesResponse = restTemplate.exchange(routesUrl, HttpMethod.GET, entity, RouteDTO[].class);
        model.addAttribute("routes", Arrays.asList(routesResponse.getBody()));

        // Fetch amenities
        String amenitiesUrl = apiBaseUrl + "/amenities";
        ResponseEntity<AmenitiesDTO[]> amenitiesResponse = restTemplate.exchange(amenitiesUrl, HttpMethod.GET, entity, AmenitiesDTO[].class);
        model.addAttribute("amenities", Arrays.asList(amenitiesResponse.getBody()));
        model.addAttribute("activePage", "buses");
        return "Operator/OperatorBuses";
    }

    @PostMapping("/add")
    public String addBus(@ModelAttribute BusDTO busDTO, Authentication authentication) {
        HttpHeaders headers = createHeaders(authentication);
        HttpEntity<BusDTO> entity = new HttpEntity<>(busDTO, headers);
        String url = apiBaseUrl + "/buses";
        restTemplate.exchange(url, HttpMethod.POST, entity, Void.class);
        return "redirect:/Operator/OperatorBuses";
    }

    @PostMapping("/update")
    public String updateBus(@ModelAttribute BusDTO busDTO, Authentication authentication) {
        HttpHeaders headers = createHeaders(authentication);
        HttpEntity<BusDTO> entity = new HttpEntity<>(busDTO, headers);
        String url = apiBaseUrl + "/buses/" + busDTO.getBusId();
        restTemplate.exchange(url, HttpMethod.PUT, entity, Void.class);
        return "redirect:/Operator/OperatorBuses";
    }

    @PostMapping("/delete/{id}")
    @ResponseBody
    public void deleteBus(@PathVariable Long id, Authentication authentication) {
        HttpHeaders headers = createHeaders(authentication);
        HttpEntity<?> entity = new HttpEntity<>(headers);
        String url = apiBaseUrl + "/buses/" + id;
        restTemplate.exchange(url, HttpMethod.DELETE, entity, Void.class);
    }

    @PostMapping("/assign-route")
    public String assignRoute(@RequestParam Long busId, @RequestParam Long routeId, Authentication authentication) {
        HttpHeaders headers = createHeaders(authentication);
        HttpEntity<?> entity = new HttpEntity<>(headers);
        String url = apiBaseUrl + "/buses/" + busId + "/route?routeId=" + routeId;
        restTemplate.exchange(url, HttpMethod.PUT, entity, Void.class);
        return "redirect:Operator/OperatorBuses";
    }

    @PostMapping("/assign-amenity")
    public String assignAmenity(@RequestParam Long busId, @RequestParam Long amenityId, Authentication authentication) {
        HttpHeaders headers = createHeaders(authentication);
        HttpEntity<?> entity = new HttpEntity<>(headers);
        String url = apiBaseUrl + "/buses/" + busId + "/amenity?amenityId=" + amenityId;
        restTemplate.exchange(url, HttpMethod.PUT, entity, Void.class);
        return "redirect:/Operator/OperatorBuses";
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