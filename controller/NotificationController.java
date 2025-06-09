package com.nexbus.nexbus_backend.controller;

import com.nexbus.nexbus_backend.dto.NotificationDTO;
import com.nexbus.nexbus_backend.service.NotificationService;
import jakarta.validation.constraints.Min;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/notifications")
@Validated 
public class NotificationController {

    private static final Logger logger = LoggerFactory.getLogger(NotificationController.class);

    @Autowired
    private NotificationService notificationService;

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ADMIN', 'CUSTOMER')")
    public ResponseEntity<Page<NotificationDTO>> getAllNotifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        logger.debug("Fetching all notifications for user: {}", 
            SecurityContextHolder.getContext().getAuthentication().getName());
        Pageable pageable = PageRequest.of(page, size);
        Page<NotificationDTO> notifications = notificationService.findAll(pageable);
        logger.info("Retrieved {} notifications", notifications.getTotalElements());
        return ResponseEntity.ok(notifications);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN') or (hasAuthority('CUSTOMER') and @notificationService.isNotificationOwner(#id, authentication.principal.userId))")
    public ResponseEntity<NotificationDTO> getNotificationById(
            @PathVariable @Min(value = 1, message = "ID must be positive") Integer id) {
        logger.debug("Fetching notification with ID: {}", id);
        NotificationDTO notification = notificationService.findById(id);
        if (notification == null) {
            logger.warn("Notification with ID: {} not found", id);
            return ResponseEntity.notFound().build();
        }
        logger.info("Found notification with ID: {}", id);
        return ResponseEntity.ok(notification);
    }

    @PostMapping("/{id}/mark-read")
    @PreAuthorize("hasAuthority('ADMIN') or (hasAuthority('CUSTOMER') and @notificationService.isNotificationOwner(#id, authentication.principal.userId))")
    public ResponseEntity<NotificationDTO> markNotificationAsRead(
            @PathVariable @Min(value = 1, message = "ID must be positive") Integer id) {
        logger.debug("Marking notification as read with ID: {}", id);
        NotificationDTO notification = notificationService.markAsRead(id);
        if (notification == null) {
            logger.warn("Notification with ID: {} not found or already read", id);
            return ResponseEntity.notFound().build();
        }
        logger.info("Marked notification as read with ID: {}", id);
        return ResponseEntity.ok(notification);
    }
}