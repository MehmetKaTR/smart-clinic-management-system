## MySQL Database Design

### Table: admin
- id: INT, Primary Key, Auto Increment
- username: String, Not Null
- password: String, Not Null

### Table: patients
- id: INT, Primary Key, Auto Increment
- username: String, Not Null
- password: String, Not Null
- name: String, Not Null
- age: INT (Min=1, Max=200)
- gender: INT (0:men, 1:women, 2:not defined)

### Table: doctors
- id: INT, Primary Key, Auto Increment
- username: String, Not Null
- password: String, Not Null
- name: String, Not Null
- specialization: String, Not Null

### Table: appointments
- id: INT, Primary Key, Auto Increment
- patient_id: INT, Foreign Key -> patients(id)
- doctor_id: INT, Foreign Key -> doctors(id)
- appointment_date: DATETIME, Not Null
- status: INT (0 = Scheduled, 1 = Completed, 2 = Cancelled)

---

## MongoDB Collection Design

### Collection: prescriptions

```json
{
  "_id": "ObjectId('64abc123456')",
  "patientName": "John Smith",
  "appointmentId": 51,
  "medication": "Paracetamol",
  "dosage": "500mg",
  "doctorNotes": "Take 1 tablet every 6 hours.",
  "refillCount": 2,
  "pharmacy": {
    "name": "Walgreens SF",
    "location": "Market Street"
  }
}
```

### Collection: feedback
```json
{
  "_id": "ObjectId('64def987654')",
  "patientName": "John Smith",
  "appointmentId": 51,
  "rating": 4,
  "comments": "The doctor was very attentive and helpful.",
  "submittedAt": "2025-08-23T18:30:00Z"
}
```

### Collection: logs
```json
{
  "_id": "ObjectId('64xyz999888')",
  "action": "LOGIN",
  "user": "admin_user",
  "timestamp": "2025-08-23T18:45:00Z",
  "tags": ["authentication", "security", "user-action"],
  "metadata": {
    "ip": "192.168.1.101",
    "device": "MacOS",
    "browser": "Safari 18.6",
    "sessionId": "abcd1234efgh5678"
  },
  "details": {
    "location": {
      "city": "San Francisco",
      "country": "USA"
    },
    "loginMethod": "password",
    "2FA": true,
    "previousFailedAttempts": 1
  }
}
```

