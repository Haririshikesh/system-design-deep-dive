# 🚀 Distributed ID Generator: Quick Start Helper

This guide helps you set up, run, and test the Snowflake-based Distributed ID Generator from scratch.

---

## 🛠️ Step 1: Prerequisites
Ensure you have the following installed:
- **Java 21** or later.
- **Docker** and **Docker Compose**.
- **cURL** (for testing).

---

## 🏗️ Step 2: Infrastructure Setup (Docker)
Start ZooKeeper (coordination), ZooKeeper Navigator UI, and two ID Generator nodes:

```bash
# Navigate to the service directory
cd foundational-utilities/distributed-id-generator/id-generator-service

# Build and spin up all containers
docker compose up --build -d
```

> [!NOTE]
> The first build will take a couple of minutes as it downloads the Java 21 base image and compiles the project inside Docker.

---

## 📦 Step 3: Running Locally (Without Docker)
If you prefer to run the service directly on your machine instead of Docker:

```bash
# Navigate to the service directory
cd foundational-utilities/distributed-id-generator/id-generator-service

# Clean, compile, and download dependencies
./mvnw clean compile

# Run the application (starts on port 8083)
./mvnw spring-boot:run
```

> [!TIP]
> You can override the Worker ID and Datacenter ID via environment variables:
> `WORKER_ID=5 DATACENTER_ID=2 ./mvnw spring-boot:run`

---

## 🧪 Step 4: How to Test (One-by-One)

### 1. Verify Infrastructure is Healthy
Run the following to check all containers are up:

```bash
docker compose ps
```

You should see 4 containers running:
| Container | Port | Purpose |
| :--- | :--- | :--- |
| `idgen-zookeeper` | `2181` | Cluster coordination |
| `idgen-zk-navigator` | `9000` | ZooKeeper Web UI |
| `idgen-node-1` | `8083` | ID Generator (Worker 1) |
| `idgen-node-2` | `8084` | ID Generator (Worker 2) |

---

### 2. Generate a Single ID
Fire a request to **Node 1** (Worker ID = 1):

```bash
curl -s http://localhost:8083/api/v1/ids/next
```

**Expected Output:** A 64-bit Long integer, e.g.:
```
264688560106377216
```

Now try **Node 2** (Worker ID = 2):

```bash
curl -s http://localhost:8084/api/v1/ids/next
```

> [!IMPORTANT]
> Notice the IDs from Node 1 and Node 2 are **different** even if generated at the same millisecond — that's because the Worker ID bits are different!

---

### 3. Generate Bulk IDs
Generate 10 IDs at once from Node 1:

```bash
curl -s http://localhost:8083/api/v1/ids/bulk?count=10 | python3 -m json.tool
```

**Expected Output:**
```json
[
    264688560349646848,
    264688560349646849,
    264688560349646850,
    264688560349646851,
    264688560349646852,
    264688560349646853,
    264688560349646854,
    264688560349646855,
    264688560349646856,
    264688560349646857
]
```

> [!TIP]
> The IDs are **monotonically increasing** within the same node. This is what makes them perfect for database primary keys — they maintain insertion order.

---

### 4. Verify Uniqueness Across Nodes
Run requests to both nodes simultaneously and compare:

```bash
echo "--- Node 1 ---"
curl -s http://localhost:8083/api/v1/ids/bulk?count=3
echo ""
echo "--- Node 2 ---"
curl -s http://localhost:8084/api/v1/ids/bulk?count=3
```

All 6 IDs should be globally unique with zero collisions.

---

### 5. Inspect ZooKeeper (Web UI)
Open the **ZooKeeper Navigator** in your browser:

🔗 [http://localhost:9000](http://localhost:9000)

When prompted for a connection string, enter:

```
idgen-zookeeper:2181
```

> [!NOTE]
> If running locally (not via Docker), use `localhost:2181` as the connection string instead.

From the Navigator UI you can:
- Browse the ZooKeeper node tree (`/zookeeper`, `/brokers`, etc.)
- Inspect ephemeral nodes that would hold Worker ID registrations in a production setup.
- Monitor cluster health and session data.

---

### 6. Run Unit Tests
Verify the core Snowflake logic (uniqueness, concurrency, monotonicity):

```bash
./mvnw test
```

**Expected Output:**
```
[INFO] Running com.architect.idgen.core.SnowflakeGeneratorTest
[INFO] Tests run: 3, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

---

## 📊 Step 5: Understanding the ID Structure

Each 64-bit ID can be decoded to see its components:

```
 0 | 0000000000 0000000000 0000000000 0000000000 0 | 0000000000 | 000000000000
   |                Timestamp (41b)                | Worker(10b)| Sequence(12b)
```

| Segment | Bits | Max Value | Purpose |
| :--- | :--- | :--- | :--- |
| Sign | 1 | — | Always 0 (positive number) |
| Timestamp | 41 | ~69 years | ms since Custom Epoch (April 2024) |
| Worker ID | 10 | 1,024 nodes | Unique server identifier |
| Sequence | 12 | 4,096/ms | Counter within same millisecond |

> [!TIP]
> **Throughput**: Each node can generate up to **4,096 IDs per millisecond** = **~4 million IDs/second**. With 1,024 nodes, the system supports **~4 billion IDs/second**.

---

## ⚙️ Step 6: Configuration Reference

All config is in `src/main/resources/application.yml`:

| Property | Default | Description |
| :--- | :--- | :--- |
| `server.port` | `8083` | HTTP port |
| `idgen.worker-id` | `1` | Worker ID (0–1023) |
| `idgen.datacenter-id` | `1` | Datacenter ID (reserved for multi-region) |
| `spring.threads.virtual.enabled` | `true` | Java 21 Virtual Threads for max concurrency |

Override via environment variables:
```bash
WORKER_ID=5 DATACENTER_ID=2 ./mvnw spring-boot:run
```

---

## 🧹 Cleanup
To stop everything and remove all containers/volumes:

```bash
docker compose down -v
```

To stop the locally running service:
```bash
# Press Ctrl+C in the terminal running mvnw, or:
kill $(lsof -t -i:8083)
```
