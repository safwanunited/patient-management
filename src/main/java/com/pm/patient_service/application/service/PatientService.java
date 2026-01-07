package com.pm.patient_service.application.service;

import com.pm.patient_service.application.port.in.CreatePatientUseCase;
import com.pm.patient_service.application.port.out.PatientRepositoryPort;
import com.pm.patient_service.domain.patient.model.Patient;
import com.pm.patient_service.domain.patient.service.PatientDomainService;
import com.pm.patient_service.dto.PatientRequestDTO;
import com.pm.patient_service.dto.PatientResponseDTO;

/**
 * Application service for patient creation (implements inbound port, uses outbound port)
 */
public class PatientService implements CreatePatientUseCase {
    private final PatientRepositoryPort patientRepositoryPort;
    private final PatientDomainService patientDomainService;

    public PatientService(PatientRepositoryPort patientRepositoryPort) {
        this.patientRepositoryPort = patientRepositoryPort;
        this.patientDomainService = new PatientDomainService();
    }

    @Override
    public PatientResponseDTO create(PatientRequestDTO request) {
        patientDomainService.validateEmailUnique(request.getEmail(), patientRepositoryPort::existsByEmail);
        // Parse dates from String to LocalDate
        java.time.LocalDate dob = java.time.LocalDate.parse(request.getDateOfBirth());
        java.time.LocalDate registeredDate = java.time.LocalDate.parse(request.getRegisteredDate());
        Patient patient = new Patient(
                request.getName(),
                request.getEmail(),
                request.getAddress(),
                dob,
                registeredDate
        );
        Patient saved = patientRepositoryPort.save(patient);
        PatientResponseDTO response = new PatientResponseDTO();
        response.setName(saved.getName());
        response.setEmail(saved.getEmail());
        response.setAddress(saved.getAddress());
        response.setDateOfBirth(saved.getDob().toString());
        // response.setId(...) // Set ID if available from persistence
        return response;
    }
}
