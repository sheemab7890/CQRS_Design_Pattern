# 📚 CQRS Concepts Explained

A deeper dive into the **Command Query Responsibility Segregation** pattern implemented in this project.

---

## Table of Contents

- [What is CQRS?](#what-is-cqrs)
- [Why use CQRS?](#why-use-cqrs)
- [When NOT to use CQRS](#when-not-to-use-cqrs)
- [Core Concepts](#core-concepts)
- [How This Implementation Works](#how-this-implementation-works)
- [Eventual Consistency](#eventual-consistency)
- [Common Patterns](#common-patterns)

---

## What is CQRS?

**CQRS** (Command Query Responsibility Segregation) is a pattern that separates **read operations** (queries) from **write operations** (commands) into different models.

### Traditional approach (single model):

```
          Controller
               │
               ▼
          Service Layer
               │
               ▼
       Single Data Model
               │
               ▼
          Database (1 table)
```

Every operation (read or write) goes through the same model and hits the same table.

### CQRS approach (separate models):

```
    Commands ──► CommandHandler ──► Write Model ──► products table
                       │
                       └──► Domain Event
                                │
                                ▼
    Queries ───► QueryHandler ◄── Read Model ◄── product_views table
```

Reads and writes use completely separate models optimized for their specific purpose.

---

## Why use CQRS?

### 1. **Performance — different optimization strategies**

**Write side** (commands):
- Normalized database schema
- Strong consistency
- Business rules and validation
- Optimized for transactional integrity

**Read side** (queries):
- Denormalized for fast reads
- Pre-computed fields (e.g., `stockStatus`)
- Multiple specialized read models for different use cases
- Can use different storage (e.g., Redis, Elasticsearch)

### 2. **Scalability — independent scaling**

```
                    ┌─ Command Service (write-heavy) × 2 instances
Load Balancer ──────┤
                    └─ Query Service (read-heavy) × 10 instances
```

Most systems are read-heavy (90% reads, 10% writes). CQRS lets you scale them independently.

### 3. **Security — precise access control**

```java
@PreAuthorize("hasRole('ADMIN')")
public String handle(CreateProductCommand cmd) { ... }   // Write

@PreAuthorize("hasRole('USER')")
public ProductSummary handle(GetProductByIdQuery q) { ... }  // Read
```

You can lock down writes to admins while allowing public read access.

### 4. **Flexibility — evolve models independently**

Change the read model without touching write logic:

```sql
-- Add a new denormalized field to the read model
ALTER TABLE product_views ADD COLUMN price_with_tax DECIMAL(10,2);

-- Update the projection listener
view.setPriceWithTax(calculateTax(event.getPrice()));
```

The write model stays untouched. The API stays untouched. Only the projection changes.

---

## When NOT to use CQRS

❌ **Simple CRUD applications** — If your app is just basic create/read/update/delete with no complex business logic, CQRS is overkill. Use a traditional service layer.

❌ **Tight consistency requirements** — If your reads MUST see writes instantly (e.g., financial transactions where you book a payment and immediately display the balance), eventual consistency won't work. You need strong consistency.

❌ **Small teams, simple domains** — CQRS adds complexity. If your team is 2-3 people building a simple app, the extra layers hurt more than they help.

✅ **Use CQRS when:**
- High read/write ratio (10:1 or more)
- Different performance requirements for reads vs writes
- Complex business logic on the write side
- Need multiple read models for different use cases
- Building event-sourced systems

---

## Core Concepts

### Commands

**Commands represent intent to change state.**

```java
CreateProductCommand
UpdateProductCommand
DeleteProductCommand
```

**Rules:**
- Named as imperative verbs ("Create", "Update", "Delete")
- Carry all data needed to perform the operation
- Validated before reaching the handler
- Return minimal data (just an ID or void)
- Can FAIL (business rule violations)

### Queries

**Queries represent intent to read data.**

```java
GetProductByIdQuery
GetAllProductsQuery
GetProductsByCategoryQuery
```

**Rules:**
- Named as questions ("Get", "Find", "Search")
- Carry only filter/sort/pagination parameters
- NEVER modify state
- Always succeed (or throw not found)
- Return DTOs, not entities

### Command Handlers

**Process commands and apply business logic.**

```java
@Transactional
public String handle(CreateProductCommand cmd) {
    // 1. Validate business rules
    if (isDuplicate(cmd)) throw new DuplicateProductException();
    
    // 2. Build aggregate
    Product product = Product.builder()...build();
    
    // 3. Persist to write DB
    Product saved = writeRepo.save(product);
    
    // 4. Publish domain event
    eventPublisher.publishEvent(new ProductCreatedEvent(...));
    
    return saved.getId();
}
```

**One handler per command.** Never mix.

### Query Handlers

**Fetch and return data from the read model.**

```java
@Transactional(readOnly = true)
public ProductSummary handle(GetProductByIdQuery query) {
    // 1. Fetch from read repository
    ProductView view = readRepo.findById(query.getProductId())
        .orElseThrow(() -> new ProductNotFoundException(...));
    
    // 2. Map to DTO
    return toSummary(view);
}
```

**One handler per query.** Never write.

### Domain Events

**Communicate changes between write and read sides.**

```java
ProductCreatedEvent
ProductUpdatedEvent
ProductDeletedEvent
```

**Rules:**
- Named in past tense (it already happened)
- Immutable (final fields)
- Published AFTER the write transaction commits
- Consumed by the projection listener to sync read model

### Projection

**The bridge that keeps the read model in sync.**

```java
@EventListener
@Transactional
public void on(ProductCreatedEvent event) {
    ProductView view = ProductView.builder()
        .id(event.getProductId())
        .name(event.getName())
        ...
        .build();
    
    readRepo.save(view);
}
```

**This is the ONLY place that writes to the read model.**

---

## How This Implementation Works

### Full flow example — creating a product

```
1. Client sends POST /api/products
   {
     "name": "Laptop",
     "category": "Electronics",
     "price": 999.99,
     "stock": 10
   }

2. ProductCommandController receives request
   - Validates via @Valid
   - Maps DTO → CreateProductCommand

3. CommandBus.dispatch(command)
   - Routes to CreateProductCommandHandler

4. CreateProductCommandHandler.handle(command)
   - Checks business rule: no duplicate name+category
   - Builds Product aggregate
   - Saves to products table (write model)
   - Publishes ProductCreatedEvent
   - Returns new product ID

5. ProductProjectionListener.on(ProductCreatedEvent)
   - Triggered by Spring's ApplicationEventPublisher
   - Builds ProductView from event data
   - Saves to product_views table (read model)

6. Client gets response
   {
     "success": true,
     "data": "abc-123",
     "message": "Product created successfully"
   }

7. Client queries GET /api/products/abc-123

8. ProductQueryController receives request
   - Builds GetProductByIdQuery

9. QueryBus.dispatch(query)
   - Routes to GetProductByIdQueryHandler

10. GetProductByIdQueryHandler.handle(query)
    - Fetches from product_views table (read model)
    - Maps ProductView → ProductSummary DTO
    - Returns to client

11. Client gets response
    {
      "success": true,
      "data": {
        "id": "abc-123",
        "name": "Laptop",
        "category": "Electronics",
        "price": 999.99,
        "stock": 10,
        "stockStatus": "IN STOCK",
        ...
      }
    }
```

---

## Eventual Consistency

### What is it?

The read model is **eventually consistent** with the write model — there's a tiny delay between:
1. Writing to `products` table
2. Event being published
3. Projection listener updating `product_views`

### In this demo:

The delay is **negligible** (< 1ms) because:
- Same JVM (synchronous Spring `ApplicationEventPublisher`)
- Same database (same transaction can update both tables if needed)

### In production:

If you use **Kafka** or **RabbitMQ** for events, the delay could be 10-500ms depending on network and load.

```
Write side ──► Kafka ──► Read side
              (async, 10-500ms delay)
```

### Is this a problem?

For most use cases, **no**. Examples where it's fine:
- E-commerce product catalogs
- Social media feeds
- Dashboards and analytics
- Any read-heavy system where freshness within 1 second is acceptable

Examples where it's NOT fine:
- Bank account balance (must be exact after a withdrawal)
- Inventory reservation (must lock stock instantly)
- Real-time bidding systems

For those cases, use strong consistency or don't use CQRS.

---

## Common Patterns

### Pattern 1: Multiple Read Models

One write model, many read models for different use cases:

```
               Write Model (products)
                       │
      ┌────────────────┼────────────────┐
      ▼                ▼                ▼
ProductListView   ProductDetailView   ProductSearchView
(id, name, price) (full data)        (indexed in Elasticsearch)
```

Each optimized for a specific query pattern.

### Pattern 2: Read Model in Different Storage

```
Write Model (PostgreSQL) ──► Event ──► Read Model (Redis)
                                    └─► Read Model (Elasticsearch)
```

PostgreSQL for writes (ACID), Redis for fast key lookups, Elasticsearch for full-text search.

### Pattern 3: Async Projection with Kafka

```
Command Handler ──► Write DB ──► Kafka Topic ──► Consumer ──► Read DB
```

Fully decoupled. Read side can be down and catch up later.

### Pattern 4: Snapshot + Event Sourcing

Combine CQRS with Event Sourcing:

```
Command ──► Event Stream (append-only log)
                │
                └──► Projection ──► Read Model
```

The write model is the event log itself. Projections rebuild the read model by replaying events.

---

## Production Considerations

### Idempotency

If a projection fails and the event is redelivered, it must be idempotent:

```java
@EventListener
@Transactional
public void on(ProductCreatedEvent event) {
    // Check if already processed
    if (readRepo.existsById(event.getProductId())) {
        log.warn("Duplicate event, skipping: {}", event.getProductId());
        return;
    }
    
    // Create view
    ProductView view = ...;
    readRepo.save(view);
}
```

### Monitoring

Track projection lag:

```java
event.setCreatedAt(LocalDateTime.now());  // Write side
view.setProjectedAt(LocalDateTime.now()); // Read side

long lagMs = Duration.between(event.getCreatedAt(), view.getProjectedAt()).toMillis();
metrics.recordLag("product_projection", lagMs);
```

If lag > 1 second, investigate.

### Error Handling

If a projection fails:
- Log the error
- Send to a dead-letter queue
- Retry with exponential backoff
- Alert if it stays failed for > 5 minutes

Never lose an event. The read model can be rebuilt by replaying all events.

---

## Further Reading

- Martin Fowler: [CQRS](https://martinfowler.com/bliki/CQRS.html)
- Greg Young: [CQRS Documents](https://cqrs.files.wordpress.com/2010/11/cqrs_documents.pdf)
- Microsoft: [CQRS Pattern](https://learn.microsoft.com/en-us/azure/architecture/patterns/cqrs)
- Udi Dahan: [Clarified CQRS](https://udidahan.com/2009/12/09/clarified-cqrs/)