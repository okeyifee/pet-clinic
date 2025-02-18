Pet Clinic Service
A Spring Boot-based microservice for managing a pet clinic, featuring a multi-module architecture with REST APIs and Docker deployment.

Project Structure
pet-clinic/
├── pet-clinic-api/        # REST API and application entry point
├── pet-clinic-dao/        # Data access and database migrations
├── pet-clinic-utils/      # Shared utilities and common code
├── docker-compose.yml     # Docker composition for local development
└── pom.xml               # Parent POM with dependency management

Technologies
Java 17
Spring Boot 3.2
Spring Data JPA
MariaDB 10.6
Flyway for migrations
Maven for build management
Docker for containerization

Prerequisites
JDK 17 or later
Maven 3.6+
Docker and Docker Compose
MariaDB 10.6 (if running locally)

Getting Started
1. Clone the repo
   git clone https://github.com/yourusername/pet-clinic.git
   cd pet-clinic

2. Build the project
   mvn clean package

3. Run with docker compose
   docker-compose up --build

4. Access the application
   The service will be available at: http://localhost:8080
   MariaDB will be available at: localhost:3306

Module Details

pet-clinic-api:
Spring Boot application entry point
REST controllers and API endpoints
Security configuration
API documentation

pet-clinic-dao:
Database entities and repositories
Flyway migrations
Database configuration

pet-clinic-utils:
Shared utilities
Common DTOs
Helper classes

Database Migrations
Database migrations are managed by Flyway and located in:
pet-clinic-dao/src/main/resources/db/migration/

Docker Support
The application can be run in containers using:
docker-compose up --build

This will start:

MariaDB container
Application container with the Spring Boot service

Testing
Run all tests with:
mvn test

Contributing

Fork the repository
Create your feature branch
Commit your changes
Push to the branch
Ensure test passes
Create a Pull Request to master branch (Master branch is protected from direct pushes)
