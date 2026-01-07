package com.pm.patient_service.config;

import com.pm.patient_service.adapter.secondary.persistence.JpaPatientPersistenceAdapter;
import com.pm.patient_service.application.port.in.DeactivatePatientUseCase;
import com.pm.patient_service.application.port.out.PatientPersistencePort;
import com.pm.patient_service.application.service.DeactivatePatientService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for hexagonal architecture bean wiring
 * 
 * Orchestrates dependency injection:
 * - Input port implementations (use cases) depend on output port interfaces
 * - Adapters are injected as needed
 * - Maintains dependency direction: Adapters → Application → Domain
 */
@Configuration
public class HexagonalArchitectureConfig {

    /**
     * Provide DeactivatePatientUseCase implementation
     * Depends on PatientPersistencePort (output port)
     */
    @Bean
    public DeactivatePatientUseCase deactivatePatientUseCase(PatientPersistencePort patientPersistencePort) {
        return new DeactivatePatientService(patientPersistencePort);
    }
}
