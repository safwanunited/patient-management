package com.pm.patient_service.domain.patient.service;

import com.pm.patient_service.domain.patient.model.Patient;
import java.util.function.Predicate;

/**
 * Domain service for patient business rules (pure Java)
 */
public class PatientDomainService {
    /**
     * Validates that the email is unique using a provided predicate.
     * @param email patient's email
     * @param emailExistsPredicate predicate to check if email exists
     * @throws IllegalArgumentException if email is not unique
     */
    public void validateEmailUnique(String email, Predicate<String> emailExistsPredicate) {
        if (emailExistsPredicate.test(email)) {
            throw new IllegalArgumentException("Email already exists: " + email);
        }
    }
}

