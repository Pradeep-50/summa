package com.nexbus.frontend.controller;

import com.nexbus.frontend.dto.SeatDTO;
import com.nexbus.frontend.dto.BusDTO;
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
@RequestMapping("/seats")
public class SeatController {

    @Autowired
    private RestTemplate restTemplate;

    @Value("${api.base.url:http://localhost:9090/api}")
    private String apiBaseUrl;

    @Value("${operator.base.url:http://localhost:9090/bus-operators}")
    private String operatorBaseUrl;

    @GetMapping
    public String getSeatsPage(Model model, Authentication authentication) {
        String operatorId = authentication.getName(); // userId from JWT
        HttpHeaders headers = createHeaders(authentication);
        HttpEntity<?> entity = new HttpEntity<>(headers);

        // Fetch seats
        String seatsUrl = apiBaseUrl + "/seats";
        ResponseEntity<SeatDTO[]> seatsResponse = restTemplate.exchange(seatsUrl, HttpMethod.GET, entity, SeatDTO[].class);
        List<SeatDTO> seats = Arrays.asList(seatsResponse.getBody());

        // Fetch operator buses
        String busesUrl = operatorBaseUrl + "/" + operatorId + "/buses";
        ResponseEntity<BusDTO[]> busesResponse = restTemplate.exchange(busesUrl, HttpMethod.GET, entity, BusDTO[].class);
        model.addAttribute("seats", seats);
        model.addAttribute("buses", Arrays.asList(busesResponse.getBody()));
        model.addAttribute("activePage", "seats");
        return "Admin/seats";
    }

    @PostMapping("/add")
    public String addSeat(@ModelAttribute SeatDTO seatDTO, Authentication authentication) {
        String operatorId = authentication.getName();
        HttpHeaders headers = createHeaders(authentication);
        HttpEntity<SeatDTO> entity = new HttpEntity<>(seatDTO, headers);
        String url = apiBaseUrl + "/seats";
        restTemplate.exchange(url, HttpMethod.POST, entity, Void.class);
        return "redirect:/Admin/seats";
    }

    @PostMapping("/update")
    public String updateSeat(@ModelAttribute SeatDTO seatDTO, Authentication authentication) {
        String operatorId = authentication.getName();
        HttpHeaders headers = createHeaders(authentication);
        HttpEntity<SeatDTO> entity = new HttpEntity<>(seatDTO, headers);
        String url = apiBaseUrl + "/seats/" + seatDTO.getSeatId();
        restTemplate.exchange(url, HttpMethod.PUT, entity, Void.class);
        return "redirect:/Admin/seats";
    }

    @PostMapping("/delete/{id}")
    @ResponseBody
    public void deleteSeat(@PathVariable Long id, Authentication authentication) {
        String operatorId = authentication.getName();
        HttpHeaders headers = createHeaders(authentication);
        HttpEntity<?> entity = new HttpEntity<>(headers);
        String url = apiBaseUrl + "/seats/" + id;
        restTemplate.exchange(url, HttpMethod.DELETE, entity, Void.class);
    }

    @GetMapping("/bus/{busId}")
    @ResponseBody
    public List<SeatDTO> getSeatsByBusId(@PathVariable Long busId, Authentication authentication) {
        String operatorId = authentication.getName();
        HttpHeaders headers = createHeaders(authentication);
        HttpEntity<?> entity = new HttpEntity<>(headers);
        String url = apiBaseUrl + "/seats/bus/" + busId;
        ResponseEntity<SeatDTO[]> response = restTemplate.exchange(url, HttpMethod.GET, entity, SeatDTO[].class);
        return Arrays.asList(response.getBody());
    }

    private HttpHeaders createHeaders(Authentication authentication) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        // Fetch JWT token from authentication context
        String token = (String) authentication.getCredentials(); // Assumes token is stored in credentials
        if (token != null) {
            headers.set("Authorization", "Bearer " + token);
        }
        return headers;
    }
}