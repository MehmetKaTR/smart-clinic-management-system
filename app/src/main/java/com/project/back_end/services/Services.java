package com.project.back_end.services;

import com.project.back_end.models.Admin;
import com.project.back_end.models.Appointment;
import com.project.back_end.models.Doctor;
import com.project.back_end.models.Patient;
import com.project.back_end.repo.*;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class Services {
// 1. **@Service Annotation**
// The @Service annotation marks this class as a service component in Spring. This allows Spring to automatically detect it through component scanning
// and manage its lifecycle, enabling it to be injected into controllers or other services using @Autowired or constructor injection.

// 2. **Constructor Injection for Dependencies**
// The constructor injects all required dependencies (TokenService, Repositories, and other Services). This approach promotes loose coupling, improves testability,
// and ensures that all required dependencies are provided at object creation time.

    private final AppointmentRepository appointmentRepository;
    private final DoctorRepository doctorRepository;
    private final AdminRepository adminRepository;
    private final PatientRepository patientRepository;
    private final PrescriptionRepository prescriptionRepository;

    private final AppointmentService appointmentService;
    private final DoctorService doctorService;
    private final PatientService patientService;
    private final PrescriptionService prescriptionService;
    private final TokenService tokenService;

    public Services(AppointmentRepository appointmentRepository, DoctorRepository doctorRepository, AdminRepository adminRepository, PatientRepository patientRepository, PrescriptionRepository prescriptionRepository, AppointmentService appointmentService, DoctorService doctorService, PatientService patientService, PrescriptionService prescriptionService, TokenService tokenService) {
        this.appointmentRepository = appointmentRepository;
        this.doctorRepository = doctorRepository;
        this.adminRepository = adminRepository;
        this.patientRepository = patientRepository;
        this.prescriptionRepository = prescriptionRepository;
        this.appointmentService = appointmentService;
        this.doctorService = doctorService;
        this.patientService = patientService;
        this.prescriptionService = prescriptionService;
        this.tokenService = tokenService;
    }

    // 3. **validateToken Method**
// This method checks if the provided JWT token is valid for a specific user. It uses the TokenService to perform the validation.
// If the token is invalid or expired, it returns a 401 Unauthorized response with an appropriate error message. This ensures security by preventing
// unauthorized access to protected resources.

    public String validateToken(String token, String role) {
        if (tokenService.validateToken(token, role)) {
            return "Token is valid";
        } else {
            return "401 Unauthorized: Token is invalid or expired";
        }
    }

// 4. **validateAdmin Method**
// This method validates the login credentials for an admin user.
// - It first searches the admin repository using the provided username.
// - If an admin is found, it checks if the password matches.
// - If the password is correct, it generates and returns a JWT token (using the admin’s username) with a 200 OK status.
// - If the password is incorrect, it returns a 401 Unauthorized status with an error message.
// - If no admin is found, it also returns a 401 Unauthorized.
// - If any unexpected error occurs during the process, a 500 Internal Server Error response is returned.
// This method ensures that only valid admin users can access secured parts of the system.

    public String validateAdmin(String username, String password) {
        try {
            var admin = adminRepository.findByUsername(username);
            if (admin == null) {
                return "401 Unauthorized: Admin not found";
            }

            if (!admin.getPassword().equals(password)) {
                return "401 Unauthorized: Incorrect password";
            }

            String token = tokenService.generateToken(admin.getUsername());
            return "200 OK: Token=" + token;

        } catch (Exception e) {
            return "500 Internal Server Error: " + e.getMessage();
        }
    }

// 5. **filterDoctor Method**
// This method provides filtering functionality for doctors based on name, specialty, and available time slots.
// - It supports various combinations of the three filters.
// - If none of the filters are provided, it returns all available doctors.
// This flexible filtering mechanism allows the frontend or consumers of the API to search and narrow down doctors based on user criteria.


// 6. **validateAppointment Method**
// This method validates if the requested appointment time for a doctor is available.
// - It first checks if the doctor exists in the repository.
// - Then, it retrieves the list of appointments for the doctor on the specified time slot.
// - If no appointment exists in that time slot, it means the slot is available → return 1.
// - If there is already an appointment, the slot is taken → return 0.
// - If the doctor doesn’t exist, return -1.
// This logic prevents overlapping or invalid appointment bookings.

    public int validateAppointment(Doctor doctor, LocalDateTime appointmentDate) {
        try {
            if (doctorRepository.findById(doctor.getId()).isEmpty()) {
                return -1;
            }

            List<Appointment> appointments = appointmentRepository
                    .findByDoctorIdAndAppointmentTimeBetween(
                            doctor.getId(),
                            appointmentDate,
                            appointmentDate.plusHours(1)
                    );

            if (appointments.isEmpty()) {
                return 1;
            } else {
                return 0;
            }

        } catch (Exception e) {
            return -1;
        }
    }

// 7. **validatePatient Method**
// This method checks whether a patient with the same email or phone number already exists in the system.
// - If a match is found, it returns false (indicating the patient is not valid for new registration).
// - If no match is found, it returns true.
// This helps enforce uniqueness constraints on patient records and prevent duplicate entries.

    public boolean validatePatient(String email, String phone) {
        try {
            if (patientRepository.findByEmailOrPhone(email, phone) != null) {
                return false;
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

// 8. **validatePatientLogin Method**
// This method handles login validation for patient users.
// - It looks up the patient by email.
// - If found, it checks whether the provided password matches the stored one.
// - On successful validation, it generates a JWT token and returns it with a 200 OK status.
// - If the password is incorrect or the patient doesn't exist, it returns a 401 Unauthorized with a relevant error.
// - If an exception occurs, it returns a 500 Internal Server Error.
// This method ensures only legitimate patients can log in and access their data securely.

    public String validatePatientLogin(String email, String password) {
        try {
            var patient = patientRepository.findByEmail(email);

            if (patient == null) {
                return "401 Unauthorized: Patient not found";
            }

            if (!patient.getPassword().equals(password)) {
                return "401 Unauthorized: Incorrect password";
            }

            String token = tokenService.generateToken(patient.getEmail());
            return "200 OK: Token=" + token;

        } catch (Exception e) {
            return "500 Internal Server Error: " + e.getMessage();
        }
    }

// 9. **filterPatient Method**
// This method filters a patient's appointment history based on condition and doctor name.
// - It extracts the email from the JWT token to identify the patient.
// - Depending on which filters (condition, doctor name) are provided, it delegates the filtering logic to PatientService.
// - If no filters are provided, it retrieves all appointments for the patient.
// This flexible method supports patient-specific querying and enhances user experience on the client side.

    public List<?> filterPatient(Long patientId, String doctorName, String condition) {
        try {
            Patient patient = patientRepository.findById(patientId).orElse(null);

            if (patient == null) {
                return new ArrayList<>();
            }

            tokenService.extractEmail(patient.getEmail());

            // Filtre kombinasyonları
            if (doctorName == null && condition == null) {
                return patientService.getPatientAppointment(patient);
            } else if (doctorName != null && condition == null) {
                return patientService.filterByDoctor(patientId, doctorName);
            } else if (doctorName == null && condition != null) {
                return patientService.filterByCondition(patientId, condition);
            } else {
                return patientService.filterByDoctorAndCondition(patientId, doctorName, condition);
            }

        } catch (Exception e) {
            return new ArrayList<>();
        }
    }
}

