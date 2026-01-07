package com.pm.patient_service.application.port.out;

import com.pm.patient_service.domain.patient.model.Patient;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Outbound port for patient persistence
 */
public interface PatientPersistencePort {
    Patient save(Patient patient);
    Optional<Patient> findById(UUID id);
    List<Patient> findAll();
    void delete(UUID id);
    boolean existsByEmail(String email);
    boolean existsByEmailAndIdNot(String email, UUID id);
}
