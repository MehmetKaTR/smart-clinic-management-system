package com.project.back_end.services;

import com.project.back_end.models.Appointment;
import com.project.back_end.models.Doctor;
import com.project.back_end.repo.AppointmentRepository;
import com.project.back_end.repo.DoctorRepository;
import com.project.back_end.repo.PatientRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DoctorService {

// 1. **Add @Service Annotation**:
//    - This class should be annotated with `@Service` to indicate that it is a service layer class.
//    - The `@Service` annotation marks this class as a Spring-managed bean for business logic.
//    - Instruction: Add `@Service` above the class declaration.

// 2. **Constructor Injection for Dependencies**:
//    - The `DoctorService` class depends on `DoctorRepository`, `AppointmentRepository`, and `TokenService`.
//    - These dependencies should be injected via the constructor for proper dependency management.
//    - Instruction: Ensure constructor injection is used for injecting dependencies into the service.

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private TokenService tokenService;

    private List<LocalTime> getAllTimeSlots(){
        List<LocalTime> timeSlots = new ArrayList<>();
        LocalTime start = LocalTime.of(9, 0);
        LocalTime end = LocalTime.of(17, 0);
        while(!start.isAfter(end)){
            timeSlots.add(start);
            start = start.plusMinutes(1);
        }
        return timeSlots;
    }

// 3. **Add @Transactional Annotation for Methods that Modify or Fetch Database Data**:
//    - Methods like `getDoctorAvailability`, `getDoctors`, `findDoctorByName`, `filterDoctorsBy*` should be annotated with `@Transactional`.
//    - The `@Transactional` annotation ensures that database operations are consistent and wrapped in a single transaction.
//    - Instruction: Add the `@Transactional` annotation above the methods that perform database operations or queries.

// 4. **getDoctorAvailability Method**:
//    - Retrieves the available time slots for a specific doctor on a particular date and filters out already booked slots.
//    - The method fetches all appointments for the doctor on the given date and calculates the availability by comparing against booked slots.
//    - Instruction: Ensure that the time slots are properly formatted and the available slots are correctly filtered.

    public List<Appointment> getDoctorAvailability(Doctor doctor, LocalDate date) {
        List<Appointment> appointments = new ArrayList<>();
        List<Appointment> availables = new ArrayList<>();

        LocalDateTime startTime = date.atStartOfDay().withHour(9);
        LocalDateTime endTime = date.atStartOfDay().withHour(17);

        appointments = appointmentRepository.findByDoctorIdAndAppointmentTimeBetween(doctor.getId(), startTime, endTime);

        availables = appointments.stream().filter(a -> a.getStatus() != null && a.getStatus() == 1)
                .collect(Collectors.toList());

        return availables;
    }

// 5. **saveDoctor Method**:
//    - Used to save a new doctor record in the database after checking if a doctor with the same email already exists.
//    - If a doctor with the same email is found, it returns `-1` to indicate conflict; `1` for success, and `0` for internal errors.
//    - Instruction: Ensure that the method correctly handles conflicts and exceptions when saving a doctor.

    public Integer saveDoctor(Doctor doctor){
        try{
            if(doctorRepository.findByEmail(doctor.getEmail()) == null){
                doctorRepository.save(doctor);
                return 1;
            }
            else{
                return -1;
            }
        }
        catch (Exception e){
            return 0;
        }
    }

// 6. **updateDoctor Method**:
//    - Updates an existing doctor's details in the database. If the doctor doesn't exist, it returns `-1`.
//    - Instruction: Make sure that the doctor exists before attempting to save the updated record and handle any errors properly.

    public Integer updateDoctor(Doctor doctor){
        try{
            Doctor existingDoctor = doctorRepository.findByEmail(doctor.getEmail());
            if(existingDoctor == null){
                return -1;
            }

            existingDoctor.setName(doctor.getName());
            existingDoctor.setSpecialty(doctor.getSpecialty());
            existingDoctor.setPhone(doctor.getPhone());
            existingDoctor.setPassword(doctor.getPassword());

            doctorRepository.save(existingDoctor);
            return 1;
        }
        catch (Exception e){
            return 0;
        }
    }

// 7. **getDoctors Method**:
//    - Fetches all doctors from the database. It is marked with `@Transactional` to ensure that the collection is properly loaded.
//    - Instruction: Ensure that the collection is eagerly loaded, especially if dealing with lazy-loaded relationships (e.g., available times). 

    @Transactional
    public List<Doctor> getDoctors(){
        return doctorRepository.findAll();
    }

// 8. **deleteDoctor Method**:
//    - Deletes a doctor from the system along with all appointments associated with that doctor.
//    - It first checks if the doctor exists. If not, it returns `-1`; otherwise, it deletes the doctor and their appointments.
//    - Instruction: Ensure the doctor and their appointments are deleted properly, with error handling for internal issues.

    public Integer deleteDoctor(Doctor doctor){
        try{
            if(doctorRepository.findByEmail(doctor.getEmail()) == null)
                return -1;

            appointmentRepository.deleteAllByDoctorId(doctor.getId());
            doctorRepository.delete(doctor);
            return 1;
        }
        catch (Exception e){
            return 0;
        }
    }

// 9. **validateDoctor Method**:
//    - Validates a doctor's login by checking if the email and password match an existing doctor record.
//    - It generates a token for the doctor if the login is successful, otherwise returns an error message.
//    - Instruction: Make sure to handle invalid login attempts and password mismatches properly with error responses.

    public String validateDoctor(Doctor doctor){
        try {
            Doctor existingDoctor = doctorRepository.findByEmail(doctor.getEmail());

            if(existingDoctor != null){
                return "Doctor not found";
            }

            if(!existingDoctor.getPassword().equals(doctor.getPassword())){
                return "Invalid password";
            }

            String token = tokenService.generateToken(existingDoctor.getEmail());
            return token;
        }
        catch (Exception e){
            return "Internal error: " + e.getMessage();
        }
    }

// 10. **findDoctorByName Method**:
//    - Finds doctors based on partial name matching and returns the list of doctors with their available times.
//    - This method is annotated with `@Transactional` to ensure that the database query and data retrieval are properly managed within a transaction.
//    - Instruction: Ensure that available times are eagerly loaded for the doctors.

    @Transactional
    public List<Doctor> findDoctorByName(String name){
        try {
            List<Doctor> doctors = doctorRepository.findByNameLike(name);

            if(doctors == null) {
                return new ArrayList<>();
            }

            return doctors;
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

// 11. **filterDoctorsByNameSpecialityAndTime Method**:
//    - Filters doctors based on their name, specialty, and availability during a specific time (AM/PM).
//    - The method fetches doctors matching the name and specialty criteria, then filters them based on their availability during the specified time period.
//    - Instruction: Ensure proper filtering based on both the name and specialty as well as the specified time period.

    @Transactional
    public List<Doctor> filterDoctorsByNameSpecialityAndTime(String name, String speciality, LocalTime start, LocalTime end) {
        List<Doctor> filteredDoctors = new ArrayList<>();

        try {
            List<Doctor> doctors = doctorRepository.findByNameContainingIgnoreCaseAndSpecialtyIgnoreCase(name, speciality);

            for (Doctor doctor : doctors) {
                LocalDateTime startDateTime = LocalDate.now().atTime(start);
                LocalDateTime endDateTime = LocalDate.now().atTime(end);

                List<Appointment> appointments = appointmentRepository.findByDoctorIdAndAppointmentTimeBetween(
                        doctor.getId(), startDateTime, endDateTime
                );

                boolean hasAvailableSlot = appointments.stream().anyMatch(a -> a.getStatus() != null && a.getStatus() == 1);

                if (hasAvailableSlot) {
                    filteredDoctors.add(doctor);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return filteredDoctors;
    }


// 12. **filterDoctorByTime Method**:
//    - Filters a list of doctors based on whether their available times match the specified time period (AM/PM).
//    - This method processes a list of doctors and their available times to return those that fit the time criteria.
//    - Instruction: Ensure that the time filtering logic correctly handles both AM and PM time slots and edge cases.



// 13. **filterDoctorByNameAndTime Method**:
//    - Filters doctors based on their name and the specified time period (AM/PM).
//    - Fetches doctors based on partial name matching and filters the results to include only those available during the specified time period.
//    - Instruction: Ensure that the method correctly filters doctors based on the given name and time of day (AM/PM).

// 14. **filterDoctorByNameAndSpecility Method**:
//    - Filters doctors by name and specialty.
//    - It ensures that the resulting list of doctors matches both the name (case-insensitive) and the specified specialty.
//    - Instruction: Ensure that both name and specialty are considered when filtering doctors.


// 15. **filterDoctorByTimeAndSpecility Method**:
//    - Filters doctors based on their specialty and availability during a specific time period (AM/PM).
//    - Fetches doctors based on the specified specialty and filters them based on their available time slots for AM/PM.
//    - Instruction: Ensure the time filtering is accurately applied based on the given specialty and time period (AM/PM).

// 16. **filterDoctorBySpecility Method**:
//    - Filters doctors based on their specialty.
//    - This method fetches all doctors matching the specified specialty and returns them.
//    - Instruction: Make sure the filtering logic works for case-insensitive specialty matching.

// 17. **filterDoctorsByTime Method**:
//    - Filters all doctors based on their availability during a specific time period (AM/PM).
//    - The method checks all doctors' available times and returns those available during the specified time period.
//    - Instruction: Ensure proper filtering logic to handle AM/PM time periods.

   
}
