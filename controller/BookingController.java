package com.nexbus.nexbus_backend.controller;

import com.nexbus.nexbus_backend.dto.BookingDTO;
import com.nexbus.nexbus_backend.security.JwtUtil;
import com.nexbus.nexbus_backend.service.BookingService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/bookings")
public class BookingController {

    private final BookingService bookingService;
    private final JwtUtil jwtUtil;
    private static final Logger logger = LoggerFactory.getLogger(BookingController.class);

    public BookingController(BookingService bookingService, JwtUtil jwtUtil) {
        this.bookingService = bookingService;
        this.jwtUtil = jwtUtil;
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ADMIN', 'BUSOPERATOR')")
    public ResponseEntity<List<BookingDTO>> getAllBookings() {
        logger.info("Fetching all bookings");
        return ResponseEntity.ok(bookingService.findAll());
    }

    @GetMapping("/bus/{busId}")
    @PreAuthorize("hasAuthority('ADMIN') or (hasAuthority('BUSOPERATOR') and @busService.isBusOwner(#busId, authentication.principal.userId))")
    public ResponseEntity<List<BookingDTO>> getBookingsByBusId(
            @PathVariable @Min(value = 1, message = "Bus ID must be positive") Integer busId) {
        logger.info("Fetching bookings for bus ID: {}", busId);
        return ResponseEntity.ok(bookingService.findByBusId(busId));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN') or (hasAuthority('CUSTOMER') and @bookingService.isBookingOwner(#id, authentication.principal.userId)) or (hasAuthority('BUSOPERATOR') and @bookingService.isBookingOperator(#id, authentication.principal.userId))")
    public ResponseEntity<BookingDTO> getBookingById(
            @PathVariable @Min(value = 1, message = "ID must be positive") Integer id) {
        logger.info("Fetching booking with ID: {}", id);
        return ResponseEntity.ok(bookingService.findById(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('CUSTOMER')")
    public ResponseEntity<BookingDTO> createBooking(
            @Valid @RequestBody BookingDTO bookingDTO,
            @AuthenticationPrincipal UserDetails userDetails) {
        Integer userId = extractUserId(userDetails);
        logger.info("Creating booking for user ID: {}", userId);
        return ResponseEntity.status(201).body(bookingService.save(bookingDTO, userId));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN') or (hasAuthority('CUSTOMER') and @bookingService.isBookingOwner(#id, authentication.principal.userId)) or (hasAuthority('BUSOPERATOR') and @bookingService.isBookingOperator(#id, authentication.principal.userId))")
    public ResponseEntity<BookingDTO> updateBooking(
            @PathVariable @Min(value = 1, message = "ID must be positive") Integer id,
            @Valid @RequestBody BookingDTO bookingDTO) {
        logger.info("Updating booking with ID: {}", id);
        return ResponseEntity.ok(bookingService.update(id, bookingDTO));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN') or (hasAuthority('CUSTOMER') and @bookingService.isBookingOwner(#id, authentication.principal.userId)) or (hasAuthority('BUSOPERATOR') and @bookingService.isBookingOperator(#id, authentication.principal.userId))")
    public ResponseEntity<Void> deleteBooking(
            @PathVariable @Min(value = 1, message = "ID must be positive") Integer id) {
        logger.info("Deleting booking with ID: {}", id);
        bookingService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasAuthority('ADMIN') or (hasAuthority('CUSTOMER') and @bookingService.isBookingOwner(#id, authentication.principal.userId)) or (hasAuthority('BUSOPERATOR') and @bookingService.isBookingOperator(#id, authentication.principal.userId))")
    public ResponseEntity<BookingDTO> cancelBooking(
            @PathVariable @Min(value = 1, message = "ID must be positive") Integer id,
            @AuthenticationPrincipal UserDetails userDetails) {
        Integer userId = extractUserId(userDetails);
        logger.info("Cancelling booking with ID: {} by user ID: {}", id, userId);
        return ResponseEntity.ok(bookingService.cancelBooking(id, userId));
    }

    @PostMapping("/{id}/refund")
    @PreAuthorize("hasAuthority('ADMIN') or (hasAuthority('CUSTOMER') and @bookingService.isBookingOwner(#id, authentication.principal.userId)) or (hasAuthority('BUSOPERATOR') and @bookingService.isBookingOperator(#id, authentication.principal.userId))")
    public ResponseEntity<BigDecimal> processRefund(
            @PathVariable @Min(value = 1, message = "ID must be positive") Integer id,
            @AuthenticationPrincipal UserDetails userDetails) {
        Integer userId = extractUserId(userDetails);
        logger.info("Processing refund for booking ID: {} by user ID: {}", id, userId);
        return ResponseEntity.ok(bookingService.processRefund(id, userId));
    }

    @GetMapping("/{id}/refund-estimate")
    @PreAuthorize("hasAuthority('ADMIN') or (hasAuthority('CUSTOMER') and @bookingService.isBookingOwner(#id, authentication.principal.userId)) or (hasAuthority('BUSOPERATOR') and @bookingService.isBookingOperator(#id, authentication.principal.userId))")
    public ResponseEntity<BigDecimal> getRefundEstimate(
            @PathVariable @Min(value = 1, message = "ID must be positive") Integer id,
            @AuthenticationPrincipal UserDetails userDetails) {
        Integer userId = extractUserId(userDetails);
        logger.info("Fetching refund estimate for booking ID: {} by user ID: {}", id, userId);
        return ResponseEntity.ok(bookingService.estimateRefundAmount(id, userId));
    }

    @GetMapping("/refund-policy")
    public ResponseEntity<String> getRefundPolicy() {
        logger.info("Fetching refund policy");
        return ResponseEntity.ok(bookingService.getRefundPolicy());
    }

    private Integer extractUserId(UserDetails userDetails) {
        Object credentials = SecurityContextHolder.getContext().getAuthentication().getCredentials();
        if (!(credentials instanceof String token)) {
            logger.error("JWT token is not a valid string");
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Invalid token format");
        }
        try {
            Object userIdClaim = jwtUtil.extractClaim(token, claims -> claims.get("userId"));
            if (userIdClaim == null) {
                logger.error("No userId claim found in JWT token");
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Missing user ID in token");
            }
            if (userIdClaim instanceof Integer) {
                return (Integer) userIdClaim;
            } else if (userIdClaim instanceof String) {
                try {
                    return Integer.parseInt((String) userIdClaim);
                } catch (NumberFormatException e) {
                    logger.error("userId claim is not a valid integer: {}", userIdClaim);
                    throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Invalid user ID format");
                }
            } else {
                logger.error("userId claim is of unexpected type: {}", userIdClaim.getClass().getName());
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Invalid user ID type");
            }
        } catch (Exception e) {
            logger.error("Failed to extract userId from token: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Invalid token: " + e.getMessage());
        }
    }
}