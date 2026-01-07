package com.pm.patient_service.domain.patient.model;

/**
 * Domain value object for patient status
 * Represents the status of a patient in the system
 */
public enum PatientStatus {
    ACTIVE("ACTIVE"),
    INACTIVE("INACTIVE");

    private final String value;

    PatientStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static PatientStatus fromValue(String value) {
        for (PatientStatus status : PatientStatus.values()) {
            if (status.value.equalsIgnoreCase(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Invalid patient status: " + value);
    }
}
