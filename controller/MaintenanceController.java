package com.nexbus.nexbus_backend.controller;

import com.nexbus.nexbus_backend.dto.MaintenanceRecordDTO;
import com.nexbus.nexbus_backend.service.BusService;
import com.nexbus.nexbus_backend.service.MaintenanceService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/maintenance-records")
public class MaintenanceController {

    private final MaintenanceService maintenanceService;
    private final BusService busService;

    public MaintenanceController(MaintenanceService maintenanceRecordService, BusService busService) {
        this.maintenanceService = maintenanceRecordService;
        this.busService = busService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'BUSOPERATOR')")
    public ResponseEntity<List<MaintenanceRecordDTO>> getAllMaintenanceRecords() {
        return ResponseEntity.ok(maintenanceService.findAll());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'BUSOPERATOR')")
    public ResponseEntity<MaintenanceRecordDTO> getMaintenanceRecordById(
            @PathVariable @Min(value = 1, message = "ID must be positive") Integer id) {
        return ResponseEntity.ok(maintenanceService.findById(id));
    }

    @GetMapping("/bus/{busId}")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('BUSOPERATOR') and @busService.isBusOwner(#busId, authentication.principal.userId))")
    public ResponseEntity<List<MaintenanceRecordDTO>> getMaintenanceRecordsByBusId(
            @PathVariable @Min(value = 1, message = "Bus ID must be positive") Integer busId) {
        return ResponseEntity.ok(maintenanceService.findByBusId(busId));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or (hasRole('BUSOPERATOR') and @busService.isBusOwner(#recordDTO.busId, authentication.principal.userId))")
    public ResponseEntity<MaintenanceRecordDTO> createMaintenanceRecord(@Valid @RequestBody MaintenanceRecordDTO recordDTO) {
        return ResponseEntity.status(201).body(maintenanceService.save(recordDTO));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('BUSOPERATOR') and @busService.isBusOwner(#recordDTO.busId, authentication.principal.userId))")
    public ResponseEntity<MaintenanceRecordDTO> updateMaintenanceRecord(
            @PathVariable @Min(value = 1, message = "ID must be positive") Integer id,
            @Valid @RequestBody MaintenanceRecordDTO recordDTO) {
        return ResponseEntity.ok(maintenanceService.update(id, recordDTO));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'BUSOPERATOR')")
    public ResponseEntity<Void> deleteMaintenanceRecord(
            @PathVariable @Min(value = 1, message = "ID must be positive") Integer id) {
        maintenanceService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}