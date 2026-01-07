package com.pm.patient_service.application.port.in;

import java.util.UUID;

/**
 * Inbound port for deactivating a patient
 */
public interface DeactivatePatientUseCase {
    void deactivate(UUID patientId);
}
