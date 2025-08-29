package com.project.back_end.services;

import com.project.back_end.models.Appointment;
import com.project.back_end.models.Doctor;
import com.project.back_end.models.Patient;
import com.project.back_end.repo.AppointmentRepository;
import com.project.back_end.repo.DoctorRepository;
import com.project.back_end.repo.PatientRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class AppointmentService {
    // 1. **Add @Service Annotation**:
    //    - To indicate that this class is a service layer class for handling business logic.
    //    - The `@Service` annotation should be added before the class declaration to mark it as a Spring service component.
    //    - Instruction: Add `@Service` above the class definition.

    // 2. **Constructor Injection for Dependencies**:
    //    - The `AppointmentService` class requires several dependencies like `AppointmentRepository`, `Service`, `TokenService`, `PatientRepository`, and `DoctorRepository`.
    //    - These dependencies should be injected through the constructor.
    //    - Instruction: Ensure constructor injection is used for proper dependency management in Spring.

    private final AppointmentRepository appointmentRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;

    public AppointmentService(AppointmentRepository appointmentRepository,
                              PatientRepository patientRepository,
                              DoctorRepository doctorRepository) {
        this.appointmentRepository = appointmentRepository;
        this.patientRepository = patientRepository;
        this.doctorRepository = doctorRepository;
    }

    // 3. **Add @Transactional Annotation for Methods that Modify Database**:
    //    - The methods that modify or update the database should be annotated with `@Transactional` to ensure atomicity and consistency of the operations.
    //    - Instruction: Add the `@Transactional` annotation above methods that interact with the database, especially those modifying data.

    // 4. **Book Appointment Method**:
    //    - Responsible for saving the new appointment to the database.
    //    - If the save operation fails, it returns `0`; otherwise, it returns `1`.
    //    - Instruction: Ensure that the method handles any exceptions and returns an appropriate result code.
    @Transactional
    public int bookAppointment(Appointment appointment) {
        try {
            Optional<Patient> patient = patientRepository.findById(appointment.getPatient().getId());
            Optional<Doctor> doctor = doctorRepository.findById(appointment.getDoctor().getId());

            if (patient.isEmpty() || doctor.isEmpty()) return 0;

            List<Appointment> existing = appointmentRepository
                    .findByDoctorIdAndAppointmentTimeBetween(
                            doctor.get().getId(),
                            appointment.getAppointmentTime(),
                            appointment.getAppointmentTime().plusHours(1)
                    );

            if (!existing.isEmpty()) return 0;

            appointmentRepository.save(appointment);
            return 1;

        } catch (Exception e) {
            return 0;
        }
    }

    // 5. **Update Appointment Method**:
    //    - This method is used to update an existing appointment based on its ID.
    //    - It validates whether the patient ID matches, checks if the appointment is available for updating, and ensures that the doctor is available at the specified time.
    //    - If the update is successful, it saves the appointment; otherwise, it returns an appropriate error message.
    //    - Instruction: Ensure proper validation and error handling is included for appointment updates.
    @Transactional
    public String updateAppointment(Long appointmentId, Appointment updatedAppointment, Long patientId) {
        Optional<Appointment> appointmentOpt = appointmentRepository.findById(appointmentId);

        if (appointmentOpt.isEmpty()) return "Appointment not found";

        Appointment appointment = appointmentOpt.get();

        if (!appointment.getPatient().getId().equals(patientId)) {
            return "Unauthorized: Patient mismatch";
        }

        List<Appointment> overlapping = appointmentRepository
                .findByDoctorIdAndAppointmentTimeBetween(
                        updatedAppointment.getDoctor().getId(),
                        updatedAppointment.getAppointmentTime(),
                        updatedAppointment.getAppointmentTime().plusHours(1)
                );

        overlapping.removeIf(a -> a.getId().equals(appointmentId));
        if (!overlapping.isEmpty()) return "Doctor is not available at this time";

        appointment.setAppointmentTime(updatedAppointment.getAppointmentTime());
        appointment.setDoctor(updatedAppointment.getDoctor());

        appointmentRepository.save(appointment);
        return "Appointment updated successfully";
    }

    // 6. **Cancel Appointment Method**:
    //    - This method cancels an appointment by deleting it from the database.
    //    - It ensures the patient who owns the appointment is trying to cancel it and handles possible errors.
    //    - Instruction: Make sure that the method checks for the patient ID match before deleting the appointment.
    @Transactional
    public String cancelAppointment(Long appointmentId, Long patientId) {
        Optional<Appointment> appointmentOpt = appointmentRepository.findById(appointmentId);

        if (appointmentOpt.isEmpty()) return "Appointment not found";

        Appointment appointment = appointmentOpt.get();

        if (!appointment.getPatient().getId().equals(patientId)) {
            return "Unauthorized: Patient mismatch";
        }

        appointmentRepository.delete(appointment);
        return "Appointment cancelled successfully";
    }

    // 7. **Get Appointments Method**:
    //    - This method retrieves a list of appointments for a specific doctor on a particular day, optionally filtered by the patient's name.
    //    - It uses `@Transactional` to ensure that database operations are consistent and handled in a single transaction.
    //    - Instruction: Ensure the correct use of transaction boundaries, especially when querying the database for appointments.
    public List<Appointment> getAppointments(Long doctorId, LocalDateTime dayStart, LocalDateTime dayEnd, String patientName) {
        if (patientName == null || patientName.isEmpty()) {
            return appointmentRepository.findByDoctorIdAndAppointmentTimeBetween(doctorId, dayStart, dayEnd);
        } else {
            return appointmentRepository.findByDoctorIdAndAppointmentTimeBetween(doctorId, dayStart, dayEnd).stream()
                    .filter(a -> a.getPatient().getName().toLowerCase().contains(patientName.toLowerCase()))
                    .toList();
        }
    }

    // 8. **Change Status Method**:
    //    - This method updates the status of an appointment by changing its value in the database.
    //    - It should be annotated with `@Transactional` to ensure the operation is executed in a single transaction.
    //    - Instruction: Add `@Transactional` before this method to ensure atomicity when updating appointment status.
    @Transactional
    public String changeStatus(Long appointmentId, int newStatus) {
        Optional<Appointment> appointmentOpt = appointmentRepository.findById(appointmentId);

        if (appointmentOpt.isEmpty()) return "Appointment not found";

        Appointment appointment = appointmentOpt.get();
        appointment.setStatus(newStatus);
        appointmentRepository.save(appointment);

        return "Status updated successfully";
    }
}
