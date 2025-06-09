package com.nexbus.nexbus_backend.controller;

   import com.nexbus.nexbus_backend.dto.SupportDTO;
   import com.nexbus.nexbus_backend.service.SupportService;
   import org.slf4j.Logger;
   import org.slf4j.LoggerFactory;
   import org.springframework.http.ResponseEntity;
   import org.springframework.security.access.prepost.PreAuthorize;
   import org.springframework.web.bind.annotation.*;

   import java.util.List;

   @RestController
   @RequestMapping("/api/support")
   public class SupportController {

       private static final Logger logger = LoggerFactory.getLogger(SupportController.class);

       private final SupportService supportService;

       public SupportController(SupportService supportService) {
           this.supportService = supportService;
       }

       @GetMapping
       @PreAuthorize("hasAnyAuthority('ADMIN', 'CUSTOMER')")
       public ResponseEntity<List<SupportDTO>> getAllSupportTickets() {
           logger.debug("Fetching all support tickets");
           List<SupportDTO> tickets = supportService.findAll();
           logger.info("Retrieved {} support tickets", tickets.size());
           return ResponseEntity.ok(tickets);
       }

       @GetMapping("/{id}")
       @PreAuthorize("hasAnyAuthority('ADMIN', 'CUSTOMER')")
       public ResponseEntity<SupportDTO> getSupportTicketById(@PathVariable Integer id) {
           logger.debug("Fetching support ticket with ID: {}", id);
           SupportDTO ticket = supportService.findById(id);
           logger.info("Retrieved support ticket with ID: {}", id);
           return ResponseEntity.ok(ticket);
       }

       @PostMapping
       @PreAuthorize("hasAnyAuthority('ADMIN', 'CUSTOMER')")
       public ResponseEntity<SupportDTO> createSupportTicket(@RequestBody SupportDTO supportDTO) {
           logger.debug("Creating new support ticket");
           SupportDTO createdTicket = supportService.create(supportDTO);
           logger.info("Created support ticket with ID: {}", createdTicket.getSupportId());
           return ResponseEntity.ok(createdTicket);
       }

       @PutMapping("/{id}")
       @PreAuthorize("hasAuthority('ADMIN')")
       public ResponseEntity<SupportDTO> updateSupportTicket(@PathVariable Integer id, @RequestBody SupportDTO supportDTO) {
           logger.debug("Updating support ticket with ID: {}", id);
           SupportDTO updatedTicket = supportService.update(id, supportDTO);
           logger.info("Updated support ticket with ID: {}", id);
           return ResponseEntity.ok(updatedTicket);
       }

       @DeleteMapping("/{id}")
       @PreAuthorize("hasAuthority('ADMIN')")
       public ResponseEntity<Void> deleteSupportTicket(@PathVariable Integer id) {
           logger.debug("Deleting support ticket with ID: {}", id);
           supportService.delete(id);
           logger.info("Deleted support ticket with ID: {}", id);
           return ResponseEntity.noContent().build();
       }
   }