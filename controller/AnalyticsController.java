package com.nexbus.nexbus_backend.controller;

   import com.nexbus.nexbus_backend.dto.AnalyticsDTO;
   import com.nexbus.nexbus_backend.service.AnalyticsService;
   import org.slf4j.Logger;
   import org.slf4j.LoggerFactory;
   import org.springframework.http.ResponseEntity;
   import org.springframework.security.access.prepost.PreAuthorize;
   import org.springframework.web.bind.annotation.GetMapping;
   import org.springframework.web.bind.annotation.RequestMapping;
   import org.springframework.web.bind.annotation.RestController;

   @RestController
   @RequestMapping("/api/analytics")
   public class AnalyticsController {

       private static final Logger logger = LoggerFactory.getLogger(AnalyticsController.class);

       private final AnalyticsService analyticsService;

       public AnalyticsController(AnalyticsService analyticsService) {
           this.analyticsService = analyticsService;
       }

       @GetMapping("/bookings")
       @PreAuthorize("hasAuthority('ADMIN')")
       public ResponseEntity<AnalyticsDTO> getBookingAnalytics() {
           logger.debug("Fetching booking analytics");
           AnalyticsDTO analytics = analyticsService.getBookingAnalytics();
           logger.info("Retrieved booking analytics");
           return ResponseEntity.ok(analytics);
       }
   }