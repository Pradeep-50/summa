package com.nexbus.nexbus_backend.controller;

import com.nexbus.nexbus_backend.dto.PaymentDTO;
import com.nexbus.nexbus_backend.service.PaymentService;
import com.razorpay.Order;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/razorpay/create")
    @PreAuthorize("hasAnyRole('ADMIN', 'BUS_OPERATOR')")
    public ResponseEntity<Order> createRazorpayOrder(@Valid @RequestBody PaymentDTO paymentDTO) throws Exception {
        Order order = paymentService.createRazorpayOrder(paymentDTO);
        return ResponseEntity.ok(order);
    }

    @PostMapping("/razorpay/confirm")
    @PreAuthorize("hasAnyRole('ADMIN', 'BUS_OPERATOR')")
    public ResponseEntity<PaymentDTO> confirmPayment(@RequestParam String paymentId,
                                                     @RequestParam String orderId,
                                                     @Valid @RequestBody PaymentDTO paymentDTO) {
        PaymentDTO confirmedPayment = paymentService.processPayment(paymentId, orderId, paymentDTO);
        return ResponseEntity.ok(confirmedPayment);
    }

}
