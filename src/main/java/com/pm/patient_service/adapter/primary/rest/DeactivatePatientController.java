package com.pm.patient_service.adapter.primary.rest;

import com.pm.patient_service.application.port.in.DeactivatePatientUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * Primary adapter: REST controller for deactivating patients
 * 
 * Follows hexagonal architecture:
 * - Depends on inbound port: DeactivatePatientUseCase
 * - Handles HTTP concerns only (routing, status codes, response format)
 * - No business logic, delegates to use case
 * - Converts HTTP requests to use case inputs
 */
@RestController
@RequestMapping("/patients")
@Tag(name = "Patient", description = "API for managing patients")
public class DeactivatePatientController {
    private final DeactivatePatientUseCase deactivatePatientUseCase;

    public DeactivatePatientController(DeactivatePatientUseCase deactivatePatientUseCase) {
        this.deactivatePatientUseCase = deactivatePatientUseCase;
    }

    @PutMapping("/{id}/deactivate")
    @Operation(summary = "Deactivate a Patient - patient remains in DB but cannot be updated")
    public ResponseEntity<Void> deactivatePatient(@PathVariable UUID id) {
        deactivatePatientUseCase.deactivate(id);
        return ResponseEntity.noContent().build();
    }
}
