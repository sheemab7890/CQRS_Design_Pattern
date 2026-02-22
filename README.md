# 🎯 CQRS Pattern — Spring Boot Demo

A production-ready implementation of the **Command Query Responsibility Segregation (CQRS)** pattern using Spring Boot, demonstrating clean separation between write and read operations.

---

## 📋 Table of Contents

- [What is CQRS?](#-what-is-cqrs)
- [Architecture](#-architecture)
- [Project Structure](#-project-structure)
- [Tech Stack](#-tech-stack)
- [Getting Started](#-getting-started)
- [API Reference](#-api-reference)
- [Key Concepts](#-key-concepts)
- [Design Decisions](#-design-decisions)
- [Testing the Flow](#-testing-the-flow)

---

## 💡 What is CQRS?

**CQRS** separates read and write operations into different models:

```
Traditional Approach:
    Client → Service → Single Model → Database

CQRS Approach:
    Commands → CommandHandler → Write Model → products table
                                     ↓ (event)
    Queries  → QueryHandler   → Read Model  ← product_views table
```

### Why CQRS?

- **Performance** — Optimize writes and reads independently
- **Scalability** — Scale read and write sides separately (10 read instances, 2 write instances)
- **Flexibility** — Change read model without touching write logic
- **Security** — Fine-grained access control per operation type

### When to use CQRS?

✅ High read-to-write ratio (90%+ reads)  
✅ Complex business logic on writes  
✅ Need multiple specialized read models  
✅ Performance bottlenecks on queries  

❌ Simple CRUD apps  
❌ Tight consistency requirements  
❌ Small teams, simple domains  

---

## 🏗 Architecture

```
┌─────────────────────────────────────────────────────────────────────────┐
│                            CLIENT REQUEST                               │
└────────────────────────────────┬────────────────────────────────────────┘
                                 │
                    ┌────────────┴────────────┐
                    │                         │
                    ▼                         ▼
        ┌──────────────────────┐  ┌──────────────────────┐
        │ Command Controller   │  │ Query Controller     │
        │ (POST/PUT/DELETE)    │  │ (GET)                │
        └──────────┬───────────┘  └──────────┬───────────┘
                   │                         │
                   ▼                         ▼
        ┌──────────────────────┐  ┌──────────────────────┐
        │ CommandBus           │  │ QueryBus             │
        └──────────┬───────────┘  └──────────┬───────────┘
                   │                         │
                   ▼                         ▼
        ┌──────────────────────┐  ┌──────────────────────┐
        │ Command Handlers     │  │ Query Handlers       │
        │ • CreateProduct      │  │ • GetProductById     │
        │ • UpdateProduct      │  │ • GetAllProducts     │
        │ • DeleteProduct      │  │ • GetByCategory      │
        └──────────┬───────────┘  └──────────┬───────────┘
                   │                         │
                   ▼                         ▼
        ┌──────────────────────┐  ┌──────────────────────┐
        │ Write Model          │  │ Read Model           │
        │ (Product)            │  │ (ProductView)        │
        └──────────┬───────────┘  └──────────▲───────────┘
                   │                         │
                   ▼                         │
        ┌──────────────────────┐             │
        │ products table       │             │
        │ (PostgreSQL)         │             │
        └──────────┬───────────┘             │
                   │                         │
                   │ Domain Event            │
                   └─────────────────────────┘
                              │
                              ▼
                   ┌──────────────────────┐
                   │ Projection Listener  │  ⭐ The Sync Bridge
                   └──────────┬───────────┘
                              │
                              ▼
                   ┌──────────────────────┐
                   │ product_views table  │
                   │ (PostgreSQL)         │
                   └──────────────────────┘
```

### Two Separate Tables

| Table | Purpose | Accessed By |
|---|---|---|
| `products` | Write model — normalized, has business logic | Command handlers only |
| `product_views` | Read model — denormalized, optimized for queries | Query handlers only |

They are kept in sync via **domain events** published by the write side and consumed by the **Projection Listener**.

---

## 📁 Project Structure

```
cqrs-demo/
├── src/main/java/com/example/cqrs/
│   │
│   ├── command/                          ← WRITE SIDE
│   │   ├── model/
│   │   │   ├── Product.java              (domain aggregate)
│   │   │   ├── CreateProductCommand.java
│   │   │   ├── UpdateProductCommand.java
│   │   │   └── DeleteProductCommand.java
│   │   ├── repository/
│   │   │   └── ProductWriteRepository.java
│   │   └── handler/
│   │       ├── CreateProductCommandHandler.java
│   │       ├── UpdateProductCommandHandler.java
│   │       └── DeleteProductCommandHandler.java
│   │
│   ├── query/                            ← READ SIDE
│   │   ├── model/
│   │   │   ├── ProductView.java          (read model)
│   │   │   ├── ProductSummary.java       (response DTO)
│   │   │   ├── GetProductByIdQuery.java
│   │   │   ├── GetAllProductsQuery.java
│   │   │   └── GetProductsByCategoryQuery.java
│   │   ├── repository/
│   │   │   └── ProductReadRepository.java
│   │   ├── handler/
│   │   │   ├── GetProductByIdQueryHandler.java
│   │   │   ├── GetAllProductsQueryHandler.java
│   │   │   └── GetProductsByCategoryQueryHandler.java
│   │   └── projection/
│   │       └── ProductProjectionListener.java  ⭐ (sync bridge)
│   │
│   ├── common/                           ← SHARED
│   │   ├── event/
│   │   │   ├── ProductCreatedEvent.java
│   │   │   ├── ProductUpdatedEvent.java
│   │   │   └── ProductDeletedEvent.java
│   │   └── exception/
│   │       ├── ProductNotFoundException.java
│   │       └── DuplicateProductException.java
│   │
│   ├── infrastructure/                   ← BUSES
│   │   └── bus/
│   │       ├── CommandBus.java
│   │       └── QueryBus.java
│   │
│   └── api/                              ← REST LAYER
│       ├── controller/
│       │   ├── ProductCommandController.java  (POST, PUT, DELETE)
│       │   └── ProductQueryController.java    (GET)
│       ├── dto/
│       │   ├── CreateProductRequest.java
│       │   ├── UpdateProductRequest.java
│       │   └── ApiResponse.java
│       └── exception/
│           └── GlobalExceptionHandler.java
│
├── docker-compose.yml
├── QUICKSTART.md             ← Run the project in 3 minutes
└── CQRS_CONCEPTS.md          ← Deep dive into CQRS pattern
```

---

## 🛠 Tech Stack

| Technology | Version | Purpose |
|---|---|---|
| Java | 17 | Language |
| Spring Boot | 3.2.1 | Application framework |
| Spring Data JPA | — | Database access layer |
| PostgreSQL | 15 | Relational database |
| Lombok | — | Boilerplate reduction |
| Maven | 3.8+ | Build tool |
| Docker Compose | — | Local infrastructure |

---

## 🚀 Getting Started

### Prerequisites

- Java 17+
- Maven 3.8+
- Docker & Docker Compose

### 1. Clone the repository

```bash
git clone https://github.com/your-username/cqrs-demo.git
cd cqrs-demo
```

### 2. Start PostgreSQL

```bash
docker-compose up -d
```

Wait ~10 seconds for PostgreSQL to be ready:

```bash
docker-compose ps
```

### 3. Run the application

```bash
mvn clean spring-boot:run
```

The app starts on **http://localhost:8080**

### 4. Verify it's running

```bash
curl http://localhost:8080/actuator/health
```

**Response:**
```json
{"status":"UP"}
```

---

## 📡 API Reference

### Command Endpoints (Write Operations)

| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/api/products` | Create a new product |
| `PUT` | `/api/products/{id}` | Update an existing product |
| `DELETE` | `/api/products/{id}` | Delete a product (soft delete) |

### Query Endpoints (Read Operations)

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/products/{id}` | Get a single product by ID |
| `GET` | `/api/products` | Get all products (paginated) |
| `GET` | `/api/products/category/{category}` | Get products by category |

---

## 📝 API Examples

### Create a product (COMMAND)

```bash
curl -X POST http://localhost:8080/api/products \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Laptop",
    "category": "Electronics",
    "price": 999.99,
    "stock": 10,
    "description": "High-performance laptop"
  }'
```

**Response:**
```json
{
  "success": true,
  "data": "a3f1c2d4-5678-90ab-cdef-1234567890ab",
  "message": "Product created successfully"
}
```

---

### Get a product (QUERY)

```bash
curl http://localhost:8080/api/products/a3f1c2d4-5678-90ab-cdef-1234567890ab
```

**Response:**
```json
{
  "success": true,
  "data": {
    "id": "a3f1c2d4-5678-90ab-cdef-1234567890ab",
    "name": "Laptop",
    "category": "Electronics",
    "price": 999.99,
    "stock": 10,
    "stockStatus": "IN STOCK",
    "description": "High-performance laptop",
    "createdAt": "2024-01-15T10:30:00",
    "lastUpdatedAt": "2024-01-15T10:30:00"
  }
}
```

---

### Get all products (QUERY with pagination)

```bash
curl "http://localhost:8080/api/products?page=0&size=10"
```

---

### Update a product (COMMAND)

```bash
curl -X PUT http://localhost:8080/api/products/a3f1c2d4-5678-90ab-cdef-1234567890ab \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Laptop Pro",
    "category": "Electronics",
    "price": 1299.99,
    "stock": 5,
    "description": "Professional-grade laptop"
  }'
```

**Response:**
```json
{
  "success": true,
  "data": null,
  "message": "Product updated successfully"
}
```

---

### Delete a product (COMMAND)

```bash
curl -X DELETE http://localhost:8080/api/products/a3f1c2d4-5678-90ab-cdef-1234567890ab
```

**Response:**
```json
{
  "success": true,
  "data": null,
  "message": "Product deleted successfully"
}
```

---

## 🔑 Key Concepts

### Commands vs Queries

| Aspect | Commands | Queries |
|---|---|---|
| **Purpose** | Change state | Read state |
| **Verb** | Imperative (Create, Update) | Question (Get, Find) |
| **Return** | Minimal (ID or void) | Full data (DTO) |
| **Side effects** | Yes — modifies database | No — read-only |
| **Can fail** | Yes — business rules | No — always succeeds or 404 |

### Domain Events

Events bridge the write and read sides:

```java
// Write side publishes
eventPublisher.publishEvent(new ProductCreatedEvent(...));

// Read side listens
@EventListener
public void on(ProductCreatedEvent event) {
    // Update read model
}
```

This keeps the two models **eventually consistent** without tight coupling.

### Three Separate Models

| Model | Purpose | Where |
|---|---|---|
| `Product` | Write model — has business logic | `products` table |
| `ProductView` | Read model — optimized for queries | `product_views` table |
| `ProductSummary` | Response DTO — what the client sees | API layer |

This separation means:
- Change the read model without touching write logic
- Change the API response without changing internal models
- Optimize each for its specific purpose

### Command/Query Bus Pattern

Controllers don't call handlers directly — they go through a bus:

```java
// Controller
commandBus.dispatch(createCommand);

// Bus routes to handler
createProductCommandHandler.handle(createCommand);
```

**Benefits:**
- Single place for cross-cutting concerns (logging, metrics, auth)
- Easy to mock in tests
- Controllers stay thin and focused

---

## 🎯 Design Decisions

### 1. Separate repositories for read and write

```java
ProductWriteRepository  // Used by command handlers only
ProductReadRepository   // Used by query handlers only
```

**Why?** Enforces the separation at compile time. A query handler physically cannot write to the products table.

### 2. One handler per command/query

```java
CreateProductCommandHandler  // Only handles CreateProductCommand
UpdateProductCommandHandler  // Only handles UpdateProductCommand
```

**Why?** Single Responsibility Principle. Each handler is independently testable and changeable.

### 3. Projection listener is the ONLY writer to read model

```java
@EventListener
public void on(ProductCreatedEvent event) {
    ProductView view = ...;
    readRepository.save(view);  // Only place this happens
}
```

**Why?** Single source of truth. No command handler, no query handler ever touches `product_views` directly.

### 4. Soft delete instead of hard delete

```java
public void deactivate() {
    this.active = false;  // Soft delete
}
```

**Why?** Preserves history. The read model can show "deleted" products in audit trails.

### 5. Optimistic locking on aggregates

```java
@Version
private Long version;
```

**Why?** Prevents lost updates when two concurrent requests try to modify the same product.

### 6. Validation at API layer

```java
@Valid @RequestBody CreateProductRequest request
```

**Why?** Commands stay domain-focused. The controller validates API contracts, the handler validates business rules.

---

## 🔄 Testing the Flow

### End-to-end flow when creating a product

```
1. POST /api/products
   ↓
2. ProductCommandController validates request
   ↓
3. CommandBus.dispatch(CreateProductCommand)
   ↓
4. CreateProductCommandHandler.handle()
   ├── Validates business rules (no duplicate)
   ├── Saves to products table (write model)
   └── Publishes ProductCreatedEvent
   ↓
5. ProductProjectionListener.on(ProductCreatedEvent)
   └── Saves to product_views table (read model)
   ↓
6. Client receives response with new product ID
   ↓
7. Client issues GET /api/products/{id}
   ↓
8. ProductQueryController builds query
   ↓
9. QueryBus.dispatch(GetProductByIdQuery)
   ↓
10. GetProductByIdQueryHandler.handle()
    └── Fetches from product_views table (read model)
    ↓
11. Client receives full product data
```

### Inspect the database

```bash
# Connect to PostgreSQL
docker exec -it cqrs-postgres psql -U postgres -d cqrs_db

# Check write model
SELECT * FROM products;

# Check read model
SELECT * FROM product_views;

# Exit
\q
```

Both tables should have the same data, but optimized for different purposes.

---

## 📚 Further Reading

| File | Description |
|---|---|
| [QUICKSTART.md](QUICKSTART.md) | Run the project in 3 minutes with curl examples |
| [CQRS_CONCEPTS.md](CQRS_CONCEPTS.md) | Deep dive: why CQRS, when to use it, eventual consistency, production patterns |
| Inline code comments | Every handler, model, and controller is heavily documented |

**External resources:**
- [Martin Fowler - CQRS](https://martinfowler.com/bliki/CQRS.html)
- [Microsoft - CQRS Pattern](https://learn.microsoft.com/en-us/azure/architecture/patterns/cqrs)
- [Greg Young - CQRS Documents](https://cqrs.files.wordpress.com/2010/11/cqrs_documents.pdf)

---

## 🧪 Running Tests

```bash
mvn test
```

_(Note: Full test suite not included in this demo — add unit tests for handlers and integration tests for the full flow)_

---

## 🐳 Docker Commands

```bash
# Start PostgreSQL
docker-compose up -d

# Check status
docker-compose ps

# View logs
docker-compose logs postgres

# Stop and remove containers
docker-compose down

# Stop and remove + delete data
docker-compose down -v
```

---

## 🤝 Contributing

Pull requests are welcome. For major changes, please open an issue first to discuss what you would like to change.

---

## 📝 License

MIT License — feel free to use this as a reference implementation.

---

## 🎓 What You'll Learn

By studying this codebase, you'll understand:

✅ How to separate read and write operations in a real application  
✅ How to use domain events to keep models in sync  
✅ How to implement the Command/Query Bus pattern  
✅ How to structure a Spring Boot app with clean architecture  
✅ How to handle eventual consistency  
✅ How to build REST APIs that follow CQRS principles  

This pattern is used in production at companies like Uber, Netflix, and Amazon for systems that need to scale reads and writes independently.

---

> **Note:** This is a simplified demo for learning purposes. In production, you would add:
> - Async event processing with Kafka or RabbitMQ
> - Separate read and write databases (e.g., PostgreSQL for writes, Redis for reads)
> - Circuit breakers and retry logic
> - Comprehensive test coverage
> - Monitoring and metrics
> - API versioning
> - Authentication and authorization
