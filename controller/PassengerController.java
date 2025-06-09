package com.nexbus.nexbus_backend.controller;

import com.nexbus.nexbus_backend.dto.PassengerDTO;
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
@RequestMapping("/api/passengers")
public class PassengerController {

    private static final Logger logger = LoggerFactory.getLogger(PassengerController.class);

    @Autowired
    private UserService userService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('BUSOPERATOR')")
    public List<PassengerDTO> getAllPassengers() {
        logger.debug("Fetching all passengers");
        List<PassengerDTO> passengers = userService.findAllPassengers();
        logger.info("Retrieved {} passengers", passengers.size());
        return passengers;
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('BUSOPERATOR')")
    public PassengerDTO getPassengerById(@PathVariable Integer id) {
        logger.debug("Fetching passenger with ID: {}", id);
        PassengerDTO passenger = userService.findPassengerById(id);
        logger.info("Found passenger with ID: {}", id);
        return passenger;
    }
}