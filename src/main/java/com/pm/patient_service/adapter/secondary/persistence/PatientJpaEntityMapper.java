package com.pm.patient_service.adapter.secondary.persistence;

import com.pm.patient_service.domain.patient.model.Patient;
import com.pm.patient_service.domain.patient.model.PatientStatus;
import org.springframework.stereotype.Component;

/**
 * Mapper for converting between domain Patient entity and JPA Patient entity
 * Handles the boundary crossing between domain and persistence layers
 * 
 * Follows hexagonal architecture:
 * - Maps domain entities to/from persistence models
 * - No business logic, only structural transformation
 */
@Component
public class PatientJpaEntityMapper {

    /**
     * Convert domain entity to JPA entity for persistence
     */
    public com.pm.patient_service.model.Patient toJpaEntity(Patient domainPatient) {
        com.pm.patient_service.model.Patient jpaEntity = new com.pm.patient_service.model.Patient();
        
        // Map basic fields (use reflection or direct access if needed)
        // Note: Domain entity has immutable fields, so we need to work with what's available
        jpaEntity.setName(domainPatient.getName());
        jpaEntity.setEmail(domainPatient.getEmail());
        jpaEntity.setAddress(domainPatient.getAddress());
        jpaEntity.setDateOfBirth(domainPatient.getDob());
        jpaEntity.setRegisteredDate(domainPatient.getRegisteredDate());
        jpaEntity.setStatus(mapStatus(domainPatient.getStatus()));
        jpaEntity.setDeactivatedDate(domainPatient.getDeactivatedDate());
        
        return jpaEntity;
    }

    /**
     * Convert JPA entity to domain entity
     */
    public Patient toDomainEntity(com.pm.patient_service.model.Patient jpaEntity) {
        Patient domainPatient = new Patient(
                jpaEntity.getName(),
                jpaEntity.getEmail(),
                jpaEntity.getAddress(),
                jpaEntity.getDateOfBirth(),
                jpaEntity.getRegisteredDate()
        );
        
        // Set status and deactivation date if different from defaults
        if (jpaEntity.getStatus() != null && jpaEntity.getStatus() == com.pm.patient_service.model.PatientStatus.INACTIVE) {
            domainPatient.deactivate(jpaEntity.getDeactivatedDate());
        }
        
        return domainPatient;
    }

    /**
     * Map domain PatientStatus to JPA PatientStatus
     */
    private com.pm.patient_service.model.PatientStatus mapStatus(PatientStatus domainStatus) {
        if (domainStatus == PatientStatus.ACTIVE) {
            return com.pm.patient_service.model.PatientStatus.ACTIVE;
        } else if (domainStatus == PatientStatus.INACTIVE) {
            return com.pm.patient_service.model.PatientStatus.INACTIVE;
        }
        return com.pm.patient_service.model.PatientStatus.ACTIVE;
    }
}
