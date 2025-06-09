package com.nexbus.nexbus_backend.controller;

import com.nexbus.nexbus_backend.dto.BookingDTO;
import com.nexbus.nexbus_backend.exception.MailSendingException;
import com.nexbus.nexbus_backend.service.EmailService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/email")
public class EmailController {

    private final EmailService emailService;

    public EmailController(EmailService emailService) {
        this.emailService = emailService;
    }

    @PostMapping("/send-booking")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> sendBookingEmail(@Valid @RequestBody BookingDTO bookingDTO) {
        try {
            emailService.sendBookingConfirmationEmail(bookingDTO);
            return ResponseEntity.ok(createResponse("Booking confirmation email sent successfully."));
        } catch (MailSendingException e) {
            throw e;
        } catch (Exception e) {
            throw new MailSendingException("Unexpected error while sending booking email", e);
        }
    }

    @PostMapping("/send-cancellation")
    @PreAuthorize("hasRole('ADMIN')") // Optional
    public ResponseEntity<Map<String, String>> sendCancellationEmail(@Valid @RequestBody BookingDTO bookingDTO) {
        try {
            emailService.sendCancellationEmail(bookingDTO);
            return ResponseEntity.ok(createResponse("Booking cancellation email sent successfully."));
        } catch (MailSendingException e) {
            throw e;
        } catch (Exception e) {
            throw new MailSendingException("Unexpected error while sending cancellation email", e);
        }
    }

    @PostMapping("/send-refund")
    @PreAuthorize("hasRole('ADMIN')") // Optional
    public ResponseEntity<Map<String, String>> sendRefundEmail(@Valid @RequestBody BookingDTO bookingDTO) {
        try {
            emailService.sendRefundEmail(bookingDTO);
            return ResponseEntity.ok(createResponse("Refund email sent successfully."));
        } catch (MailSendingException e) {
            throw e;
        } catch (Exception e) {
            throw new MailSendingException("Unexpected error while sending refund email", e);
        }
    }

    private Map<String, String> createResponse(String message) {
        Map<String, String> response = new HashMap<>();
        response.put("message", message);
        return response;
    }
}