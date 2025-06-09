package com.nexbus.nexbus_backend.controller;

import com.nexbus.nexbus_backend.dto.SeatAvailabilitySummaryDTO;
import com.nexbus.nexbus_backend.dto.SeatDTO;
import com.nexbus.nexbus_backend.security.CustomUserDetails;
import com.nexbus.nexbus_backend.service.BusService;
import com.nexbus.nexbus_backend.service.SeatService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/seats")
@Validated
public class SeatController {

    private static final Logger logger = LoggerFactory.getLogger(SeatController.class);

    private final SeatService seatService;
    private final BusService busService;

    public SeatController(SeatService seatService, BusService busService) {
        this.seatService = seatService;
        this.busService = busService;
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ADMIN', 'BUSOPERATOR', 'CUSTOMER')")
    public ResponseEntity<List<SeatDTO>> getAllSeats(@AuthenticationPrincipal CustomUserDetails userDetails) {
        logger.debug("User ID: {} (Role: {}) fetching all seats", userDetails.getUserId(), userDetails.getAuthorities());
        List<SeatDTO> seats = seatService.findAll();
        logger.info("User ID: {} retrieved {} seats", userDetails.getUserId(), seats.size());
        return ResponseEntity.ok(seats);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'BUSOPERATOR', 'CUSTOMER')")
    public ResponseEntity<SeatDTO> getSeatById(
            @PathVariable @Positive(message = "ID must be positive") Integer id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        logger.debug("User ID: {} (Role: {}) fetching seat ID: {}", userDetails.getUserId(), userDetails.getAuthorities(), id);
        SeatDTO seat = seatService.findById(id);
        if (userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("BUSOPERATOR"))) {
            validateSeatAccess(seat.getBusId(), userDetails.getUserId());
        }
        logger.info("User ID: {} retrieved seat ID: {}", userDetails.getUserId(), id);
        return ResponseEntity.ok(seat);
    }

    @GetMapping("/bus/{busId}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'BUSOPERATOR', 'CUSTOMER')")
    public ResponseEntity<List<SeatDTO>> getSeatsByBusId(
            @PathVariable @Positive(message = "Bus ID must be positive") Integer busId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        logger.debug("User ID: {} (Role: {}) fetching seats for bus ID: {}", userDetails.getUserId(), userDetails.getAuthorities(), busId);
        if (userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("BUSOPERATOR"))) {
            validateSeatAccess(busId, userDetails.getUserId());
        }
        List<SeatDTO> seats = seatService.findByBusId(busId);
        logger.info("User ID: {} retrieved {} seats for bus ID: {}", userDetails.getUserId(), seats.size(), busId);
        return ResponseEntity.ok(seats);
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('ADMIN', 'BUSOPERATOR')")
    public ResponseEntity<SeatDTO> createSeat(
            @Valid @RequestBody SeatDTO seatDTO,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        logger.debug("User ID: {} (Role: {}) creating seat for bus ID: {}", userDetails.getUserId(), userDetails.getAuthorities(), seatDTO.getBusId());
        if (userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("BUSOPERATOR"))) {
            validateSeatAccess(seatDTO.getBusId(), userDetails.getUserId());
        }
        SeatDTO createdSeat = seatService.save(seatDTO);
        logger.info("User ID: {} created seat ID: {} for bus ID: {}", userDetails.getUserId(), createdSeat.getSeatId(), seatDTO.getBusId());
        return ResponseEntity.status(HttpStatus.CREATED).body(createdSeat);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'BUSOPERATOR')")
    public ResponseEntity<SeatDTO> updateSeat(
            @PathVariable @Positive(message = "ID must be positive") Integer id,
            @Valid @RequestBody SeatDTO seatDTO,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        logger.debug("User ID: {} (Role: {}) updating seat ID: {}", userDetails.getUserId(), userDetails.getAuthorities(), id);
        if (userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("BUSOPERATOR"))) {
            validateSeatAccess(seatDTO.getBusId(), userDetails.getUserId());
        }
        SeatDTO updatedSeat = seatService.update(id, seatDTO);
        logger.info("User ID: {} updated seat ID: {}", userDetails.getUserId(), id);
        return ResponseEntity.ok(updatedSeat);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'BUSOPERATOR')")
    public ResponseEntity<Void> deleteSeat(
            @PathVariable @Positive(message = "ID must be positive") Integer id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        logger.debug("User ID: {} (Role: {}) deleting seat ID: {}", userDetails.getUserId(), userDetails.getAuthorities(), id);
        SeatDTO seat = seatService.findById(id);
        if (userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("BUSOPERATOR"))) {
            validateSeatAccess(seat.getBusId(), userDetails.getUserId());
        }
        if (!seat.getIsAvailable()) {
            logger.warn("User ID: {} cannot delete seat ID: {} as it is booked", userDetails.getUserId(), id);
            throw new IllegalStateException("Cannot delete booked seat");
        }
        seatService.deleteById(id);
        logger.info("User ID: {} deleted seat ID: {}", userDetails.getUserId(), id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/book")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'CUSTOMER')")
    public ResponseEntity<SeatDTO> bookSeat(
            @PathVariable @Positive(message = "ID must be positive") Integer id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        logger.debug("User ID: {} (Role: {}) booking seat ID: {}", userDetails.getUserId(), userDetails.getAuthorities(), id);
        seatService.markAsBooked(id, userDetails.getUserId());
        SeatDTO bookedSeat = seatService.findById(id);
        logger.info("User ID: {} booked seat ID: {}", userDetails.getUserId(), id);
        return ResponseEntity.ok(bookedSeat);
    }

    @GetMapping("/bus/{busId}/availability")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'BUSOPERATOR', 'CUSTOMER')")
    public ResponseEntity<SeatAvailabilitySummaryDTO> getSeatAvailability(
            @PathVariable @Positive(message = "Bus ID must be positive") Integer busId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        logger.debug("User ID: {} (Role: {}) fetching seat availability for bus ID: {}", userDetails.getUserId(), userDetails.getAuthorities(), busId);
        if (userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("BUSOPERATOR"))) {
            validateSeatAccess(busId, userDetails.getUserId());
        }
        SeatAvailabilitySummaryDTO summary = seatService.getSeatAvailabilitySummary(busId);
        logger.info("User ID: {} retrieved seat availability for bus ID: {}", userDetails.getUserId(), busId);
        return ResponseEntity.ok(summary);
    }

    private void validateSeatAccess(Integer busId, Integer userId) {
        if (!busService.isBusOwner(busId, userId)) {
            logger.warn("User ID: {} not authorized to access seats for bus ID: {}", userId, busId);
            throw new AccessDeniedException("Not authorized to access seats for this bus");
        }
    }
}