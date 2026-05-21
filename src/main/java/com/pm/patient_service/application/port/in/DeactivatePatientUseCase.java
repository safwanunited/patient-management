package com.pm.patient_service.application.port.in;

import java.util.UUID;

public interface DeactivatePatientUseCase {
    void deactivate(UUID patientId);
}
