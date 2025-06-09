package com.nexbus.nexbus_backend.controller;

import com.nexbus.nexbus_backend.dto.BookingDTO;
import com.nexbus.nexbus_backend.dto.BusDTO;
import com.nexbus.nexbus_backend.dto.CustomerDTO;
import com.nexbus.nexbus_backend.dto.LoginRequest;
import com.nexbus.nexbus_backend.dto.LoginResponse;
import com.nexbus.nexbus_backend.dto.SeatDTO;
import com.nexbus.nexbus_backend.service.CustomerService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    private static final Logger logger = LoggerFactory.getLogger(CustomerController.class);

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @GetMapping
    public ResponseEntity<List<CustomerDTO>> findAll() {
        logger.info("Fetching all customers");
        return ResponseEntity.ok(customerService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CustomerDTO> findById(@PathVariable Long id) {
        logger.info("Fetching customer with ID: {}", id);
        return ResponseEntity.ok(customerService.findById(id));
    }

    @PostMapping("/register")
    public ResponseEntity<CustomerDTO> register(@Valid @RequestBody CustomerDTO dto) {
        logger.info("Registering new customer with email: {}", dto.getEmail());
        return ResponseEntity.status(HttpStatus.CREATED).body(customerService.register(dto));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        logger.info("Login attempt for email: {}", loginRequest.getEmail());
        return ResponseEntity.ok(customerService.login(loginRequest.getEmail(), loginRequest.getPassword()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CustomerDTO> update(@PathVariable Long id, @Valid @RequestBody CustomerDTO dto) {
        logger.info("Updating customer with ID: {}", id);
        return ResponseEntity.ok(customerService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        logger.info("Deleting customer with ID: {}", id);
        customerService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/buses")
    public ResponseEntity<List<BusDTO>> findAvailableBuses(
        @RequestParam String source,
        @RequestParam String destination,
        @RequestParam String date) {
        logger.info("Fetching available buses from {} to {} on {}", source, destination, date);
        return ResponseEntity.ok(customerService.findAvailableBuses(source, destination, date));
    }

    @GetMapping("/buses/{busId}/seats")
    public ResponseEntity<List<SeatDTO>> findAllSeats(@PathVariable Integer busId) {
        logger.info("Fetching all seats for bus ID: {}", busId);
        return ResponseEntity.ok(customerService.findAllSeats(busId));
    }

    @GetMapping("/{customerId}/bookings")
    public ResponseEntity<List<BookingDTO>> findCustomerBookings(@PathVariable Long customerId) {
        logger.info("Fetching bookings for customer ID: {}", customerId);
        return ResponseEntity.ok(customerService.findCustomerBookings(customerId));
    }

    @PostMapping("/{customerId}/book")
    public ResponseEntity<BookingDTO> bookTicket(
        @PathVariable Long customerId,
        @RequestBody @Valid BookingRequest bookingRequest) {
        logger.info("Booking ticket for customer ID: {} on bus ID: {}", customerId, bookingRequest.getBusId());
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(customerService.bookTicket(customerId, bookingRequest.getBusId(), bookingRequest.getSeatNumber()));
    }

    @SuppressWarnings("unused")
    private static class BookingRequest {
        private Integer busId;
        private String seatNumber;

        public Integer getBusId() { return busId; }
        public void setBusId(Integer busId) { this.busId = busId; }
        public String getSeatNumber() { return seatNumber; }
        public void setSeatNumber(String seatNumber) { this.seatNumber = seatNumber; }
    }
}