# URL Shortener (TinyURL) System Design 🔗

## 🏗️ Phase 1: Clarify Requirements

### ✅ Functional Requirements (FR)
* **Shortening:** Given a long URL, return a unique 7-character alias.
* **Redirection:** Clicking the short link redirects to the original long URL (HTTP 301/302).
* **Custom Aliases:** Users can specify a name (e.g., `bit.ly/my-portfolio`).
* **Expiration:** Links should expire after 2 years by default.

### ⚙️ Non-Functional Requirements (NFR)
* **High Availability:** Redirection must never fail (99.999% uptime).
* **Low Latency:** Redirection must happen in < 50ms.
* **Scalability:** Must support 100M new URLs/month and 10B clicks/month (Read-Heavy 100:1 ratio).
* **Uniqueness:** No two long URLs should produce the same short code.


## 🧮 2. Estimation

To design the right storage and caching layer, we must calculate the scale.

### Traffic Estimates
* **Write Volume:** 100 Million new URLs/month.
* **Read/Write Ratio:** 100:1 (Read-heavy).
* **Read QPS (Queries Per Second):** $$(100M \times 100 \text{ reads}) / (30 \text{ days} \times 86,400 \text{ sec}) \approx 4,000 \text{ RPS}$$
* **Write QPS:** $\approx 40 \text{ RPS}$

### Storage Estimates (5-Year Projection)
* **Total URLs:** $100M \times 12 \text{ months} \times 5 \text{ years} = 6 \text{ Billion URLs}$.
* **Average Record Size:** 500 bytes (URL + Hash + Metadata).
* **Total Storage Required:** $6B \times 500 \text{ bytes} \approx 3 \text{ Terabytes}$.

### Cache Estimates
* **Rule:** Cache 20% of the daily traffic (Pareto Principle).
* **Daily Requests:** $4,000 \text{ RPS} \times 86,400 \text{ sec} \approx 345 \text{ Million requests/day}$.
* **Cache Size:** $0.2 \times 345M \times 500 \text{ bytes} \approx 34 \text{ GB of RAM}$ for caching.

## 🏗️ 3. High-Level Design (HLD)

The system is designed to handle a read-heavy workload by decoupling the **Write Path** (URL Generation) from the **Read Path** (Redirection). 

![System Architecture](./assets/architecture-v1.svg)

### 🧱 Component Breakdown

* **Load Balancer (Nginx/AWS ELB):** Acts as the entry point. It distributes incoming traffic to multiple web servers using a *Least Connections* strategy to ensure no single server is overwhelmed during traffic spikes.
* **Web Servers (API Tier):** Stateless servers that handle the core logic. 
    * **Write:** Grabs a pre-allocated key from the KGS, stores the mapping, and updates the cache.
    * **Read:** Queries the cache first, falling back to the DB only on a cache miss.
* **Key Generation Service (KGS):** A dedicated microservice that pre-generates unique 7-character keys (Base62). 
    * *The "Why":* This eliminates runtime collisions and prevents the database from becoming a bottleneck during URL creation.
* **Cache (Redis):** Stores the most frequently accessed `short_url -> long_url` mappings. Based on the **Pareto Principle (80/20 rule)**, caching 20% of traffic can handle 80% of our requests, keeping redirection latency under 20ms.
* **Database (NoSQL/SQL):** The source of truth. At this scale, we optimize for high-availability and horizontal scaling. (See Phase 4 for the DB selection deep dive).

---

## 🗓️ Progress
- [x] Step 1: Requirements Gathering
- [x] Step 2: Traffic & Storage Estimation
- [x] Step 3: High-Level Design & Excalidraw Architecture
- [ ] Step 4: Database Schema & Deep Dive (SQL vs NoSQL)
- [ ] Step 5: Proof of Concept Implementation (Python/Go)