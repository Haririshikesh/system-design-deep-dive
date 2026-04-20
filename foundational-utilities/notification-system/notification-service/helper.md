# 🚀 Notification Service: Quick Start Helper

This guide helps you set up, run, and test the Distributed Notification System from scratch.

---

## 🛠️ Step 1: Prerequisites
Ensure you have the following installed:
- **Java 21** or later.
- **Docker** and **Docker Compose**.
- **Postgres** (Make sure local Postgres is stopped if it's using port `5432`).
- **cURL** (for testing).

---

## 🏗️ Step 2: Infrastructure Setup
Start the local database, cache, message broker, and mail tester:

```bash
# Navigate to the service directory
cd foundational-utilities/notification-system/notification-service

# Spin up Postgres, Redis, Kafka, and MailHog
docker compose up -d
```

> [!NOTE]
> If you get a "Port 5432 is already in use" error, stop your local PostgreSQL service:
> `sudo systemctl stop postgresql`

---

## 📦 Step 3: Fast-Track Dependencies & Build
The project includes a Maven wrapper, so you don't need to install Maven manually.

```bash
# Clean and compile to download dependencies and verify build
./mvnw clean compile
```

---

## 🚀 Step 4: Start the Application
Run the Spring Boot application:

```bash
./mvnw spring-boot:run
```

The application will start on port `8080`.

---

## 🧪 Step 5: How to Test (One-by-One)

### 1. Verify Infrastructure is Healthy
- **MailHog UI:** Open [http://localhost:8025](http://localhost:8025) in your browser.
- **Kafka UI:** Open [http://localhost:8081](http://localhost:8081) to visualize clusters, topics, and consumer groups.
- **Kafka Topics:** Run `docker exec -it notification-kafka kafka-topics --list --bootstrap-server localhost:9092` to see `notification.high_priority` and `notification.bulk`.

### 2. Send a Test Email Notification
In a new terminal, fire a request to the API:

```bash
curl -i -X POST http://localhost:8080/api/v1/notifications \
-H "Content-Type: application/json" \
-d '{
  "userId": "usr_9981",
  "channel": "EMAIL",
  "priority": "HIGH",
  "payload": {
    "templateId": "welcome_email",
    "data": { "name": "Rishi", "message": "Welcome to the system!" }
  }
}'
```

### 3. Send a Test SMS Notification
Verify the multi-channel strategy by sending an SMS:

```bash
curl -i -X POST http://localhost:8080/api/v1/notifications \
-H "Content-Type: application/json" \
-d '{
  "userId": "usr_9981",
  "channel": "SMS",
  "priority": "HIGH",
  "payload": {
    "templateId": "otp_delivery",
    "data": { "code": "9910", "message": "Your OTP is 9910" }
  }
}'
```

### 4. Verify Delivery
- **Email:** Open the **MailHog Web UI** ([http://localhost:8025](http://localhost:8025)).
- **SMS:** Check the application console logs for "SMS successfully handshaked with Provider".
- **Kafka Streams:** Use the **Kafka UI** ([http://localhost:8081](http://localhost:8081)) to watch the events being produced and consumed in real-time.

### 5. Verify Kafka Retries (Failure Simulation)
If something fails, check the application logs. You will see the system retrying the message with **Exponential Backoff** (2s, 4s, 8s...) before it finally lands in the **Dead Letter Topic (DLT)**.

---

## 🧹 Cleanup
To stop everything and remove volumes:
```bash
docker compose down -v
```
