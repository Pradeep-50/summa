package com.nexbus.nexbus_backend.controller;

import com.nexbus.nexbus_backend.dto.AmenitiesDTO;
import com.nexbus.nexbus_backend.service.AmenityService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/amenities")
public class AmenityController {

    private final AmenityService amenityService;

    public AmenityController(AmenityService amenityService) {
        this.amenityService = amenityService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'BUSOPERATOR')")
    public ResponseEntity<List<AmenitiesDTO>> getAllAmenities() {
        return ResponseEntity.ok(amenityService.findAll());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'BUSOPERATOR')")
    public ResponseEntity<AmenitiesDTO> getAmenityById(
            @PathVariable @Min(value = 1, message = "ID must be positive") Integer id) {
        return ResponseEntity.ok(amenityService.findById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AmenitiesDTO> createAmenity(@Valid @RequestBody AmenitiesDTO amenitiesDTO) {
        return ResponseEntity.status(201).body(amenityService.save(amenitiesDTO));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AmenitiesDTO> updateAmenity(
            @PathVariable @Min(value = 1, message = "ID must be positive") Integer id,
            @Valid @RequestBody AmenitiesDTO amenitiesDTO) {
        return ResponseEntity.ok(amenityService.update(id, amenitiesDTO));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteAmenity(
            @PathVariable @Min(value = 1, message = "ID must be positive") Integer id) {
        amenityService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}