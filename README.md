# StreetFoodGo 🍴

A distributed street food marketplace built with Spring Boot.

## ✨ Key Features (Implemented)

### 🗺️ Real-Time Delivery Tracking (New!)
- **Self-Hosted OSRM Router**: Fully integrated Open Source Routing Machine (OSRM) running in Docker. No external API keys or rate limits!
- **Interactive Maps**: Leaflet.js + OpenStreetMap integration for visualizing store locations and delivery routes.
- **Live Driver Simulation**: Smart algorithm that simulates driver movement along real road networks (calculated via OSRM) for demo purposes.
- **WebSocket Updates**: Push notifications update the driver's pin on the map instantly without page reloads.

### 🍱 Complete Order Workflow
- **Customer**: Browse stores, customize items (toppings/ingredients), cart management, checkout with precise map-based address selection.
- **Store Owner**: Real-time dashboard to Receive/Accept/Reject orders.
- **Status Updates**: Automatic timeline updates (Pending -> Preparing -> Ready -> Delivering -> Completed).
- **Notifications**: Email & SMS alerts for order status changes using external microservices.

### 🏢 Store Management
- **Dashboard**: Store owners can manage menus, prices, and view order history.
- **Schedule**: Define operating hours. The system automatically prevents orders when the store is closed.
- **Delivery Zones**: Distance calculation using real road networks to enforce maximum delivery range.

### 🏗️ Microservices Architecture
- **StreetFoodGo Core**: Main Spring Boot application handling business logic.
- **NOC Service**: Dedicated microservice for handling notifications (SMS/Email) and user verification.
- **PostgreSQL**: Robust relational database for data persistence.
- **OSRM Container**: Dedicated routing engine container.

---

### 🚀 Prerequisites

- **Docker Desktop** (recommended) or Docker + Docker Compose  
- **Git**  
- *(For local development)* Java 21, Maven, PostgreSQL 15

---

### Setup Instructions

#### Option 1: Run with Docker (Recommended)

1. **Clone the repository:**
   ```bash
   git clone https://github.com/Marget14/Distributed-Systems-Project.git streetfoodgo
   cd streetfoodgo
   ```

2. **Folder structure:**
   ```
   streetfoodgo/
   ├─ services/
   │   └─ Distributed-Systems-Project-NOC/
   ├─ src/
   ├─ docker-compose.yml
   ├─ Dockerfile
   └─ pom.xml
   ```

3. **Start all services:**
   ```bash
   docker-compose up --build
   ```
   Wait ~1–2 minutes for full startup.

4. **Access the services:**
   - API: `http://localhost:8080`
   - Swagger UI: `http://localhost:8080/swagger-ui.html`
   - pgAdmin: `http://127.0.0.1:53807/?key=1a1edf91-28ef-4650-9183-a106869c5c56`  
     - Email: `admin@streetfoodgo.com`  
     - Password: `admin`

5. **Stop everything:**
   ```bash
   docker-compose down
   ```

---

#### Option 2: Run Locally (Development)

1. **Clone the main repo:**
   ```bash
   git clone https://github.com/Marget14/Distributed-Systems-Project.git
   cd Distributed-Systems-Project
   ```

2. **Set up PostgreSQL:**
   - Create database: `streetfoodgo`
   - User: `streetfoodgo`, Password: `streetfoodgo`

3. **Run the NOC service (now inside services folder):**
   ```bash
   cd services/Distributed-Systems-Project-NOC
   ./mvnw spring-boot:run           # macOS/Linux
   ./mvnw.cmd spring-boot:run       # Windows
   ```

4. **Run the main app:**
   ```bash
   cd ../../
   ./mvnw spring-boot:run           # macOS/Linux
   ./mvnw.cmd spring-boot:run       # Windows
   ```

5. **Open the API:**  
   `http://localhost:8080`

---

### 🛠️ Technologies Used

- **Backend**: Spring Boot 3.x  
- **Database**: PostgreSQL 15  
- **Containerization**: Docker & Docker Compose  
- **Build Tool**: Maven  
- **Security**: JWT-based authentication

---

### 📁 Project Structure

```
streetfoodgo/
├── src/
│   ├── main/
│   │   ├── java/
│   │   └── resources/
│   └── test/
├── services/
│   └── Distributed-Systems-Project-NOC/
├── docker-compose.yml
├── Dockerfile
├── pom.xml
└── README.md
```

---

### 🔗 External Services

The **Distributed-Systems-Project-NOC** service is now included inside the `services` folder and provides:

- Phone number validation  
- SMS notifications  
- User lookups  

---

### 💡 Notes

- Docker configuration handles all dependencies and DB initialization automatically.  
- For local dev, ensure both PostgreSQL and the NOC service are running before the Spring Boot app.  
- Default credentials are only for development — ensure you update them for production.

---

### ✅ Quick Start (Docker)

```bash
git clone https://github.com/Marget14/Distributed-Systems-Project.git streetfoodgo
cd streetfoodgo
docker-compose up --build
```

Access:
- API → `http://localhost:8080`
- Swagger → `http://localhost:8081`
- pgAdmin → `http://localhost:5050` (admin@streetfoodgo.com / admin)

To stop, simply run:
```bash
docker-compose down
```
