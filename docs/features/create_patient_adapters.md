# Adapters – Create Patient

## Inbound Adapter (Web)
- REST Controller: `PatientController`
- Maps HTTP request to use case
- Validates input

## Outbound Adapter (Persistence)
- JPA Repository: `PatientRepository`
- Implements outbound port

## Package
- Inbound: `com.pm.patient_service.adapter.in.web`
- Outbound: `com.pm.patient_service.adapter.out.persistence`
