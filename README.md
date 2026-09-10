# Three-Tier Shop Application (Spring Boot + React + Supabase)

## Supabase Database Setup
1. Created a PostgreSQL database on Supabase.
2. Executed the `schema.sql` script in the Supabase SQL Editor to initialize `inventory` and `orders` tables and seed default products (`P100`, `P200`, `P300`).
3. Retained the IPv4 Session Pooler host (`aws-0-ap-northeast-2.pooler.supabase.com`) for Spring Boot connection pooling.
4. Set the database password locally using the `DB_PASSWORD` environment variable to ensure credentials are never committed to Git.

## Network Tab Evidence
### Confirmed Order Path (P100)
![Confirmed Order](screenshots/confirmed_order.png)

### Rejected Order Path (P300)
![Rejected Order](screenshots/rejected_order.png)

---

## Architectural Reflection

### 1. In-Process Integration vs. Separate Microservices
Integrating `OrderService` and `InventoryService` in-process within a single Spring Boot application provides low latency, direct Java method calls, compile-time type safety, and transactional consistency out of the box using `@Transactional`. Transactions execute atomically across both order writing and stock updates within the same database connection. 

If split into separate microservices over HTTP/gRPC, we lose ACID transactions, necessitating distributed transaction patterns like Sagas or Two-Phase Commit (2PC) along with eventual consistency models. Furthermore, network latency, serialization overhead, API gateways, circuit breakers (e.g., Resilience4j), service discovery, and retry mechanisms would need to be added to manage remote network failures and service unreliability.

### 2. Importance of Package-Private Visibility on `InventoryServiceImpl`
Enforcing package-private visibility on `InventoryServiceImpl` guarantees strict module boundaries at compile time. The `shop` package is forced to depend exclusively on the `InventoryService` interface exposed by the `inventory` module.

If `InventoryServiceImpl` were made `public`, developer code in the `shop` module could directly instantiate or cast to the implementation class. This leaks internal operational details, bypasses interface abstraction, creates tight coupling, and invalidates the modular monolith boundary—making future refactoring or service extraction significantly harder.

### 3. Extracting Inventory into its Own Microservice
Extracting `Inventory` into an independent microservice would be justified when inventory management requires independent scaling (e.g., high-frequency stock reads during sales), dedicated database isolation, or autonomous deployment lifecycles by a separate engineering team.

To accomplish this:
* **Code Adjustments:** Replace direct in-process calls to `InventoryService` inside `OrderService` with an HTTP client (`RestTemplate`, `WebClient`, or `FeignClient`) or an asynchronous message broker (RabbitMQ/Kafka).
* **Database Adjustments:** Split the database into two isolated storage instances—one for Orders and one for Inventory.
* **Resilience Adjustments:** Introduce fallback mechanisms, timeout configurations, and circuit breakers when calling the external Inventory REST API to handle service downtime gracefully.