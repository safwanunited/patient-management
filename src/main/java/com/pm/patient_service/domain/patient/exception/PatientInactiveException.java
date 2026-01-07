package com.pm.patient_service.domain.patient.exception;

/**
 * Domain exception thrown when attempting to update an inactive patient
 */
public class PatientInactiveException extends RuntimeException {
    public PatientInactiveException(String message) {
        super(message);
    }
}
