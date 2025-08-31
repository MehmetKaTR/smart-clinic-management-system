package com.project.back_end.controllers;

import com.project.back_end.models.Appointment;
import com.project.back_end.services.AppointmentService;
import com.project.back_end.services.Services;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@RestController("/appointments")
public class AppointmentController {

// 1. Set Up the Controller Class:
//    - Annotate the class with `@RestController` to define it as a REST API controller.
//    - Use `@RequestMapping("/appointments")` to set a base path for all appointment-related endpoints.
//    - This centralizes all routes that deal with booking, updating, retrieving, and canceling appointments.


// 2. Autowire Dependencies:
//    - Inject `AppointmentService` for handling the business logic specific to appointments.
//    - Inject the general `Service` class, which provides shared functionality like token validation and appointment checks.

    private final AppointmentService appointmentService;
    private final Services services;

    @Autowired
    public AppointmentController(AppointmentService appointmentService, Services services) {
        this.appointmentService = appointmentService;
        this.services = services;
    }

    // 3. Define the `getAppointments` Method:
//    - Handles HTTP GET requests to fetch appointments based on date and patient name.
//    - Takes the appointment date, patient name, and token as path variables.
//    - First validates the token for role `"doctor"` using the `Service`.
//    - If the token is valid, returns appointments for the given patient on the specified date.
//    - If the token is invalid or expired, responds with the appropriate message and status code.

    @GetMapping("/{date}/{patientName}/{token}")
    public ResponseEntity<Map<String, Object>> getAppointments(@PathVariable LocalDate date, String patientName, String token) {
        Map<String, Object> map = new HashMap<>();
        ResponseEntity<Map<String, String>> response = services.validateToken(token, "doctor");
        if(!response.getBody().isEmpty()){
            map.putAll(response.getBody());
            return new ResponseEntity<>(map, response.getStatusCode());
        }
        map = appointmentService.getAppointment(patientName, date, token);
        return new ResponseEntity<>(map, response.getStatusCode());
    }


// 4. Define the `bookAppointment` Method:
//    - Handles HTTP POST requests to create a new appointment.
//    - Accepts a validated `Appointment` object in the request body and a token as a path variable.
//    - Validates the token for the `"patient"` role.
//    - Uses service logic to validate the appointment data (e.g., check for doctor availability and time conflicts).
//    - Returns success if booked, or appropriate error messages if the doctor ID is invalid or the slot is already taken.

    @PostMapping("/{token}")
    public ResponseEntity<Map<String, String>> bookAppointment(@PathVariable String token, @RequestBody Appointment appointment) {
        Map<String, String> map = new HashMap<>();
        ResponseEntity<Map<String, String>> response = services.validateToken(token, "patient");
        if(!response.getBody().isEmpty()){
            map.put("error","Invalid token");
            return new ResponseEntity<>(map, response.getStatusCode());
        }

        int temp = services.validateAppointment(appointment);
        if(temp == -1){
            map.put("error","doctor not found");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(map);
        }
        else if (temp == 1) {
            appointmentService.bookAppointment(appointment);
            map.put("message","appointment booked");
            return new ResponseEntity<>(map, HttpStatus.CREATED);
        }
        else {
            map.put("error","appointment not booked");
            return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
        }
    }

// 5. Define the `updateAppointment` Method:
//    - Handles HTTP PUT requests to modify an existing appointment.
//    - Accepts a validated `Appointment` object and a token as input.
//    - Validates the token for `"patient"` role.
//    - Delegates the update logic to the `AppointmentService`.
//    - Returns an appropriate success or failure response based on the update result.

    @PutMapping("/{token}")
    public ResponseEntity<Map<String, String>> updateAppointment(@RequestBody Appointment appointment, @PathVariable String token) {
        ResponseEntity<Map<String, String>> tempMap = services.validateToken(token, "patient");
        if (!tempMap.getBody().isEmpty()) {
            return tempMap;
        }
        return appointmentService.updateAppointment(appointment);
    }


// 6. Define the `cancelAppointment` Method:
//    - Handles HTTP DELETE requests to cancel a specific appointment.
//    - Accepts the appointment ID and a token as path variables.
//    - Validates the token for `"patient"` role to ensure the user is authorized to cancel the appointment.
//    - Calls `AppointmentService` to handle the cancellation process and returns the result.

    @DeleteMapping("/{id}/{token}")
    public ResponseEntity<Map<String, String>> cancelAppointment(@PathVariable Long id, @PathVariable String token) {
        ResponseEntity<Map<String, String>> tempMap = services.validateToken(token, "patient");
        if (!tempMap.getBody().isEmpty()) {
            return tempMap;
        }
        return appointmentService.cancelAppointment(id,token);
    }
}
