package com.project.back_end.services;

import ch.qos.logback.core.net.ObjectWriter;
import com.project.back_end.DTO.AppointmentDTO;
import com.project.back_end.models.Appointment;
import com.project.back_end.models.Patient;
import com.project.back_end.repo.AppointmentRepository;
import com.project.back_end.repo.PatientRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class PatientService {
// 1. **Add @Service Annotation**:
//    - The `@Service` annotation is used to mark this class as a Spring service component.
//    - It will be managed by Spring's container and used for business logic related to patients and appointments.
//    - Instruction: Ensure that the `@Service` annotation is applied above the class declaration.

// 2. **Constructor Injection for Dependencies**:
//    - The `PatientService` class has dependencies on `PatientRepository`, `AppointmentRepository`, and `TokenService`.
//    - These dependencies are injected via the constructor to maintain good practices of dependency injection and testing.
//    - Instruction: Ensure constructor injection is used for all the required dependencies.

    private static final Logger logger = LoggerFactory.getLogger(PatientService.class);

    private final PatientRepository patientRepository;
    private final AppointmentRepository appointmentRepository;
    private final TokenService tokenService;

    // Constructor injection
    public PatientService(PatientRepository patientRepository,
                          AppointmentRepository appointmentRepository,
                          TokenService tokenService) {
        this.patientRepository = patientRepository;
        this.appointmentRepository = appointmentRepository;
        this.tokenService = tokenService;
    }

// 3. **createPatient Method**:
//    - Creates a new patient in the database. It saves the patient object using the `PatientRepository`.
//    - If the patient is successfully saved, the method returns `1`; otherwise, it logs the error and returns `0`.
//    - Instruction: Ensure that error handling is done properly and exceptions are caught and logged appropriately.

    @Transactional
    public int createPatient(Patient patient){
        try {
            patientRepository.save(patient);
            return 1;
        }
        catch (Exception e){
            logger.error("Error creating patient {}: {}", patient.getEmail(), e.getMessage(), e);
            return 0;
        }
    }

// 4. **getPatientAppointment Method**:
//    - Retrieves a list of appointments for a specific patient, based on their ID.
//    - The appointments are then converted into `AppointmentDTO` objects for easier consumption by the API client.
//    - This method is marked as `@Transactional` to ensure database consistency during the transaction.
//    - Instruction: Ensure that appointment data is properly converted into DTOs and the method handles errors gracefully.

    @Transactional
    public ResponseEntity<Map<String, Object>> getPatientAppointment(Long id, String token){
        Map<String, Object> map = new HashMap<>();
        List<Appointment> appointments = appointmentRepository.findByPatientId(id);
        List<AppointmentDTO> appointmentDTOS = new ArrayList<>();

        if (appointments == null){
            map.put("error", "appointment not found");
            return new ResponseEntity<>(map, HttpStatus.NOT_FOUND);
        }
        else{
            try{
                for(Appointment appointment : appointments){
                    appointmentDTOS.add(mapToDTO(appointment));
                }

                map.put("appointments", appointmentDTOS);
                return new ResponseEntity<>(map, HttpStatus.OK);
            }
            catch (Exception e){
                map.put("error", e.getMessage());
                return new ResponseEntity<>(map, HttpStatus.INTERNAL_SERVER_ERROR   );
            }
        }
    }

// 5. **filterByCondition Method**:
//    - Filters appointments for a patient based on the condition (e.g., "past" or "future").
//    - Retrieves appointments with a specific status (0 for future, 1 for past) for the patient.
//    - Converts the appointments into `AppointmentDTO` and returns them in the response.
//    - Instruction: Ensure the method correctly handles "past" and "future" conditions, and that invalid conditions are caught and returned as errors.

    public ResponseEntity<Map<String, Object>> filterByCondition(Long id, String condition) {
        Map<String, Object> map = new HashMap<>();
        List<Appointment> appointments;

        if (condition.equals("past")) {
            appointments = appointmentRepository.findByPatient_IdAndStatusOrderByAppointmentTimeAsc(id, 1);
        }
        else if (condition.equals("future")) {
            appointments = appointmentRepository.findByPatient_IdAndStatusOrderByAppointmentTimeAsc(id, 0);
        }
        else {
            map.put("error", "Invalid filter");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(map);
        }

        List<AppointmentDTO> appointmentDTOS = appointments.stream().map(appointment -> mapToDTO(appointment)).collect(Collectors.toList());
        map.put("appointments", appointmentDTOS);
        return ResponseEntity.status(HttpStatus.OK).body(map);
    }

// 6. **filterByDoctor Method**:
//    - Filters appointments for a patient based on the doctor's name.
//    - It retrieves appointments where the doctor’s name matches the given value, and the patient ID matches the provided ID.
//    - Instruction: Ensure that the method correctly filters by doctor's name and patient ID and handles any errors or invalid cases.

    public ResponseEntity<Map<String, Object>> filterByDoctor(Long patientId, String name){
        Map<String, Object> map = new HashMap<>();
        List<AppointmentDTO> appointmentDTOS;

        if(name != null && patientId != null){
            List<Appointment> appointments = appointmentRepository.filterByDoctorNameAndPatientId(name, patientId);
            appointmentDTOS = appointments.stream().map(appointment -> mapToDTO(appointment)).collect(Collectors.toList());

            map.put("appointments", appointmentDTOS);
            return ResponseEntity.status(HttpStatus.OK).body(map);
        }
        else{
            map.put("error", "Invalid filter");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(map);
        }
    }

// 7. **filterByDoctorAndCondition Method**:
//    - Filters appointments based on both the doctor's name and the condition (past or future) for a specific patient.
//    - This method combines filtering by doctor name and appointment status (past or future).
//    - Converts the appointments into `AppointmentDTO` objects and returns them in the response.
//    - Instruction: Ensure that the filter handles both doctor name and condition properly, and catches errors for invalid input.

    public ResponseEntity<Map<String, Object>> filterByDoctorAndCondition(Long patientId, String name, String condition) {
        Map<String, Object> map = new HashMap<>();
        List<Appointment> appointments;
        List<AppointmentDTO> appointmentDTOS;

        if(condition.equals("past")) {
            appointments = appointmentRepository.findByDoctorNameAndPatientIdAndStatus(name, patientId, 0);
            appointmentDTOS = appointments.stream().map(appointment -> mapToDTO(appointment)).collect(Collectors.toList());

            map.put("appointments", appointmentDTOS);
            return ResponseEntity.status(HttpStatus.OK).body(map);
        }
        else if (condition.equals("future")) {
            appointments = appointmentRepository.findByDoctorNameAndPatientIdAndStatus(name, patientId, 1);
            appointmentDTOS = appointments.stream().map(appointment -> mapToDTO(appointment)).collect(Collectors.toList());

            map.put("appointments", appointmentDTOS);
            return ResponseEntity.status(HttpStatus.OK).body(map);
        }
        else {
            map.put("error", "Invalid filter");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(map);
        }
    }

// 8. **getPatientDetails Method**:
//    - Retrieves patient details using the `tokenService` to extract the patient's email from the provided token.
//    - Once the email is extracted, it fetches the corresponding patient from the `patientRepository`.
//    - It returns the patient's information in the response body.
    //    - Instruction: Make sure that the token extraction process works correctly and patient details are fetched properly based on the extracted email.

    public ResponseEntity<Map<String, Object>> getPatientDetails(String token){
        Map<String, Object> map = new HashMap<>();

        String extractedEmail = tokenService.extractEmail(token);
        Patient patient = patientRepository.findByEmail(extractedEmail);

        if(patient == null){
            map.put("error", "patient not found");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(map);
        }
        else{
            map.put("patient", patient);
            return ResponseEntity.status(HttpStatus.OK).body(map);
        }
    }

// 9. **Handling Exceptions and Errors**:
//    - The service methods handle exceptions using try-catch blocks and log any issues that occur. If an error occurs during database operations, the service responds with appropriate HTTP status codes (e.g., `500 Internal Server Error`).
//    - Instruction: Ensure that error handling is consistent across the service, with proper logging and meaningful error messages returned to the client.

// 10. **Use of DTOs (Data Transfer Objects)**:
//    - The service uses `AppointmentDTO` to transfer appointment-related data between layers. This ensures that sensitive or unnecessary data (e.g., password or private patient information) is not exposed in the response.
//    - Instruction: Ensure that DTOs are used appropriately to limit the exposure of internal data and only send the relevant fields to the client.


    private AppointmentDTO mapToDTO(Appointment appointment) {
        return new AppointmentDTO(
                appointment.getId(),
                appointment.getDoctor().getId(),
                appointment.getDoctor().getName(),
                appointment.getPatient().getId(),
                appointment.getPatient().getName(),
                appointment.getPatient().getEmail(),
                appointment.getPatient().getPhone(),
                appointment.getPatient().getAddress(),
                appointment.getAppointmentTime(),
                appointment.getStatus()
        );
    }

}
