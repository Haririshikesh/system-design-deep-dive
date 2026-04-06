# Distributed Notification System 🔔

A scalable, reliable, and fault-tolerant system design for delivering real-time notifications across multiple channels (Email, SMS, Push, In-App).

---

## 🏗️ Step 1: Requirements & Channel Analysis

Before designing the system, we must define what we are building and the channels we support.

### ✅ Functional Requirements (FR)
* **Multi-Channel Delivery:** The system must support Email, SMS, Push Notifications (iOS/Android), and In-App messages.
* **User Preferences:** Users can opt-in or opt-out of specific channels or categories (e.g., receive transactional alerts but block promotional emails).
* **Prioritization:** The system must differentiate between critical alerts (e.g., OTPs, Security warnings) and bulk messages (e.g., Marketing newsletters).
* **Status Tracking:** Track delivery statuses (Sent, Delivered, Read, Bounced).

### ⚙️ Non-Functional Requirements (NFR)
* **High Reliability (No Data Loss):** A notification, once accepted, must not be lost. If it fails, it must be retried.
* **Low Latency for Critical Alerts:** OTPs and transaction alerts must be delivered in under 3 seconds.
* **High Throughput for Bulk:** Must be capable of pushing millions of background promotional messages efficiently.
* **Idempotency:** Prevent duplicate notifications, especially for billing or payment alerts.
* **Rate Limiting:** Protect third-party providers (like Twilio or SendGrid) from being overwhelmed.

---

## 🏛️ Step 2: Distributed Architecture (The Role of Kafka)

To handle high throughput and decouple the core application from external third-party APIs, we introduce an asynchronous, event-driven architecture using **Apache Kafka**.

![System Architecture](./assets/architecture-v1.svg)

### 🧱 Component Breakdown
1. **Notification API (Gateway):** The entry point for internal microservices to request a notification. It performs basic validation and rate-limiting.
2. **User Preferences Cache / DB:** Checks if the user is opted-in and resolves contact info (phone number, email, device token).
3. **The Message Broker (Kafka):** Acts as the shock absorber. It decouples the fast API servers from the slower, third-party network calls. Use Kafka Topics to manage data streams.
    * *Why Kafka?* Incredible throughput, ideal for massive bulk streams (100M+ promo emails) and robust replayability.
4. **Channel Workers (Consumers):** Independent microservices subscribed to specific topics (e.g., `high_priority`, `bulk`). They pull events and call external APIs (SendGrid, Twilio, APNs, FCM).

> [!NOTE] 
> **Decoupling is Key:** By placing a Kafka topic between the API and the Workers, if the external SMS provider goes down, our internal services are unaffected. Events simply stay in the topic until the provider is back online.

---

## 🚦 Step 3: Prioritization Logic (High-Priority vs. Bulk Topics)

Not all notifications are created equal. A password reset email cannot be stuck behind 2 million promotional newsletters.

### Topic Segregation Strategy
To prevent head-of-line blocking, we separate traffic logically and physically:

* **High-Priority Topics (The "Fast Lane"):**
    * **Use Case:** OTPs, Security Alerts, Payment Confirmations.
    * **Setup:** Dedicated topics with isolated consumer groups. These workers are strictly reserved for urgent messages.
    * **SLA:** Delivery within 1-3 seconds.

* **Standard / Bulk Topics:**
    * **Use Case:** Marketing materials, weekly digests.
    * **Setup:** Separate topics handled by a larger pool of workers that process events in batches.
    * **SLA:** Delivery within minutes to hours.

### Rate Limiting the Bulk Output
Third-party providers (APNs, SendGrid) impose strict rate limits. Our Bulk Workers must enforce distributed rate-limiting (e.g., via Redis Token Bucket algorithms) to ensure we don't get IP-banned or severely throttled by the vendors.

---

## 🛡️ Step 4: Dealing with Failure (Retries, Exponential Backoff, & Dead Letter Topics)

Failures in distributed systems are guaranteed. External APIs rate limit, networks partition, and user devices go offline.

### 🔄 1. The Retry Mechanism & Exponential Backoff
If a worker fails to send an SMS because Twilio returns an `HTTP 429 Too Many Requests` or a `50x Server Error`, the worker should not discard the message. 
* It places the event into a **Retry Topic**.
* **Exponential Backoff:** Hard-retrying immediately will just hammer the failing API. Instead, we wait progressively longer between attempts: *Wait 2s → 4s → 8s → 16s → 32s*.

### 🚫 2. Dead Letter Topics (DLT)
If an event fails repeatedly and exhausts its maximum retry count (e.g., 5 attempts), or if it encounters a fatal error (like a "Hard Bounce" invalid email address), it is routed to a **Dead Letter Topic (DLT)**.
* **Purpose:** Prevents "poison pill" messages from clogging the main topics. Data in the DLT can be analyzed later, used to update user profiles (e.g., marking an email as invalid), or manually re-queued.

### 🔑 3. Idempotency Keys
What if the SMS Worker successfully texts the user, but crashes before it can acknowledge the offset to Kafka?
* The broker will assume failure and the message will be re-processed. The user gets two texts.
* **Solution:** Create an **Idempotency Key** (a unique hash of the payload + user ID + timestamp) and store it in Redis. Before sending, the worker checks Redis: `EXISTS(hash)`. If true, skip processing. If false, process and `SET(hash, 24h)`.

---

## 🚀 Step 5: Implementation Blueprint (SOP)

To ensure this system is robust, maintainable, and highly concurrent, the implementation will rely heavily on modern Java capabilities and Gang of Four (GoF) design patterns to cleanly separate concerns across multiple delivery channels.

### 🛠️ Technical Stack

| Component | Technology | Reasoning |
| :--- | :--- | :--- |
| **Framework** | Spring Boot 3.x | Industry standard for microservices; rapid PoC development. |
| **Language** | Java 21 | Virtual Threads allow consumers to handle high I/O wait times easily. |
| **Broker** | Apache Kafka | High throughput and reliability for event-driven systems. |
| **Rate Limit** | Redis | Fast token-bucket rate limiting and Idempotency key storage. |
| **Database** | PostgreSQL | Source of truth for User Preferences and Audit Logs. |

### 📂 Project Structure (The "SOP" Layout)

We organize the Notification Service by domain and technical layer to easily add new channels (like Slack integration) in the future:

```plaintext
src/main/java/com/architect/notification/
├── api/             # REST Endpoints for internal triggers (Facade entry)
├── config/          # Bean, Kafka (Topics), and Redis configuration
├── service/         # Business Logic, DB lookup, & Kafka Producer
├── worker/          # Kafka Consumers (The background receivers)
├── strategy/        # GoF Strategy implementations for channels
│   ├── factory/     # GoF Factory to select the right strategy
│   └── channel/     # SMS, Email, Push implementations
├── model/           # Preferences and Notification entities
└── exception/       # Error handling and DLT routing
```

### 🎯 Core GoF Design Patterns Used

Building a system that interacts with massive scale and diverse 3rd-party APIs (Twilio, SendGrid, Firebase) demands serious decoupling.

#### 1. Strategy Pattern (Behavioral)
**Use Case:** Handling different dispatch logic per channel.
**Why?** Sending an email requires an HTML body and SMTP headers, whereas an SMS strictly needs a 160-character limit string. Instead of creating a God Class (`NotificationSender`) with massive `switch` statements, we create a common interface `NotificationStrategy` with a `deliver()` method. `EmailStrategy` and `SmsStrategy` encapsulate their own robust rules.

#### 2. Factory Method Pattern (Creational)
**Use Case:** Instantiating the correct Strategy dynamically.
**Why?** When a generic Worker pulls a serialized `NotificationEvent` from Kafka, it only knows the channel name. The `StrategyFactory` dispenses the concrete `SmsStrategy` bean. This strictly enforces the **Open/Closed Principle**.

#### 3. Facade Pattern (Structural)
**Use Case:** Providing a simplified API layer.
**Why?** Internal microservices simply call a `NotificationFacade` (`facade.dispatch(request)`), and the Facade orchestrates the complex interaction with Redis, Postgres, and the Kafka producer.

---

## 💻 Step 6: Proof of Concept (PoC) & Walkthrough

The PoC validates our asynchronous, high-priority delivery approach using Kafka and Java Virtual Threads.

### 🧪 Verification Results (Integration)

#### 1. Dispatching a Notification (Gateway Path)
**Request:**
```bash
curl -i -X POST http://localhost:8080/api/v1/notifications \
-H "Content-Type: application/json" \
-d '{
  "userId": "usr_9981",
  "channel": "SMS",
  "priority": "HIGH",
  "payload": {
    "templateId": "otp_template_1",
    "data": { "code": "819022" }
  }
}'
```

**Response (202 Accepted):**
The API generates a tracking ID and accepts the request immediately:
```json
{
  "status": "QUEUED",
  "trackingId": "ntf_abc123xyz",
  "timestamp": "2026-04-06T16:00:01.123Z"
}
```

#### 2. Worker Execution (Consumer Path)
Logs from the `NotificationWorker` on a Virtual Thread:
```plaintext
INFO [VirtualThread-1] c.a.n.worker.NotificationWorker : Pulled event [ntf_abc123xyz] from topic [notification.high_priority]
INFO [VirtualThread-1] c.a.n.strategy.SmsStrategy : Dispatching SMS payload for USER [usr_9981]...
INFO [VirtualThread-1] c.a.n.strategy.SmsStrategy : SMS Delivered successfully.
```

### 🛠️ How to Setup and Run

#### 📋 Prerequisites
- **Java 21** or later (Virtual Thread support)
- **Docker** and **Docker Compose**
- **Maven** (optional, wrapper provided)

#### 🚀 Step-by-Step Execution

1. **Spin up Infrastructure**:
   Go to the service directory and start the Kafka cluster, Redis, and Postgres:
   ```bash
   cd foundational-utilities/notification-system/notification-service
   docker compose up -d
   ```

2. **Run the Application**:
   Use the Maven wrapper to start the Spring Boot service:
   ```bash
   ./mvnw spring-boot:run
   ```

3. **Validate Kafka Topics**:
   Check if the topics were auto-created by the application:
   ```bash
   docker exec -it notification-kafka kafka-topics --list --bootstrap-server localhost:9092
   ```

4. **Verify Connectivity**:
   Ensure you can reach the API:
   ```bash
   curl http://localhost:8080/api/v1/notifications
   ```

---

## 🗓️ Final Progress Tracker

- [x] **Step 1:** Requirements & Channel Analysis
- [x] **Step 2:** Distributed Architecture (The Role of Kafka)
- [x] **Step 3:** Prioritization Logic (High-Priority vs. Bulk Topics)
- [x] **Step 4:** Dealing with Failure (Retries, Exponential Backoff, & Dead Letter Topics)
- [x] **Step 5:** Implementation Blueprint (SOP) 
- [x] **Step 6:** Proof of Concept (PoC) & Walkthrough 🏁
