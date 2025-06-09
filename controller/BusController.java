package com.nexbus.nexbus_backend.controller;

import com.nexbus.nexbus_backend.dto.BusDTO;
import com.nexbus.nexbus_backend.dto.MaintenanceRecordDTO;
import com.nexbus.nexbus_backend.dto.ScheduleDTO;
import com.nexbus.nexbus_backend.dto.SeatDTO;
import com.nexbus.nexbus_backend.security.CustomUserDetails;
import com.nexbus.nexbus_backend.service.BusService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/buses")
public class BusController {

    private final BusService busService;

    public BusController(BusService busService) {
        this.busService = busService;
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ADMIN', 'BUSOPERATOR')")
    public ResponseEntity<List<BusDTO>> getAllBuses(@AuthenticationPrincipal CustomUserDetails userDetails) {
        List<BusDTO> buses;
        if (userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("BUSOPERATOR"))) {
            buses = busService.findByOperatorId(userDetails.getUserId());
        } else {
            buses = busService.findAll();
        }
        return ResponseEntity.ok(buses);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'BUSOPERATOR')")
    public ResponseEntity<BusDTO> getBusById(@PathVariable Integer id, @AuthenticationPrincipal CustomUserDetails userDetails) {
        BusDTO bus = busService.findById(id);
        if (userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("BUSOPERATOR")) &&
            !busService.isBusOwner(id, userDetails.getUserId())) {
            throw new AccessDeniedException("Not authorized to view this bus");
        }
        return ResponseEntity.ok(bus);
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('ADMIN', 'BUSOPERATOR')")
    public ResponseEntity<BusDTO> createBus(@Valid @RequestBody BusDTO busDTO, @AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("BUSOPERATOR")) &&
            !busDTO.getOperatorId().equals(userDetails.getUserId())) {
            throw new AccessDeniedException("Cannot create bus for another operator");
        }
        BusDTO savedBus = busService.save(busDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedBus);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'BUSOPERATOR')")
    public ResponseEntity<BusDTO> updateBus(@PathVariable Integer id, @Valid @RequestBody BusDTO busDTO, @AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("BUSOPERATOR")) &&
            !busService.isBusOwner(id, userDetails.getUserId())) {
            throw new AccessDeniedException("Not authorized to update this bus");
        }
        BusDTO updatedBus = busService.update(id, busDTO);
        return ResponseEntity.ok(updatedBus);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'BUSOPERATOR')")
    public ResponseEntity<Void> deleteBus(@PathVariable Integer id, @AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("BUSOPERATOR")) &&
            !busService.isBusOwner(id, userDetails.getUserId())) {
            throw new AccessDeniedException("Not authorized to delete this bus");
        }
        busService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/seats")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'BUSOPERATOR', 'CUSTOMER')")
    public ResponseEntity<List<SeatDTO>> getSeatsByBusId(@PathVariable Integer id, @AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("BUSOPERATOR")) &&
            !busService.isBusOwner(id, userDetails.getUserId())) {
            throw new AccessDeniedException("Not authorized to view seats for this bus");
        }
        List<SeatDTO> seats = busService.findSeatsByBusId(id);
        return ResponseEntity.ok(seats);
    }

    @GetMapping("/{id}/schedules")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'BUSOPERATOR', 'CUSTOMER')")
    public ResponseEntity<List<ScheduleDTO>> getSchedulesByBusId(@PathVariable Integer id, @AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("BUSOPERATOR")) &&
            !busService.isBusOwner(id, userDetails.getUserId())) {
            throw new AccessDeniedException("Not authorized to view schedules for this bus");
        }
        List<ScheduleDTO> schedules = busService.findSchedulesByBusId(id);
        return ResponseEntity.ok(schedules);
    }

    @PostMapping("/{busId}/schedules")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'BUSOPERATOR')")
    public ResponseEntity<ScheduleDTO> addSchedule(@PathVariable Integer busId, @Valid @RequestBody ScheduleDTO scheduleDTO, @AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("BUSOPERATOR")) &&
            !busService.isBusOwner(busId, userDetails.getUserId())) {
            throw new AccessDeniedException("Not authorized to add schedules for this bus");
        }
        ScheduleDTO savedSchedule = busService.addSchedule(busId, scheduleDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedSchedule);
    }

    @PutMapping("/{busId}/schedules/{scheduleId}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'BUSOPERATOR')")
    public ResponseEntity<ScheduleDTO> updateSchedule(@PathVariable Integer busId, @PathVariable Integer scheduleId, @Valid @RequestBody ScheduleDTO scheduleDTO, @AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("BUSOPERATOR")) &&
            !busService.isBusOwner(busId, userDetails.getUserId())) {
            throw new AccessDeniedException("Not authorized to update schedules for this bus");
        }
        ScheduleDTO updatedSchedule = busService.updateSchedule(busId, scheduleId, scheduleDTO);
        return ResponseEntity.ok(updatedSchedule);
    }

    @DeleteMapping("/{busId}/schedules/{scheduleId}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'BUSOPERATOR')")
    public ResponseEntity<Void> deleteSchedule(@PathVariable Integer busId, @PathVariable Integer scheduleId, @AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("BUSOPERATOR")) &&
            !busService.isBusOwner(busId, userDetails.getUserId())) {
            throw new AccessDeniedException("Not authorized to delete schedules for this bus");
        }
        busService.deleteSchedule(scheduleId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{busId}/amenities/{amenityId}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'BUSOPERATOR')")
    public ResponseEntity<BusDTO> addAmenityToBus(@PathVariable Integer busId, @PathVariable Integer amenityId, @AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("BUSOPERATOR")) &&
            !busService.isBusOwner(busId, userDetails.getUserId())) {
            throw new AccessDeniedException("Not authorized to add amenities to this bus");
        }
        BusDTO updatedBus = busService.addAmenityToBus(busId, amenityId);
        return ResponseEntity.ok(updatedBus);
    }

    @DeleteMapping("/{busId}/amenities/{amenityId}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'BUSOPERATOR')")
    public ResponseEntity<BusDTO> removeAmenityFromBus(@PathVariable Integer busId, @PathVariable Integer amenityId, @AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("BUSOPERATOR")) &&
            !busService.isBusOwner(busId, userDetails.getUserId())) {
            throw new AccessDeniedException("Not authorized to remove amenities from this bus");
        }
        BusDTO updatedBus = busService.removeAmenityFromBus(busId, amenityId);
        return ResponseEntity.ok(updatedBus);
    }

    @PostMapping("/{busId}/drivers/{driverId}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'BUSOPERATOR')")
    public ResponseEntity<BusDTO> assignDriverToBus(@PathVariable Integer busId, @PathVariable Integer driverId, @AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("BUSOPERATOR")) &&
            !busService.isBusOwner(busId, userDetails.getUserId())) {
            throw new AccessDeniedException("Not authorized to assign drivers to this bus");
        }
        BusDTO updatedBus = busService.assignDriverToBus(busId, driverId);
        return ResponseEntity.ok(updatedBus);
    }

    @PostMapping("/{busId}/routes/{routeId}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'BUSOPERATOR')")
    public ResponseEntity<BusDTO> assignRouteToBus(@PathVariable Integer busId, @PathVariable Integer routeId, @AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("BUSOPERATOR")) &&
            !busService.isBusOwner(busId, userDetails.getUserId())) {
            throw new AccessDeniedException("Not authorized to assign routes to this bus");
        }
        BusDTO updatedBus = busService.assignRouteToBus(busId, routeId);
        return ResponseEntity.ok(updatedBus);
    }

    @PostMapping("/{busId}/maintenance-records")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'BUSOPERATOR')")
    public ResponseEntity<MaintenanceRecordDTO> addMaintenanceRecord(@PathVariable Integer busId, @Valid @RequestBody MaintenanceRecordDTO recordDTO, @AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("BUSOPERATOR")) &&
            !busService.isBusOwner(busId, userDetails.getUserId())) {
            throw new AccessDeniedException("Not authorized to add maintenance records to this bus");
        }
        MaintenanceRecordDTO savedRecord = busService.addMaintenanceRecord(busId, recordDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedRecord);
    }
}