package com.pm.patient_service.dto;

import jakarta.validation.constraints.NotNull;
import com.pm.patient_service.model.PatientStatus;

public class PatientResponseDTO {
    @NotNull
    private String id;

    @NotNull
    private String name;

    @NotNull
    private String email;

    @NotNull
    private String address;

    @NotNull
    private String dateOfBirth;

    private PatientStatus status;

    private String deactivatedDate;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(String dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public PatientStatus getStatus() {
        return status;
    }

    public void setStatus(PatientStatus status) {
        this.status = status;
    }

    public String getDeactivatedDate() {
        return deactivatedDate;
    }

    public void setDeactivatedDate(String deactivatedDate) {
        this.deactivatedDate = deactivatedDate;
    }
}