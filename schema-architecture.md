# Schema Architecture

## Section 1: Architecture summary

This Spring Boot application combines both MVC and REST architectures to serve different modules efficiently.  

The Admin and Doctor dashboards are implemented using Thymeleaf templates, providing server-side rendered HTML pages, while other modules, such as patient management, appointments, and prescriptions, are accessible via REST APIs.  

The application interacts with two databases: MySQL stores structured data including patients, doctors, appointments, and admin records, while MongoDB handles unstructured data, specifically prescriptions.  

All controllers delegate requests to a common service layer, which contains the business logic and communicates with the appropriate repositories. MySQL entities are managed using JPA, and MongoDB documents are mapped via Spring Data MongoDB.

## Section 2: Numbered flow of data and control

1. A user accesses the application through a browser (for dashboards) or an API client (for REST endpoints).  
2. The request is routed to the corresponding controller—Thymeleaf controllers for the dashboards or REST controllers for APIs.  
3. The controller forwards the request to the service layer to execute business logic.  
4. The service layer communicates with the proper repository to fetch or modify data in MySQL or MongoDB.  
5. The repository performs the database operation and returns the results to the service layer.  
6. The service layer processes the returned data and prepares a response for the controller.  
7. The controller responds to the user either by rendering a Thymeleaf page or returning JSON data through the REST API.
