package com.nexbus.nexbus_backend.controller;

import com.nexbus.nexbus_backend.dto.DriverDTO;
import com.nexbus.nexbus_backend.security.CustomUserDetails;
import com.nexbus.nexbus_backend.service.BusService;
import com.nexbus.nexbus_backend.service.DriverService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/drivers")
public class DriverController {

    private static final Logger logger = LoggerFactory.getLogger(DriverController.class);

    private final DriverService driverService;
    private final BusService busService;

    public DriverController(DriverService driverService, BusService busService) {
        this.driverService = driverService;
        this.busService = busService;
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ADMIN', 'BUSOPERATOR')")
    public ResponseEntity<List<DriverDTO>> getAllDrivers(@AuthenticationPrincipal CustomUserDetails userDetails) {
        logger.debug("User ID: {} (Role: {}) fetching all drivers", userDetails.getUserId(), userDetails.getAuthorities());
        List<DriverDTO> drivers = driverService.findAll();
        logger.info("User ID: {} retrieved {} drivers", userDetails.getUserId(), drivers.size());
        return ResponseEntity.ok(drivers);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'BUSOPERATOR')")
    public ResponseEntity<DriverDTO> getDriverById(
            @PathVariable @Positive(message = "ID must be positive") Integer id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        logger.debug("User ID: {} (Role: {}) fetching driver ID: {}", userDetails.getUserId(), userDetails.getAuthorities(), id);
        DriverDTO driver = driverService.findById(id);
        if (userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("BUSOPERATOR"))) {
            // Check if driver is assigned to a bus owned by the operator
            validateDriverAccess(id, userDetails.getUserId());
        }
        logger.info("User ID: {} retrieved driver ID: {}", userDetails.getUserId(), id);
        return ResponseEntity.ok(driver);
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('ADMIN', 'BUSOPERATOR')")
    public ResponseEntity<DriverDTO> createDriver(
            @Valid @RequestBody DriverDTO driverDTO,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        logger.debug("User ID: {} (Role: {}) creating driver", userDetails.getUserId(), userDetails.getAuthorities());
        DriverDTO createdDriver = driverService.save(driverDTO);
        logger.info("User ID: {} created driver ID: {}", userDetails.getUserId(), createdDriver.getDriverId());
        return ResponseEntity.status(HttpStatus.CREATED).body(createdDriver);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'BUSOPERATOR')")
    public ResponseEntity<DriverDTO> updateDriver(
            @PathVariable @Positive(message = "ID must be positive") Integer id,
            @Valid @RequestBody DriverDTO driverDTO,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        logger.debug("User ID: {} (Role: {}) updating driver ID: {}", userDetails.getUserId(), userDetails.getAuthorities(), id);
        if (userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("BUSOPERATOR"))) {
            validateDriverAccess(id, userDetails.getUserId());
        }
        DriverDTO updatedDriver = driverService.update(id, driverDTO);
        logger.info("User ID: {} updated driver ID: {}", userDetails.getUserId(), id);
        return ResponseEntity.ok(updatedDriver);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'BUSOPERATOR')")
    public ResponseEntity<Void> deleteDriver(
            @PathVariable @Positive(message = "ID must be positive") Integer id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        logger.debug("User ID: {} (Role: {}) deleting driver ID: {}", userDetails.getUserId(), userDetails.getAuthorities(), id);
        if (userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("BUSOPERATOR"))) {
            validateDriverAccess(id, userDetails.getUserId());
            // Check if driver is assigned to a bus
            if (driverService.isDriverAssigned(id)) {
                logger.warn("User ID: {} cannot delete driver ID: {} as it is assigned to a bus", userDetails.getUserId(), id);
                throw new IllegalStateException("Cannot delete driver as it is assigned to a bus");
            }
        }
        driverService.deleteById(id);
        logger.info("User ID: {} deleted driver ID: {}", userDetails.getUserId(), id);
        return ResponseEntity.noContent().build();
    }

    private void validateDriverAccess(Integer driverId, Integer userId) {
        Integer busId = driverService.getBusIdForDriver(driverId);
        if (busId != null && !busService.isBusOwner(busId, userId)) {
            logger.warn("User ID: {} not authorized to access driver ID: {} assigned to bus ID: {}", userId, driverId, busId);
            throw new AccessDeniedException("Not authorized to access this driver");
        }
    }
}