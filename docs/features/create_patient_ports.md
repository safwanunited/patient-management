# Ports – Create Patient

## Inbound Port
- `CreatePatientUseCase`
  - Method: `PatientResponseDTO create(PatientRequestDTO request)`

## Outbound Port
- `PatientRepositoryPort`
  - Methods:
    - `Patient save(Patient patient)`
    - `boolean existsByEmail(String email)`

## Package
- Inbound: `com.pm.patient_service.application.port.in`
- Outbound: `com.pm.patient_service.application.port.out`
