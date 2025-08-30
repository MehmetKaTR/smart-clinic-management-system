import { API_BASE_URL } from '../config/config';

const DOCTOR_API = API_BASE_URL + '/doctor';

export async function getDoctors() {
    try{
        const response = await fetch(DOCTOR_API, {
            method: 'GET',
            headers: {
                "Content-Type": "application/json",
            }
        });

        if(!response.ok){
            console.error("Failed to fetch doctors:", response.statusText);
            return [];
        }

        const data = await response.json();
        return data.doctors;
    }
    catch(err){
        console.error("Failed to fetch doctors:", err);
        return [];
    }
}

export async function deleteDoctor(id, token) {
    try{
        const response = await fetch(`${DOCTOR_API}/delete/${id}/${token}`, {
            method: 'DELETE',
            headers: {
                "Content-Type": "application/json",
            }
        });

        if(!response.ok){
            return {success: false, message: 'Failed to delete doctor'};
        }

        const data = await response.json();

        return{
            success: data.success || false,
            message: data.message || "Operation completed"
        };
    }
    catch(err){
        console.error("Failed to delete doctor:", err);
        return {success: false, message: "Network or server error"};
    }
}

export async function saveDoctor(doctor, token) {
    try{
        const response = await fetch(`${DOCTOR_API}/save/${token}`, {
            method: 'POST',
            headers: {
                "Content-Type": "application/json",
            },
            body: JSON.stringify(doctor)
        });

        if(!response.ok){
            return {success: false, message: 'Failed to save doctor'};
        }

        const data = await response.json();

        return {
            success: data.success || false,
            message: data.message || "Doctor saved successfully"

        };
    }
    catch(err){
        console.error('Error saving doctor:', error);
        return { success: false, message: 'Network or server error' };
    }
}

export async function filterDoctors(name = "", time = "", speciality = "") {
    try{
        const response = await fetch(`${DOCTOR_API}/filter/${name}/${time}/${speciality}`, {
           method: 'GET',
           headers: {
               "Content-Type": "application/json",
           }});

        if(!response.ok){
            console.error("filterDoctors response no ok");
            return [];
        }

        const data = await response.json();
        return data || {doctors: []};
    }
    catch(err){
        console.error('filterDoctors error:', error);
        alert('Failed to fetch filtered doctors.');
        return { doctors: [] };
    }
}

/*
  Import the base API URL from the config file
  Define a constant DOCTOR_API to hold the full endpoint for doctor-related actions


  Function: getDoctors
  Purpose: Fetch the list of all doctors from the API

   Use fetch() to send a GET request to the DOCTOR_API endpoint
   Convert the response to JSON
   Return the 'doctors' array from the response
   If there's an error (e.g., network issue), log it and return an empty array


  Function: deleteDoctor
  Purpose: Delete a specific doctor using their ID and an authentication token

   Use fetch() with the DELETE method
    - The URL includes the doctor ID and token as path parameters
   Convert the response to JSON
   Return an object with:
    - success: true if deletion was successful
    - message: message from the server
   If an error occurs, log it and return a default failure response


  Function: saveDoctor
  Purpose: Save (create) a new doctor using a POST request

   Use fetch() with the POST method
    - URL includes the token in the path
    - Set headers to specify JSON content type
    - Convert the doctor object to JSON in the request body

   Parse the JSON response and return:
    - success: whether the request succeeded
    - message: from the server

   Catch and log errors
    - Return a failure response if an error occurs


  Function: filterDoctors
  Purpose: Fetch doctors based on filtering criteria (name, time, and specialty)

   Use fetch() with the GET method
    - Include the name, time, and specialty as URL path parameters
   Check if the response is OK
    - If yes, parse and return the doctor data
    - If no, log the error and return an object with an empty 'doctors' array

   Catch any other errors, alert the user, and return a default empty result
*/
