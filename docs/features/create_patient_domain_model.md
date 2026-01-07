# Domain Model – Create Patient
- registeredDate: LocalDate (mandatory on creation)
`com.pm.patient_service.domain.patient.model.Patient`
## Package

- registeredDate must not be null on creation
- Validate DOB is before today
- Validate email uniqueness via domain service
## Validation

- registeredDate is mandatory only during creation
- DOB must be in the past
- Email must be unique
## Business Rules
- dob: LocalDate (must be in the past)
- address: String
- email: String (must be unique)
- name: String
## Fields


