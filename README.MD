# ☕ Process Order Service – Documentation

## 1. 📄 Overview

**Process Order Service** manages the full lifecycle of customer orders in a coffee shop environment. It is responsible
for:

- Accepting new orders
- Assigning orders to shop-specific queues
- Tracking order statuses
- Coordinating queue assignments based on shop configurations
- Supporting serving of orders
- Returning real-time queue snapshots

It is implemented using **Spring Boot**, follows **Hexagonal Architecture**, and integrates with **Redis**, **PostgreSQL
**, and external services like **Menu** and **Shop**.

---
## 2. 🧪 Demo

This section helps you quickly **build, run, and test** the Process Order Service using the provided scripts.

---

### ▶️ Step 1: Run the Application with `./run.sh`

The `run.sh` script simplifies setup by building the app and spinning up all required services using Docker Compose.

#### 🔧 What It Does

- Builds the Spring Boot app with Maven
- Starts PostgreSQL, Redis, and the Process Order Service
- Applies Liquibase migrations
- Exposes the service at: `http://localhost:8082`

#### 🚀 Run the Script

```bash
chmod +x ./run.sh
./run.sh
```
### ▶️ Step 2: Demonstrate the API with `./test-api.sh`

Once the app is running, use `test-api.sh` to simulate end-to-end order flow.

#### 🧪 What It Tests

- Place an order
- Check order status
- View queue snapshot
- Serve the order
- Attempt to cancel the order

#### 🏃 Run the Script

```bash
chmod +x ./test-api.sh
./test-api.sh
```

---

## 3. 🧱 Architecture

The **Process Order Service** is built with a clean, modular foundation using **Hexagonal Architecture (Ports &
Adapters)**. It focuses on separation of concerns, testability, and future scalability.

---

### 🔹 Key Patterns

- **Hexagonal Architecture** (Ports & Adapters)
- **Redis-based distributed locking** for queue safety
- **Spring Data JPA** for persistence
- **Liquibase** for database versioning
- **Feign Clients** for inter-service communication

---

### ⚙️ Technologies Used

| Component         | Technology                                   |
|-------------------|----------------------------------------------|
| Language          | Java 17                                      |
| Framework         | Spring Boot 3.x                              |
| Database          | PostgreSQL (via Spring Data JPA)             |
| Caching & Locking | Redis + Redisson                             |
| Messaging         | Kafka (planned, event-driven future)         |
| Config & Secrets  | Spring Profiles, Docker Compose              |
| API Spec          | OpenAPI 3.0 (YAML-first)                     |
| API Docs UI       | Springdoc + Swagger UI                       |
| DB Versioning     | Liquibase                                    |
| Client Comm.      | Feign Client (Shop, Menu)                    |
| Testing           | JUnit 5, Mockito, Testcontainers, WireMock   |

---

### 🔁 Locking Strategy

- Queue assignment uses **Redis-based distributed lock** via Redisson
- Lock key: `lock:queue:{shopId}`
- Prevents race conditions and duplicate queue positions in high-concurrency environments

### 🔢 Queue Position Calculation

To ensure **consistency and immutability of queue positions**, each `Order` is assigned a unique `Long` ID generated
sequentially by the database.

- **Queue position** is calculated dynamically by **sorting all `WAITING` orders in a queue by `order.id`**
- This approach avoids problems like reordering after cancellations or manual shifting
- It enables **immutable assignment** (position at insert), and **live position calculation** for user-facing views

#### Advantages:

- Eliminates race conditions without needing to update other orders
- Naturally reflects order creation time
- Enables efficient sorting with indexed `order.id`

---

### 💡 Extensibility

- Queue assignment strategies are pluggable (e.g., MostAvailable, VIPPriority)
- Easily extendable to multi-shop, multi-tenant environments

---

## 4. 🌐 API Reference

### 🔹 Orders

- `POST /api/orders` – Create a new order
    - example request:
      ```http request
      POST http://localhost:8082/api/orders
      Content-Type: application/json
      ```

      ```json
      {
        "shopId": "123e4567-e89b-12d3-a456-426614174000",
        "items": [
          {
            "menuItemId": "123e4567-e89b-12d3-a456-426614174001",
            "quantity": 2
          },
          {
            "menuItemId": "123e4567-e89b-12d3-a456-426614174002",
            "quantity": 1
          }
        ]
      }
      ```

- `GET /api/orders/{orderId}` – Get order details
- `PATCH /api/orders/{orderId}` – Cancel an order
- `PATCH /api/orders/{orderId}/serve` – Mark an order as served
    - example request:
      ```http request
          PATCH http://localhost:8082/api/orders/12/serve
      ```

### 🔹 Queues

- `GET /api/shops/{shopId}/queue` – Get queue snapshot with live positions
    - example request:
      ```http request
      GET http://localhost:8082/api/shops/123e4567-e89b-12d3-a456-426614174000/queue
      Accept: application/json
      ```

---

## 5. 🧩 Domain Model

### 🔹 Entities

| Entity                  | Description                             |
|-------------------------|-----------------------------------------|
| `Order`                 | Represents a customer's order           |
| `OrderItem`             | Represents each item in an order        |
| `ShopConfiguration`     | Contains queue strategy & capacities    |
| `QueueAssignmentResult` | Result of assigning an order to a queue |
| `OrderStatus`           | Enum: `WAITING`, `SERVED`, `CANCELED`   |

---

## 6. 🗃️ Data Storage

### 🔹 PostgreSQL

Tables:

- `orders`
- `order_items`

Indexes:

- `orders.shop_id`
- Composite index: (`shop_id`, `status`) to increase query performance with frequent queries on these fields for getting
  all WAITING orders in a shop.

### 🔹 Redis

- **Distributed Lock** per shop queue: `lock:queue:{shopId}`
- **Cache**:
    - Queue snapshot: `shop-queue-snapshot::{shopId}`
    - Shop configurations: `shop-config::{shopId}`
    - Shop existence: `shop-existence::{shopId}`

---

## 7. 🚀 Future Considerations

### ✳️ Feature Enhancements

- Support for VIP and priority queues
- Custom queue assignment strategies per shop (e.g., based on item type)
- Support for multiple currencies and regional pricing:
    - Menu items priced per currency (e.g., VND, USD)
    - Customer preference or location determines pricing display
- Payment integration (optional or pre-order payment):
    - Support for digital wallets (e.g., Momo, ZaloPay, Apple Pay)
    - Integration with third-party payment gateways (e.g., Stripe, VNPay)
    - Order status updates automatically on payment success/failure

### 🔁 Idempotency

Implement `idempotencyKey` on `POST /api/orders` to prevent duplicate submissions:

- Accept key from client or generate server-side
- Store and reuse existing order if key matches

### 🧯 Circuit Breaker & Fallbacks

Use **Resilience4j** to isolate failures when Shop Service is down:

- Block or defer new orders for affected shops
- Use cached configs or fallback messages
- Alert operations via logging

### 🔐 Security

- **Currently**, the service bypasses security checks to simplify local development, as setting up a full OAuth 2.0
  authorization server takes time.
- Enforce role-based access (customer vs. operator)
- Extract `customerId` from token
- Validate ownership of `Order` and `Shop` on access

### 📲 Notifications

Emit push notifications when orders are created, served, or canceled for real-time UX updates.

### 📡 Event-Driven Architecture

Publish domain events like `OrderCreated`, `OrderServed`, `OrderCanceled` to support:

- Notification service
- Inventory updates
- Analytics tracking

### 📋 Queue Storage Alternative

Introduce a `shop_queue` table with **optimistic row-level locking** to track queue state:

- Eliminates reliance on Redis for queue safety
- Enables atomic updates per shop
