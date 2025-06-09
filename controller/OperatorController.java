package com.nexbus.nexbus_backend.controller;

import com.nexbus.nexbus_backend.dto.BusDTO;
import com.nexbus.nexbus_backend.dto.OperatorDTO;
import com.nexbus.nexbus_backend.service.OperatorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/bus-operators")
public class OperatorController {

    @Autowired
    private OperatorService operatorService;

    @PostMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<OperatorDTO> create(@RequestBody OperatorDTO dto) {
        OperatorDTO saved = operatorService.save(dto);
        return ResponseEntity.ok(saved);
    }

    @GetMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<List<OperatorDTO>> getAll() {
        return ResponseEntity.ok(operatorService.findAll());
    }

    @GetMapping("/{operatorId}/buses")
    @PreAuthorize("hasAnyAuthority('BUSOPERATOR', 'ADMIN')")
    public ResponseEntity<List<BusDTO>> getBusesByOperatorId(
            @PathVariable Integer operatorId,
            Authentication authentication) {
        String email = authentication.getName();
        boolean isAdmin = authentication.getAuthorities().stream()
            .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
        Integer userId = operatorService.findById(operatorId).getUserId();
        List<BusDTO> buses = operatorService.findBusesByOperatorId(operatorId, userId, isAdmin);
        return ResponseEntity.ok(buses);
    }
}