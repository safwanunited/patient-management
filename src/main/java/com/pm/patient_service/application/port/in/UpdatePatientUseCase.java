package com.pm.patient_service.application.port.in;

import com.pm.patient_service.dto.PatientRequestDTO;
import com.pm.patient_service.dto.PatientResponseDTO;

import java.util.UUID;

public interface UpdatePatientUseCase {
    PatientResponseDTO update(UUID id, PatientRequestDTO patientRequestDTO);
}
