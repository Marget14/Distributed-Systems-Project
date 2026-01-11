# StreetFoodGo

A distributed street food marketplace application built with Spring Boot.

---

## Prerequisites

- **Docker Desktop** (recommended) or Docker + Docker Compose
- **Git**

---

## Setup Instructions

### Option 1: Run with Docker (Recommended)

#### 1. Clone both repositories in the same parent folder
```bash
# Create a working directory
mkdir distributed-systems
cd distributed-systems

# Clone the main project
git clone https://github.com/Marget14/Distributed-Systems-Project.git streetfoodgo

# Clone the external NOC service
git clone https://github.com/gkoulis/DS-Lab-NOC.git DS-Lab-NOC
```

**Your folder structure should look like:**
```
📁 distributed-systems/
  📁 streetfoodgo/
  📁 DS-Lab-NOC/
```

#### 2. Run the application
```bash
cd streetfoodgo
docker-compose up --build
```

Wait for all services to start (this may take 1-2 minutes on first run).

#### 3. Access the application

- **Application API**: http://localhost:8080
- **Swagger UI**: http://localhost:8081
- **pgAdmin**: http://localhost:5050
  - Email: `admin@streetfoodgo.com`
  - Password: `admin`

#### 4. Stop the application
```bash
docker-compose down
```

---

### Option 2: Run Locally (Development)

**Prerequisites:**
- Java 21
- Maven
- PostgreSQL 15

#### 1. Clone the repository
```bash
git clone https://github.com/Marget14/Distributed-Systems-Project.git
cd Distributed-Systems-Project
```

#### 2. Setup PostgreSQL database

Create a database named `streetfoodgo` with user `streetfoodgo` and password `streetfoodgo`.

#### 3. Run the external NOC service

In a separate terminal:
```bash
cd ../DS-Lab-NOC
./mvnw spring-boot:run  # MacOS / Linux
./mvnw.cmd spring-boot:run  # Windows
```

#### 4. Run the application
```bash
./mvnw spring-boot:run  # MacOS / Linux
./mvnw.cmd spring-boot:run  # Windows
```

#### 5. Open in browser

[localhost:8080](http://localhost:8080)

---

## Technologies Used

- Spring Boot 3.x
- PostgreSQL 15
- Docker & Docker Compose
- Maven
- JWT Authentication

---

## Project Structure
```
streetfoodgo/
├── src/
│   ├── main/
│   │   ├── java/
│   │   └── resources/
│   └── test/
├── docker-compose.yml
├── Dockerfile
├── pom.xml
└── README.md
```

---

## External Services

This project integrates with the HUA NOC (Notification Operations Center) service for:
- Phone number validation
- SMS notifications
- User lookups

Repository: https://github.com/gkoulis/DS-Lab-NOC

---

## Notes

- The Docker setup automatically handles all dependencies and database initialization
- For development, make sure both the NOC service and PostgreSQL are running before starting the application
- Default database credentials are for development only - change them in production

---
