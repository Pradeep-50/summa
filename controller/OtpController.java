package com.nexbus.nexbus_backend.controller;

import com.nexbus.nexbus_backend.dto.UserDTO;
import com.nexbus.nexbus_backend.service.SmsService;
import com.nexbus.nexbus_backend.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/otp")
public class OtpController {

    private static final Logger logger = LoggerFactory.getLogger(OtpController.class);

    private final SmsService smsService;
    private final UserService userService;

    @Value("${twilio.phone.number}")
    private String twilioPhoneNumber;

    public OtpController(SmsService smsService, UserService userService) {
        this.smsService = smsService;
        this.userService = userService;
    }

    @PostMapping("/send")
    public ResponseEntity<Map<String, String>> sendOtp(@RequestBody SendOtpRequest request) {
        try {
            // Validate request
            if (request.getUserId() == null) {
                return ResponseEntity.badRequest().body(createErrorResponse("User ID is required"));
            }

            // Fetch user
            UserDTO user = userService.findById(request.getUserId());
            if (user.getPhoneNumber() == null || user.getPhoneNumber().isBlank()) {
                return ResponseEntity.badRequest().body(createErrorResponse("User phone number is not set"));
            }

            // Validate phone number format (basic E.164 check)
            if (!user.getPhoneNumber().matches("^\\+\\d{10,15}$")) {
                return ResponseEntity.badRequest().body(createErrorResponse("Invalid phone number format. Use E.164 (e.g., +12345678901)"));
            }

            // Prevent sending OTP to Twilio's own number
            if (user.getPhoneNumber().equals(twilioPhoneNumber)) {
                return ResponseEntity.badRequest().body(createErrorResponse("Cannot send OTP to the Twilio sender number"));
            }

            // Send OTP via SMS
            smsService.sendOtpSms(user.getPhoneNumber(), user.getFirstName() + " " + user.getLastName());

            Map<String, String> response = new HashMap<>();
            response.put("message", "OTP sent successfully to " + user.getPhoneNumber());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Failed to send OTP for user ID: {}", request.getUserId(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Failed to send OTP: " + e.getMessage()));
        }
    }

    @PostMapping("/verify")
    public ResponseEntity<Map<String, String>> verifyOtp(@RequestBody VerifyOtpRequest request) {
        try {
            // Validate request
            if (request.getUserId() == null) {
                return ResponseEntity.badRequest().body(createErrorResponse("User ID is required"));
            }
            if (request.getOtp() == null || request.getOtp().isBlank()) {
                return ResponseEntity.badRequest().body(createErrorResponse("OTP is required"));
            }

            // Fetch user
            UserDTO user = userService.findById(request.getUserId());
            if (user.getPhoneNumber() == null || user.getPhoneNumber().isBlank()) {
                return ResponseEntity.badRequest().body(createErrorResponse("User phone number is not set"));
            }

            // Verify OTP
            boolean isValid = smsService.validateOtp(user.getPhoneNumber(), request.getOtp());
            if (isValid) {
                Map<String, String> response = new HashMap<>();
                response.put("message", "OTP verified successfully");
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.badRequest().body(createErrorResponse("Invalid or expired OTP"));
            }
        } catch (Exception e) {
            logger.error("Failed to verify OTP for user ID: {}", request.getUserId(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Failed to verify OTP: " + e.getMessage()));
        }
    }

    private Map<String, String> createErrorResponse(String message) {
        Map<String, String> response = new HashMap<>();
        response.put("error", message);
        return response;
    }

    // Request DTOs
    public static class SendOtpRequest {
        @NotNull
        private Integer userId;

        public Integer getUserId() {
            return userId;
        }

        public void setUserId(Integer userId) {
            this.userId = userId;
        }
    }

    public static class VerifyOtpRequest {
        @NotNull
        private Integer userId;
        @NotBlank
        private String otp;

        public Integer getUserId() {
            return userId;
        }

        public void setUserId(Integer userId) {
            this.userId = userId;
        }

        public String getOtp() {
            return otp;
        }

        public void setOtp(String otp) {
            this.otp = otp;
        }
    }
}