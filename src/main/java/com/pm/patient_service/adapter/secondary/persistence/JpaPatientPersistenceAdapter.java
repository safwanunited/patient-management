package com.pm.patient_service.adapter.secondary.persistence;

import com.pm.patient_service.application.port.out.PatientPersistencePort;
import com.pm.patient_service.domain.patient.model.Patient;
import com.pm.patient_service.repository.PatientRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Secondary adapter implementing PatientPersistencePort
 * Bridges domain layer with JPA persistence
 * 
 * Follows hexagonal architecture:
 * - Implements output port interface
 * - Converts between domain entities and JPA entities
 * - No domain logic here, only persistence mapping
 */
@Repository
public class JpaPatientPersistenceAdapter implements PatientPersistencePort {
    private final PatientRepository patientRepository;
    private final PatientJpaEntityMapper mapper;

    public JpaPatientPersistenceAdapter(PatientRepository patientRepository, PatientJpaEntityMapper mapper) {
        this.patientRepository = patientRepository;
        this.mapper = mapper;
    }

    @Override
    public Patient save(Patient patient) {
        // Convert domain entity to JPA entity
        com.pm.patient_service.model.Patient jpaEntity = mapper.toJpaEntity(patient);
        // Save via JPA repository
        com.pm.patient_service.model.Patient saved = patientRepository.save(jpaEntity);
        // Convert back to domain entity
        return mapper.toDomainEntity(saved);
    }

    @Override
    public Optional<Patient> findById(UUID id) {
        return patientRepository.findById(id)
                .map(mapper::toDomainEntity);
    }

    @Override
    public List<Patient> findAll() {
        return patientRepository.findAll().stream()
                .map(mapper::toDomainEntity)
                .collect(Collectors.toList());
    }

    @Override
    public void delete(UUID id) {
        patientRepository.deleteById(id);
    }

    @Override
    public boolean existsByEmail(String email) {
        return patientRepository.existsByEmail(email);
    }

    @Override
    public boolean existsByEmailAndIdNot(String email, UUID id) {
        return patientRepository.existsByEmailAndIdNot(email, id);
    }
}
