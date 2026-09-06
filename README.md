# Task Tracker API (Spring Boot)
![Build Status](https://github.com/SherifMuSherif/spring-task-app/actions/workflows/tests.yml/badge.svg)

A RESTful web service for managing tasks, built to demonstrate modern backend architecture and clean code principles using Spring Boot.

## Overview

> This repository is a small practical exercise in Java, Spring Boot, backend architecture, and automated testing.
>
> The core API implementation follows a guide by [Devtiro](https://www.youtube.com/@devtiro). I expanded the project with automated tests covering the web, service, and data layers.

## 🚀 Tech Stack
*   **Framework:** Spring Boot 4.0.0 (Java 25)
*   **Database:** H2 (In-Memory) / Spring Data JPA
*   **Testing:** JUnit 5, Mockito, Spring MockMvc
*   **Architecture:** Layered (Controller → Service → Repository)
*   **Data Mapping:** Manual Mappers / Java Records

## 🧪 Test Strategy & Coverage
The automated test suite verifies the application across three distinct layers to ensure structural integrity and data validation:

1. **Web Layer Integration Tests (`MockMvc`):**
    - Verifies HTTP status codes (200 OK, 201 Created, 400 Bad Request, 404 Not Found).
    - Validates JSON serialization and API contract adherence.
    - Tests `@Valid` input validation (Negative testing for missing/malformed fields).
2. **Service Layer Unit Tests (`Mockito`):**
    - Isolates business logic by mocking the database.
    - Verifies state changes and custom business exceptions (e.g., `TaskNotFoundException`).
3. **Data Layer Tests (`@DataJpaTest`):**
    - Ensures entity-to-database mapping and basic CRUD operations perform correctly in the H2 environment.

## 🏗️ Architecture & Design Decisions
This project enforces strict separation of concerns to maintain a scalable codebase:
*   **Domain Isolation:** The API never exposes database entities directly. Instead, it uses **Java Records** for intermediate Domain Requests and DTOs, ensuring the database layer is completely decoupled from the web layer.
*   **Layered Architecture:** Controllers handle HTTP concerns, services contain business logic, and repositories handle persistence.
*   **Global Exception Handling:** Utilizes `@ControllerAdvice` to intercept custom business exceptions and return standardized, clean JSON error responses to the client.

## 🏃‍♂️ How to Run Locally

This project includes a `docker-compose.yml` file that pulls a pre-built React UI container, allowing you to visually interact with the API.

1. **Clone the repository:**
   ```bash
   git clone https://github.com/sherifmusherif/spring-task-app.git
   cd spring-task-app
   ```

2. **Start the Spring Boot API:** Run the application using the Maven wrapper. This starts the backend on port 8080.
```bash
./mvnw spring-boot:run
```

3. **Start the React UI (in a new terminal tab):** Use Docker Compose to pull and run the UI container.
```bash
docker-compose up
```

4.**View the App:** Open http://localhost:3000 in your browser. The containerized UI is pre-configured to automatically route requests to the API running on your local machine.

## 🙏 Acknowledgments

[Built with Devtiro](https://github.com/devtiro)