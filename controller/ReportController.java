package com.nexbus.nexbus_backend.controller;

   import com.nexbus.nexbus_backend.dto.ReportDTO;
   import com.nexbus.nexbus_backend.service.ReportService;
   import org.slf4j.Logger;
   import org.slf4j.LoggerFactory;
   import org.springframework.http.ResponseEntity;
   import org.springframework.security.access.prepost.PreAuthorize;
   import org.springframework.web.bind.annotation.GetMapping;
   import org.springframework.web.bind.annotation.PathVariable;
   import org.springframework.web.bind.annotation.RequestMapping;
   import org.springframework.web.bind.annotation.RestController;

   @RestController
   @RequestMapping("/api/reports")
   public class ReportController {

       private static final Logger logger = LoggerFactory.getLogger(ReportController.class);

       private final ReportService reportService;

       public ReportController(ReportService reportService) {
           this.reportService = reportService;
       }

       @GetMapping("/{type}")
       @PreAuthorize("hasAuthority('ADMIN')")
       public ResponseEntity<ReportDTO> getReport(@PathVariable String type) {
           logger.debug("Fetching report for type: {}", type);
           ReportDTO report = reportService.generateReport(type);
           logger.info("Retrieved {} report with {} entries", type, report.getRows().size());
           return ResponseEntity.ok(report);
       }
   }