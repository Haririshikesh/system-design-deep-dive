# Rate Limiter: System Design Deep Dive 🛡️

A Rate Limiter is a critical service used to control the rate of traffic sent by a client or a service. It protects APIs from being overwhelmed by too many requests (unintentional or malicious/DoS).

## 🏗️ 1. Requirements & Scope

Before designing, we must define the constraints of the system.

### ✅ Functional Requirements
* **Throttling:** Limit requests based on a unique identifier (e.g., User ID or IP Address).
* **Feedback:** Return a clear error message (`429 Too Many Requests`) when the limit is exceeded.
* **Accuracy:** The system must accurately track requests across multiple instances (Distributed).

### ⚙️ Non-Functional Requirements
* **Low Latency:** The rate limiter is in the critical path of every request. It must add < 5ms of overhead.
* **Fault Tolerance:** If the rate limiter service goes down, it should ideally "fail open" (allow traffic) rather than crashing the whole API.
* **Scalability:** Must support millions of users and thousands of requests per second.
* **Memory Efficiency:** Must store user limits efficiently to avoid massive infrastructure costs.

---

## 🗓️ Progress Tracker
- [x] Day 1: Requirements Gathering
- [ ] Day 2: High-Level Architecture & Component Flow
- [ ] Day 3: Algorithm Comparison (Token Bucket vs. Fixed Window)
- [ ] Day 4: Handling Distributed Systems (Redis & Race Conditions)
- [ ] Day 5: Proof of Concept Implementation (Code)