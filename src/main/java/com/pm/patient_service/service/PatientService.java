package com.pm.patient_service.service;

import com.pm.patient_service.application.port.in.CreatePatientUseCase;
import com.pm.patient_service.application.port.in.DeactivatePatientUseCase;
import com.pm.patient_service.application.port.in.UpdatePatientUseCase;
import com.pm.patient_service.dto.PatientRequestDTO;
import com.pm.patient_service.dto.PatientResponseDTO;
import com.pm.patient_service.exception.EmailAlreadyExistsException;
import com.pm.patient_service.exception.PatientNotFoundException;
import com.pm.patient_service.mapper.PatientMapper;
import com.pm.patient_service.model.Patient;
import com.pm.patient_service.model.PatientStatus;
import com.pm.patient_service.repository.PatientRepository;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class PatientService implements CreatePatientUseCase, UpdatePatientUseCase, DeactivatePatientUseCase {
    private PatientRepository patientRepository;

    public PatientService(PatientRepository patientRepository) {
        this.patientRepository = patientRepository;
    }

    public List<PatientResponseDTO> getPatients(){
        List<Patient> patients = patientRepository.findAll();
        return patients.stream().map(PatientMapper::toDTO).toList();
    }

    @Override
    public PatientResponseDTO create(PatientRequestDTO patientRequestDTO){
        if(patientRepository.existsByEmail(patientRequestDTO.getEmail())){
            throw new EmailAlreadyExistsException("A patient with this email " + patientRequestDTO.getEmail() + " already exists");
        }
        Patient newPatient = patientRepository.save(
                PatientMapper.toModel(patientRequestDTO)
        );
        return PatientMapper.toDTO(newPatient);
    }

    @Override
    public PatientResponseDTO update(UUID id, PatientRequestDTO patientRequestDTO){
        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new PatientNotFoundException("Patient not found with ID: " + id));
        
        // Check if patient is active before allowing update
        if (patient.getStatus() == PatientStatus.INACTIVE) {
            throw new IllegalStateException("Cannot update an inactive patient with ID: " + id);
        }
        
        if (patientRepository.existsByEmailAndIdNot(patientRequestDTO.getEmail(), id)) {
            throw new EmailAlreadyExistsException(
                    "A patient with this email " + patientRequestDTO.getEmail() + " already exists"
            );
        }

        patient.setName(patientRequestDTO.getName());
        patient.setAddress(patientRequestDTO.getAddress());
        patient.setEmail(patientRequestDTO.getEmail());
        patient.setDateOfBirth(LocalDate.parse(patientRequestDTO.getDateOfBirth()));

        Patient updatedPatient = patientRepository.save(patient);
        return PatientMapper.toDTO(updatedPatient);
    }

    @Override
    public void deactivate(UUID patientId) {
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new PatientNotFoundException("Patient not found with ID: " + patientId));
        
        if (patient.getStatus() == PatientStatus.INACTIVE) {
            throw new IllegalStateException("Patient is already inactive with ID: " + patientId);
        }
        
        patient.setStatus(PatientStatus.INACTIVE);
        patient.setDeactivatedDate(LocalDate.now());
        patientRepository.save(patient);
    }

    public PatientResponseDTO createPatient(PatientRequestDTO patientRequestDTO){
        return create(patientRequestDTO);
    }

    public PatientResponseDTO updatePatient(UUID id, PatientRequestDTO patientRequestDTO){
        return update(id, patientRequestDTO);
    }

    public void deletePatient(UUID id){
        patientRepository.deleteById(id);
    }
}
