package com.nexbus.nexbus_backend.controller;

import com.nexbus.nexbus_backend.dto.ScheduleDTO;
import com.nexbus.nexbus_backend.service.BusService;
import com.nexbus.nexbus_backend.service.ScheduleService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/schedules")
public class ScheduleController {

    private final ScheduleService scheduleService;
    private final BusService busService;

    public ScheduleController(ScheduleService scheduleService, BusService busService) {
        this.scheduleService = scheduleService;
        this.busService = busService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'BUSOPERATOR')")
    public ResponseEntity<List<ScheduleDTO>> getAllSchedules() {
        return ResponseEntity.ok(scheduleService.findAll());
    }

    @GetMapping("/bus/{busId}")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('BUSOPERATOR') and @busService.isBusOwner(#busId, authentication.principal.userId))")
    public ResponseEntity<List<ScheduleDTO>> getSchedulesByBusId(
            @PathVariable @Min(value = 1, message = "Bus ID must be positive") Integer busId) {
        return ResponseEntity.ok(scheduleService.findByBusId(busId));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'BUSOPERATOR')")
    public ResponseEntity<ScheduleDTO> getScheduleById(
            @PathVariable @Min(value = 1, message = "ID must be positive") Integer id) {
        return ResponseEntity.ok(scheduleService.findById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or (hasRole('BUSOPERATOR') and @busService.isBusOwner(#scheduleDTO.busId, authentication.principal.userId))")
    public ResponseEntity<ScheduleDTO> createSchedule(@Valid @RequestBody ScheduleDTO scheduleDTO) {
        return ResponseEntity.status(201).body(scheduleService.save(scheduleDTO));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('BUSOPERATOR') and @busService.isBusOwner(#scheduleDTO.busId, authentication.principal.userId))")
    public ResponseEntity<ScheduleDTO> updateSchedule(
            @PathVariable @Min(value = 1, message = "ID must be positive") Integer id,
            @Valid @RequestBody ScheduleDTO scheduleDTO) {
        return ResponseEntity.ok(scheduleService.update(id, scheduleDTO));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'BUSOPERATOR')")
    public ResponseEntity<Void> deleteSchedule(
            @PathVariable @Min(value = 1, message = "ID must be positive") Integer id) {
        scheduleService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}