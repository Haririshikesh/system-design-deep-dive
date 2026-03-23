# Rate Limiter: System Design Deep Dive 🛡️

A Rate Limiter is a critical service used to control the rate of traffic sent by a client or a service. It protects APIs from being overwhelmed by too many requests (unintentional or malicious/DoS).

## 🏗️ 1. Requirements & Scope

Before designing, we must define the constraints of the system.

### ✅ Functional Requirements
* **Throttling:** Limit requests based on a unique identifier (e.g., User ID or IP Address).
* **Custom Thresholds:** Allow different users to have different rate limits.
* **Feedback:** Return a clear error message (`429 Too Many Requests`) when the limit is exceeded, including a Retry-After header.
* **Distributed Support:** The system must accurately track requests across a distributed cluster of servers.

### ⚙️ Non-Functional Requirements
* **Ultra-Low Latency:** The rate limiter is in the critical path of every request. It must add < 2ms of overhead.
* **High Availability:** If the rate limiter service fails, it should "fail open" (allow traffic) to ensure the core API remains reachable.
* **Fault Tolerance:** The system should not have a single point of failure.
* **Scalability:** Must support millions of users and thousands of requests per second.
* **Memory Efficiency:** Must store user limits efficiently to avoid massive infrastructure costs.


## 🏗️ 2. Design Constraints

* **Users:** 10 million daily active users.
* **Request Data:** If we store a User_ID (8 bytes) and a Counter (4 bytes) in Redis, that is 12 bytes per user.
* **Total Memory:** $10M \times 12 \text{ bytes} \approx 120\text{ MB}$. This fits easily into a single Redis node, but we will use clustering for high availability.
---

## 🗓️ Progress Tracker
- [x] Day 1: Requirements Gathering
- [ ] Day 2: High-Level Architecture & Component Flow
- [ ] Day 3: Algorithm Comparison (Token Bucket vs. Fixed Window)
- [ ] Day 4: Handling Distributed Systems (Redis & Race Conditions)
- [ ] Day 5: Proof of Concept Implementation (Code)