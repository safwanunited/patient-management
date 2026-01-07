package com.pm.patient_service.domain.patient.model;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Domain model for Patient (pure Java, no Spring/JPA)
 */
public class Patient {
    private final String name;
    private final String email;
    private final String address;
    private final LocalDate dob;
    private final LocalDate registeredDate;
    private PatientStatus status;
    private LocalDate deactivatedDate;

    public Patient(String name, String email, String address, LocalDate dob, LocalDate registeredDate) {
        if (name == null || name.isBlank()) throw new IllegalArgumentException("Name is required");
        if (email == null || email.isBlank()) throw new IllegalArgumentException("Email is required");
        if (address == null || address.isBlank()) throw new IllegalArgumentException("Address is required");
        if (dob == null) throw new IllegalArgumentException("DOB is required");
        if (registeredDate == null) throw new IllegalArgumentException("Registered date is required");
        if (!dob.isBefore(LocalDate.now())) throw new IllegalArgumentException("DOB must be in the past");
        this.name = name;
        this.email = email;
        this.address = address;
        this.dob = dob;
        this.registeredDate = registeredDate;
        this.status = PatientStatus.ACTIVE;
        this.deactivatedDate = null;
    }

    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getAddress() { return address; }
    public LocalDate getDob() { return dob; }
    public LocalDate getRegisteredDate() { return registeredDate; }
    public PatientStatus getStatus() { return status; }
    public LocalDate getDeactivatedDate() { return deactivatedDate; }

    public void deactivate(LocalDate deactivationDate) {
        if (this.status == PatientStatus.INACTIVE) {
            throw new IllegalStateException("Patient is already inactive");
        }
        if (deactivationDate == null) {
            throw new IllegalArgumentException("Deactivation date is required");
        }
        this.status = PatientStatus.INACTIVE;
        this.deactivatedDate = deactivationDate;
    }

    public void activate() {
        if (this.status == PatientStatus.ACTIVE) {
            throw new IllegalStateException("Patient is already active");
        }
        this.status = PatientStatus.ACTIVE;
        this.deactivatedDate = null;
    }

    public boolean isActive() {
        return this.status == PatientStatus.ACTIVE;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Patient patient = (Patient) o;
        return Objects.equals(email, patient.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(email);
    }
}

