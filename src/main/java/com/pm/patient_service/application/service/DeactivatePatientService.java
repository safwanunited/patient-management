package com.pm.patient_service.application.service;

import com.pm.patient_service.application.port.in.DeactivatePatientUseCase;
import com.pm.patient_service.application.port.out.PatientPersistencePort;
import com.pm.patient_service.domain.patient.exception.PatientInactiveException;
import com.pm.patient_service.domain.patient.model.Patient;
import com.pm.patient_service.exception.PatientNotFoundException;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Application service implementing the Deactivate Patient use case
 * Implements inbound port: DeactivatePatientUseCase
 * Uses outbound port: PatientPersistencePort
 * 
 * Follows hexagonal architecture:
 * - Depends on interfaces (ports), not concrete implementations
 * - Orchestrates domain logic and persistence
 */
public class DeactivatePatientService implements DeactivatePatientUseCase {
    private final PatientPersistencePort patientPersistencePort;

    public DeactivatePatientService(PatientPersistencePort patientPersistencePort) {
        this.patientPersistencePort = patientPersistencePort;
    }

    @Override
    public void deactivate(UUID patientId) {
        // Fetch patient from persistence layer
        Patient patient = patientPersistencePort.findById(patientId)
                .orElseThrow(() -> new PatientNotFoundException("Patient not found with ID: " + patientId));
        
        // Execute domain logic (deactivation business rule)
        try {
            patient.deactivate(LocalDate.now());
        } catch (IllegalStateException e) {
            throw new PatientInactiveException(e.getMessage());
        }
        
        // Persist the updated patient
        patientPersistencePort.save(patient);
    }
}
