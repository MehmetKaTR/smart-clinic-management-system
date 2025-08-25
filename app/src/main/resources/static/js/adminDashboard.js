// adminDashboard.js
import { openModal } from './components/modals.js';
import { getDoctors, filterDoctors, saveDoctor } from './services/doctorServices.js';
import { createDoctorCard } from './components/doctorCard.js';

// =======================
// Event Binding for Add Doctor Button
// =======================
const addDoctorBtn = document.getElementById('addDocBtn');
if (addDoctorBtn) {
    addDoctorBtn.addEventListener('click', () => openModal('addDoctor'));
}

// =======================
// Load Doctor Cards on Page Load
// =======================
window.addEventListener('DOMContentLoaded', async () => {
    await loadDoctorCards();

    // Attach search and filter listeners
    const searchBar = document.getElementById("searchBar");
    const filterTime = document.getElementById("filterTime");
    const filterSpecialty = document.getElementById("filterSpecialty");

    if (searchBar) searchBar.addEventListener("input", filterDoctorsOnChange);
    if (filterTime) filterTime.addEventListener("change", filterDoctorsOnChange);
    if (filterSpecialty) filterSpecialty.addEventListener("change", filterDoctorsOnChange);
});

// =======================
// Load all doctors and display them
// =======================
async function loadDoctorCards() {
    try {
        const doctors = await getDoctors();
        renderDoctorCards(doctors);
    } catch (error) {
        console.error("Error loading doctors:", error);
        alert("Failed to load doctors. Please try again later.");
    }
}

// =======================
// Render a list of doctors
// =======================
async function renderDoctorCards(doctors) {
    const contentDiv = document.getElementById("content");
    if (!contentDiv) return;
    contentDiv.innerHTML = "";

    if (!doctors || doctors.length === 0) {
        contentDiv.innerHTML = "<p>No doctors available.</p>";
        return;
    }

    for (const doctor of doctors) {
        const card = await createDoctorCard(doctor);
        contentDiv.appendChild(card);
    }
}

// =======================
// Filter doctors based on search and filters
// =======================
async function filterDoctorsOnChange() {
    try {
        const name = document.getElementById("searchBar")?.value || null;
        const time = document.getElementById("filterTime")?.value || null;
        const specialty = document.getElementById("filterSpecialty")?.value || null;

        const result = await filterDoctors(name, time, specialty);

        if (result && result.doctors && result.doctors.length > 0) {
            renderDoctorCards(result.doctors);
        } else {
            const contentDiv = document.getElementById("content");
            if (contentDiv) contentDiv.innerHTML = "<p>No doctors found with the given filters.</p>";
        }
    } catch (error) {
        console.error("Error filtering doctors:", error);
        alert("An error occurred while filtering doctors. Please try again.");
    }
}

// =======================
// Admin adds a new doctor
// =======================
async function adminAddDoctor() {
    try {
        const name = document.getElementById("doctorName")?.value;
        const email = document.getElementById("doctorEmail")?.value;
        const phone = document.getElementById("doctorPhone")?.value;
        const password = document.getElementById("doctorPassword")?.value;
        const specialty = document.getElementById("doctorSpecialty")?.value;

        // Collect availability checkboxes
        const times = Array.from(document.querySelectorAll('input[name="availability"]:checked'))
            .map(input => input.value);

        const token = localStorage.getItem("token");
        if (!token) {
            alert("You must be logged in as admin to add a doctor.");
            return;
        }

        const doctor = { name, email, phone, password, specialty, availability: times };

        const result = await saveDoctor(doctor, token);

        if (result.success) {
            alert("Doctor added successfully!");
            // Close modal and reload doctor list
            openModal('addDoctor', false); // assuming second param closes modal
            await loadDoctorCards();
        } else {
            alert(`Failed to add doctor: ${result.message}`);
        }

    } catch (error) {
        console.error("Error adding doctor:", error);
        alert("An error occurred while adding the doctor. Please try again.");
    }
}

// Optional: Attach the function globally if used in HTML form submission
window.adminAddDoctor = adminAddDoctor;


/*
  This script handles the admin dashboard functionality for managing doctors:
  - Loads all doctor cards
  - Filters doctors by name, time, or specialty
  - Adds a new doctor via modal form


  Attach a click listener to the "Add Doctor" button
  When clicked, it opens a modal form using openModal('addDoctor')


  When the DOM is fully loaded:
    - Call loadDoctorCards() to fetch and display all doctors


  Function: loadDoctorCards
  Purpose: Fetch all doctors and display them as cards

    Call getDoctors() from the service layer
    Clear the current content area
    For each doctor returned:
    - Create a doctor card using createDoctorCard()
    - Append it to the content div

    Handle any fetch errors by logging them


  Attach 'input' and 'change' event listeners to the search bar and filter dropdowns
  On any input change, call filterDoctorsOnChange()


  Function: filterDoctorsOnChange
  Purpose: Filter doctors based on name, available time, and specialty

    Read values from the search bar and filters
    Normalize empty values to null
    Call filterDoctors(name, time, specialty) from the service

    If doctors are found:
    - Render them using createDoctorCard()
    If no doctors match the filter:
    - Show a message: "No doctors found with the given filters."

    Catch and display any errors with an alert


  Function: renderDoctorCards
  Purpose: A helper function to render a list of doctors passed to it

    Clear the content area
    Loop through the doctors and append each card to the content area


  Function: adminAddDoctor
  Purpose: Collect form data and add a new doctor to the system

    Collect input values from the modal form
    - Includes name, email, phone, password, specialty, and available times

    Retrieve the authentication token from localStorage
    - If no token is found, show an alert and stop execution

    Build a doctor object with the form values

    Call saveDoctor(doctor, token) from the service

    If save is successful:
    - Show a success message
    - Close the modal and reload the page

    If saving fails, show an error message
*/
