# Pet Clinic Service

A Spring Boot-based microservice for managing a pet clinic, featuring a multi-module architecture with REST APIs and Docker deployment.

## Project Structure

```
pet-clinic/
├── pet-clinic-api/        # REST API and application entry point
├── pet-clinic-dao/        # Data access and database migrations
├── pet-clinic-utils/      # Shared utilities and common code
├── docker-compose.yml     # Docker composition for local development
└── pom.xml               # Parent POM with dependency management
```

## Technologies

- Java 17
- Spring Boot 3.2
- Spring Data JPA
- MariaDB 10.6
- Flyway for migrations
- Maven for build management
- Docker for containerization

## Prerequisites

- JDK 17 or later
- Maven 3.6+
- Docker and Docker Compose
- MariaDB 10.6 (if running locally)

## Getting Started

1. **Clone the repository**
```bash
git clone https://github.com/yourusername/pet-clinic.git
cd pet-clinic
```

2. **Build the project**
```bash
mvn clean package
```

3. **Run with Docker Compose**
```bash
docker-compose up --build
```

4. **Access the application**
- The service will be available at: `http://localhost:8080/api`
- Swagger wil be available at: `http://localhost:8080/api/swagger-ui/index.html`
- MariaDB will be available at: `localhost:3306`

## Running outside IDE

1. **Build the image**
```bash
docker-compose build
```

2. **Run the container**
```bash
docker-compose up -d
```

3. **Check running containers**
```bash
docker ps
```

4. **Access the application**
- The service will be available at: `http://localhost:8080/api`
- Swagger wil be available at: `http://localhost:8080/api/swagger-ui/index.html`
- MariaDB will be available at: `localhost:3306`

5. **Access the application logs**
```bash
docker logs <container_id_or_name>
```

## Remote debugging

When the application starts on docker, the port 5005 is enabled for remote debugging. Steps to debug using IDE:
- Start application on docker
- Using either Intellij or eclipse, configure debugger to attach to port 5005

Debugging using Intellij IDE:
- Create a new run configuration of type 'Remote JVM Debug'
- In debugging mode dropdown, select 'Attach to remote JVM'
- Ensure port is set to 5005 (default port for JVM debugging). click apply and debug. You can add breakpoints in source code and send request and happy debugging!!!

Debugging using VS code:
- Add below configuration to launch.json
  ```
  {
    "type": "java",
    "name": "Remote Debug",
    "request": "attach",
    "hostName": "localhost",
    "port": 5005
  }
  ```
- Save and debug. You can add breakpoints in source code and send request and happy debugging!!!

Debugging using Eclipse IDE:
- Create a new "Remote Java Application" debug configuration with host "localhost" and port 5005 and debug.

Changing the debug port:
- In dockerfile (in the API module), look for JAVA_OPTS line and change address to your intended port.
- In docker compose file, look for the ports and change the debug port from 5005 to your intended port.
- Run maven clean compile and after that, restart app on docker.
- Then recreate a debug configuration in your favourite IDE to point to specified ports in the docker file and debug.
  

## Module Details

### pet-clinic-api
- Spring Boot application entry point
- REST controllers and API endpoints
- Security configuration
- API documentation

### pet-clinic-dao
- Database entities and repositories
- Flyway migrations
- Database configuration

### pet-clinic-utils
- Shared utilities
- Common DTOs
- Helper classes

## Database Migrations

Database migrations are managed by Flyway and located in:
```
pet-clinic-dao/src/main/resources/db/migration/
```

## Configuration

Key application properties (in pet-clinic-api/src/main/resources/application.properties):
```properties
spring.datasource.url=jdbc:mariadb://localhost:3306/petshop
spring.datasource.username=*** // replace with your db username
spring.datasource.password=*** // replace with your db password
```

## Docker Support

The application can be run in containers using:
```bash
docker-compose up --build
```

This will start:
- MariaDB container
- Application container with the Spring Boot service

## Testing

Run all tests with:
```bash
mvn test
```

## Contributing

1. Fork the repository
2. Create your feature branch
3. Commit your changes
4. Push to the branch
5. Create a Pull Request
