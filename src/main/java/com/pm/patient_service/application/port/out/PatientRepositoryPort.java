package com.pm.patient_service.application.port.out;

import com.pm.patient_service.domain.patient.model.Patient;

/**
 * Outbound port for patient persistence
 */
public interface PatientRepositoryPort {
    Patient save(Patient patient);
    boolean existsByEmail(String email);
}

