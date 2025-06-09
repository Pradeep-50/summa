package com.nexbus.frontend.controller;

import com.nexbus.frontend.dto.MaintenanceRecordDTO;
import com.nexbus.frontend.dto.BusDTO;
import com.nexbus.frontend.exception.ApiException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import java.util.List;

@Service
public class MaintenanceService {

    @Autowired
    private RestTemplate restTemplate;

    private static final String BACKEND_URL = "http://localhost:9090/api/maintenance";

    public List<MaintenanceRecordDTO> getAllMaintenanceRecords() {
        ResponseEntity<MaintenanceRecordDTO[]> response = restTemplate.exchange(
            BACKEND_URL + "/all",
            HttpMethod.GET,
            null,
            MaintenanceRecordDTO[].class
        );

        if (response.getStatusCode().is2xxSuccessful()) {
            return List.of(response.getBody());
        } else {
            throw new ApiException("Failed to fetch maintenance records: " + response.getStatusCode());
        }
    }

    public void addMaintenanceRecord(MaintenanceRecordDTO maintenanceRecord) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        HttpEntity<MaintenanceRecordDTO> requestEntity = new HttpEntity<>(maintenanceRecord, headers);

        ResponseEntity<Void> response = restTemplate.exchange(
            BACKEND_URL + "/add",
            HttpMethod.POST,
            requestEntity,
            Void.class
        );

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new ApiException("Failed to add maintenance record: " + response.getStatusCode());
        }
    }

    public MaintenanceRecordDTO getMaintenanceRecordById(Integer id) {
        ResponseEntity<MaintenanceRecordDTO> response = restTemplate.exchange(
            BACKEND_URL + "/{id}",
            HttpMethod.GET,
            null,
            MaintenanceRecordDTO.class,
            id
        );

        if (response.getStatusCode().is2xxSuccessful()) {
            return response.getBody();
        } else {
            throw new ApiException("Maintenance record not found: " + response.getStatusCode());
        }
    }

    public void updateMaintenanceRecord(MaintenanceRecordDTO maintenanceRecord) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        HttpEntity<MaintenanceRecordDTO> requestEntity = new HttpEntity<>(maintenanceRecord, headers);

        ResponseEntity<Void> response = restTemplate.exchange(
            BACKEND_URL + "/update",
            HttpMethod.PUT,
            requestEntity,
            Void.class
        );

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new ApiException("Failed to update maintenance record: " + response.getStatusCode());
        }
    }

    public void deleteMaintenanceRecord(Integer id) {
        ResponseEntity<Void> response = restTemplate.exchange(
            BACKEND_URL + "/delete/{id}",
            HttpMethod.DELETE,
            null,
            Void.class,
            id
        );

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new ApiException("Failed to delete maintenance record: " + response.getStatusCode());
        }
    }

    public List<BusDTO> getAllBuses() {
        ResponseEntity<BusDTO[]> response = restTemplate.exchange(
            "http://localhost:9090/api/buses/all",
            HttpMethod.GET,
            null,
            BusDTO[].class
        );

        if (response.getStatusCode().is2xxSuccessful()) {
            return List.of(response.getBody());
        } else {
            throw new ApiException("Failed to fetch buses: " + response.getStatusCode());
        }
    }
}
