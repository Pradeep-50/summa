package com.nexbus.nexbus_backend.controller;

import com.nexbus.nexbus_backend.dto.RouteDTO;
import com.nexbus.nexbus_backend.security.CustomUserDetails;
import com.nexbus.nexbus_backend.service.RouteService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/routes")
public class RouteController {

    private static final Logger logger = LoggerFactory.getLogger(RouteController.class);
    private final RouteService routeService;

    @Autowired
    public RouteController(RouteService routeService) {
        this.routeService = routeService;
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ADMIN', 'BUSOPERATOR', 'CUSTOMER')")
    public ResponseEntity<List<RouteDTO>> getAllRoutes(@AuthenticationPrincipal CustomUserDetails userDetails) {
        logger.debug("User ID: {}, Username: {}, Authorities: {}", 
            userDetails.getUserId(), userDetails.getUsername(), userDetails.getAuthorities());
        if (userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("BUSOPERATOR"))) {
            logger.debug("Fetching routes for BUSOPERATOR with userId: {}", userDetails.getUserId());
            return ResponseEntity.ok(routeService.findByOperatorId(userDetails.getUserId()));
        }
        logger.debug("Fetching all routes for user with role: {}", userDetails.getAuthorities());
        return ResponseEntity.ok(routeService.findAll());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'BUSOPERATOR', 'CUSTOMER')")
    public ResponseEntity<RouteDTO> getRouteById(
            @PathVariable @Min(value = 1, message = "ID must be positive") Integer id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        logger.debug("User ID: {}, Username: {}, Authorities: {} for route ID: {}", 
            userDetails.getUserId(), userDetails.getUsername(), userDetails.getAuthorities(), id);
        if (userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("BUSOPERATOR"))) {
            RouteDTO route = routeService.findById(id);
            if (!routeService.isRouteAccessibleByOperator(id, userDetails.getUserId())) {
                logger.warn("Access denied for BUSOPERATOR userId: {} to routeId: {}", userDetails.getUserId(), id);
                throw new IllegalStateException("Not authorized to access this route");
            }
            return ResponseEntity.ok(route);
        }
        return ResponseEntity.ok(routeService.findById(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<RouteDTO> createRoute(@Valid @RequestBody RouteDTO routeDTO) {
        logger.debug("Creating route: {}", routeDTO.getRouteName());
        return ResponseEntity.status(201).body(routeService.save(routeDTO));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<RouteDTO> updateRoute(
            @PathVariable @Min(value = 1, message = "ID must be positive") Integer id,
            @Valid @RequestBody RouteDTO routeDTO) {
        logger.debug("Updating route with id: {}", id);
        return ResponseEntity.ok(routeService.update(id, routeDTO));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Void> deleteRoute(
            @PathVariable @Min(value = 1, message = "ID must be positive") Integer id) {
        logger.debug("Deleting route with id: {}", id);
        routeService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}