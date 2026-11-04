# Insurance Claims Service

A robust Spring Boot application designed to manage insurance claims, streamline workflow state transitions (Submission $\rightarrow$ Under Review $\rightarrow$ Approved/Rejected), enforce business rules (such as duplicate claim prevention), and maintain a complete status history audit trail.

---

## Tech Stack & Architecture

- **Backend Framework:** Spring Boot 4.1.2
- **Data Persistence:** Spring Data JPA, Hibernate
- **Database:** H2 (in-memory) and PostgreSQL
- **Validation & Exceptions:** Jakarta Validation, Custom Exception Handling
- **Testing:** JUnit 5, Mockito
- **Build Tool:** Maven

---

## Project Structure

```text
claims-service/
├── src/
│   ├── main/
│   │   ├── java/com/insurance/claims/claims_service/
│   │   │   ├── controller/      # REST API Controllers
│   │   │   ├── dto/             # Data Transfer Objects
│   │   │   ├── entity/          # JPA Entities (InsuranceClaim, ClaimStatusHistory)
│   │   │   ├── exception/       # Global Exception Handlers & Custom Exceptions
│   │   │   ├── repository/      # Spring Data JPA Repositories
│   │   │   ├── service/         # Core Business Logic
│   │   │   └── workflow/        # Workflow State Transition Engine
│   │   └── resources/
│   │       ├── application.properties          # Default profile and common settings
│   │       ├── application-h2.properties       # H2 datasource and console settings
│   │       └── application-postgres.properties # PostgreSQL datasource settings
│   └── test/
│       └── java/com/insurance/claims/claims_service/
│           └── service/         # Unit Tests (ClaimServiceTest)
└── pom.xml
```

---

## Getting Started & Running Locally

### Prerequisites
- JDK 25 installed.
- Maven installed (or use the provided `./mvnw` wrapper).

### 1. Build the Project
Compile the source code and ensure there are no build errors:
```bash
mvn clean compile
```

### 2. Run Unit Tests
Verify core service business logic and duplicate detection rules:
```bash
mvn test -Dtest=ClaimServiceTest
```

### 3. Start the Spring Boot Application

The application uses the `postgres` profile by default. It connects to a PostgreSQL database at `localhost:5432/claims_db`; configure the database and credentials in `application-postgres.properties` for your environment.

To run with the default PostgreSQL profile:
```bash
mvn spring-boot:run
```

To use the in-memory H2 database instead:
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=h2
```

Both profiles use port `8080`. H2 data is in-memory and is cleared when the application stops. The H2 console is available at `http://localhost:8080/h2-console` only when the `h2` profile is active. Its JDBC URL is `jdbc:h2:mem:claimsdb`, username is `sa`, and password is blank.

---

## End-to-End API Testing Guide

Once the application is running, you can test the REST endpoints using **Postman**, **cURL**, or IntelliJ's HTTP Client.

### 1. Create a Claim (`POST /api/claims`)
Submits a new insurance claim. Automatically checks for duplicates based on Customer ID, Policy Number, Claim Type, and Incident Date.
- **URL:** `http://localhost:8080/api/claims`
- **Method:** `POST`
- **Body:**
  ```json
  {
    "policyNumber": "POL-99999",
    "customerId": "CUST-101",
    "claimType": "AUTO",
    "incidentDate": "2026-11-04",
    "claimedAmount": 2500.00,
    "claimDescription": "Windshield crack from highway debris."
  }
  ```

### 2. Retrieve Claim by ID (`GET /api/claims/{id}`)
Fetches claim details.
- **URL:** `http://localhost:8080/api/claims/1`
- **Method:** `GET`

### 3. List / Filter Claims (`GET /api/claims`)
Supports pagination and optional status filtering.
- **URL:** `http://localhost:8080/api/claims?status=SUBMITTED&page=0&size=10`
- **Method:** `GET`

### 4. Review a Claim (`PATCH /api/claims/{id}/review`)
Transitions the claim status from `SUBMITTED` to `UNDER_REVIEW`.
- **URL:** `http://localhost:8080/api/claims/1/review`
- **Method:** `PATCH`
- **Body:**
  ```json
  {
    "reviewerId": "REV-99",
    "remarks": "Reviewing claim documentation and damage photos."
  }
  ```

### 5. Make a Decision: Approve or Reject (`PATCH /api/claims/{id}/decision`)
Transitions an `UNDER_REVIEW` claim to either `APPROVED` or `REJECTED`.

#### **Approve Payload:**
```json
{
  "decision": "APPROVED",
  "approvedAmount": 2400.00,
  "decidedBy": "REV-99"
}
```

#### **Reject Payload:**
```json
{
  "decision": "REJECTED",
  "rejectionReason": "Policy does not cover windshield damage under current tier.",
  "decidedBy": "REV-99"
}
```

### 6. View Status Audit Trail History (`GET /api/claims/{id}/history`)
Retrieves the complete state transition history for a given claim.
- **URL:** `http://localhost:8080/api/claims/1/history`
- **Method:** `GET`

---

## Postman collection is available in the below json file on root

insurance-demo.postman_collection.json
