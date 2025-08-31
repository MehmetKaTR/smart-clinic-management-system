package com.project.back_end.controllers;


import com.project.back_end.DTO.Login;
import com.project.back_end.models.Doctor;
import com.project.back_end.services.DoctorService;
import com.project.back_end.services.Services;
import com.project.back_end.services.TokenService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("${api.path}"+"doctor")
public class DoctorController {

// 1. Set Up the Controller Class:
//    - Annotate the class with `@RestController` to define it as a REST controller that serves JSON responses.
//    - Use `@RequestMapping("${api.path}doctor")` to prefix all endpoints with a configurable API path followed by "doctor".
//    - This class manages doctor-related functionalities such as registration, login, updates, and availability.


// 2. Autowire Dependencies:
//    - Inject `DoctorService` for handling the core logic related to doctors (e.g., CRUD operations, authentication).
//    - Inject the shared `Service` class for general-purpose features like token validation and filtering.

    private final DoctorService doctorService;
    private final Services services;
    private final TokenService tokenService;

    @Autowired
    public DoctorController(DoctorService doctorService, Services services, TokenService tokenService) {
        this.doctorService = doctorService;
        this.services = services;
        this.tokenService = tokenService;
    }

    // 3. Define the `getDoctorAvailability` Method:
//    - Handles HTTP GET requests to check a specific doctor’s availability on a given date.
//    - Requires `user` type, `doctorId`, `date`, and `token` as path variables.
//    - First validates the token against the user type.
//    - If the token is invalid, returns an error response; otherwise, returns the availability status for the doctor.

    @GetMapping("/{user}/{doctorId}/{date}/{token}")
    public ResponseEntity<Map<String,Object>> getDoctorAvailability(@PathVariable String user, @PathVariable Long doctorId, @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date, @PathVariable String token) {

        Map<String, Object> map = new HashMap<>();
        ResponseEntity<Map<String,String>> tempMap= services.validateToken(token, user);
        if (!tempMap.getBody().isEmpty()) {
            map.putAll(tempMap.getBody());
            return new ResponseEntity<>(map, tempMap.getStatusCode());
        }
        map.put("message",doctorService.getDoctorAvailability(doctorId,date));
        return ResponseEntity.status(HttpStatus.OK).body(map);
    }

// 4. Define the `getDoctor` Method:
//    - Handles HTTP GET requests to retrieve a list of all doctors.
//    - Returns the list within a response map under the key `"doctors"` with HTTP 200 OK status.

    @GetMapping
    public ResponseEntity<Map<String,Object>> getDoctor() {
        Map<String, Object> map = new HashMap<>();
        map.put("message", doctorService.getDoctors());
        return ResponseEntity.status(HttpStatus.OK).body(map);
    }

// 5. Define the `saveDoctor` Method:
//    - Handles HTTP POST requests to register a new doctor.
//    - Accepts a validated `Doctor` object in the request body and a token for authorization.
//    - Validates the token for the `"admin"` role before proceeding.
//    - If the doctor already exists, returns a conflict response; otherwise, adds the doctor and returns a success message.

    @PostMapping("/{token}")
    public  ResponseEntity<Map<String,String>> saveDoctor(@RequestBody Doctor doctor, @PathVariable String token) {
        Map<String, String> map = new HashMap<>();
        ResponseEntity<Map<String,String>> tempMap= services.validateToken(token, "doctor");
        if (!tempMap.getBody().isEmpty()) {
            return tempMap;
        }

        int result = doctorService.saveDoctor(doctor);
        if(result == 1)
        {
            map.put("message","Doctor added to database");
            return ResponseEntity.status(HttpStatus.OK).body(map);
        }
        else if(result == -1){
            map.put("message","Doctor not added to database");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(map);
        }
        else{
            map.put("message","Some internal error occured");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(map);
        }
    }

// 6. Define the `doctorLogin` Method:
//    - Handles HTTP POST requests for doctor login.
//    - Accepts a validated `Login` DTO containing credentials.
//    - Delegates authentication to the `DoctorService` and returns login status and token information.

    @PostMapping
    public ResponseEntity<Map<String,String>> doctorLogin(@RequestBody @Valid Login login) {
        return doctorService.validateDoctor(login);
    }

// 7. Define the `updateDoctor` Method:
//    - Handles HTTP PUT requests to update an existing doctor's information.
//    - Accepts a validated `Doctor` object and a token for authorization.
//    - Token must belong to an `"admin"`.
//    - If the doctor exists, updates the record and returns success; otherwise, returns not found or error messages.

    @PutMapping("/{token}")
    public ResponseEntity<Map<String, String>> updateDoctor(@RequestBody @Valid Doctor doctor,@PathVariable String token) {
        Map<String, String> response = new HashMap<>();

        ResponseEntity<Map<String,String>> tempMap= services.validateToken(token, "admin");
        if (!tempMap.getBody().isEmpty()) {
            return tempMap;
        }
        int res =doctorService.updateDoctor(doctor);
        if (res==1) {
            response.put("message", "Doctor updated");
            return ResponseEntity.status(HttpStatus.OK).body(response); // 200 OK
        }
        else if(res==-1)
        {
            response.put("message", "Doctor not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response); // 404 Not Found
        }
        response.put("message", "Some internal error occurred");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response); // 409 Conflict
    }

// 8. Define the `deleteDoctor` Method:
//    - Handles HTTP DELETE requests to remove a doctor by ID.
//    - Requires both doctor ID and an admin token as path variables.
//    - If the doctor exists, deletes the record and returns a success message; otherwise, responds with a not found or error message.

    @DeleteMapping("/{id}/{token}")
    public ResponseEntity<Map<String, String>> deleteDoctor(@PathVariable Long id, @PathVariable String token) {
        Map<String, String> response = new HashMap<>();

        ResponseEntity<Map<String, String>> tempMap = services.validateToken(token, "doctor");

        if(!tempMap.getBody().isEmpty()) {
            return tempMap;
        }
        int result = doctorService.deleteDoctor(id);
        if (result == 1) {
            response.put("message", "Doctor deleted");
            return ResponseEntity.status(HttpStatus.OK).body(response);
        }
        else if(result == -1){
            response.put("message", "Doctor not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
        response.put("message", "Some internal error occured");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

// 9. Define the `filter` Method:
//    - Handles HTTP GET requests to filter doctors based on name, time, and specialty.
//    - Accepts `name`, `time`, and `speciality` as path variables.
//    - Calls the shared `Service` to perform filtering logic and returns matching doctors in the response.

    @GetMapping("/filter/{name}/{time}/{speciality}")
    public ResponseEntity<Map<String, Object>> filter(@PathVariable String name, @PathVariable String time, @PathVariable String speciality) {
        Map<String, Object> map = new HashMap<>();
        map = services.filterDoctor(name, time, speciality);
        return ResponseEntity.status(HttpStatus.OK).body(map);
    }

}
