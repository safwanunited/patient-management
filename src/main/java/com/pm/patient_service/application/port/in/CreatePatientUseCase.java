package com.pm.patient_service.application.port.in;

import com.pm.patient_service.dto.PatientRequestDTO;
import com.pm.patient_service.dto.PatientResponseDTO;

/**
 * Inbound port for creating a patient
 */
public interface CreatePatientUseCase {
    PatientResponseDTO create(PatientRequestDTO request);
}

