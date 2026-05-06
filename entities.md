# Entity Designs

## User
| Field     | Type          | Constraints            |
|-----------|---------------|------------------------|
| id        | Long          | PK, auto-increment     |
| name      | String        | NOT NULL               |
| email     | String        | NOT NULL, UNIQUE       |
| password  | String        | NOT NULL               |
| role      | Role          | NOT NULL, default USER |
| createdAt | LocalDateTime | NOT NULL               |

---

## Category
| Field       | Type   | Constraints        |
|-------------|--------|--------------------|
| id          | Long   | PK, auto-increment |
| name        | String | NOT NULL, UNIQUE   |
| description | String | nullable           |

---

## Product
| Field         | Type                 | Constraints        |
|---------------|----------------------|--------------------|
| id            | Long                 | PK, auto-increment |
| name          | String               | NOT NULL           |
| description   | String               | nullable           |
| price         | BigDecimal           | NOT NULL, min: 0   |
| imageUrl      | String               | nullable           |
| stockQuantity | Integer              | NOT NULL, min: 0   |
| createdAt     | LocalDateTime        | NOT NULL           |
| category      | ManyToOne → Category | NOT NULL           |

---

## CartItem
| Field    | Type                | Constraints        |
|----------|---------------------|--------------------|
| id       | Long                | PK, auto-increment |
| quantity | Integer             | NOT NULL, min: 1   |
| user     | ManyToOne → User    | NOT NULL           |
| product  | ManyToOne → Product | NOT NULL           |

> UNIQUE constraint on (user, product).

---

## Order
| Field       | Type                  | Constraints                 |
|-------------|----------------------|-----------------------------|
| id          | Long                  | PK, auto-increment          |
| status      | OrderStatus           | NOT NULL, default: PENDING  |
| totalAmount | BigDecimal            | NOT NULL, min: 0            |
| createdAt   | LocalDateTime         | NOT NULL                    |
| user        | ManyToOne → User      | NOT NULL                    |
| items       | OneToMany → OrderItem | cascade: ALL, orphanRemoval |

---

## OrderItem
| Field           | Type                | Constraints        |
|-----------------|---------------------|--------------------|
| id              | Long                | PK, auto-increment |
| quantity        | Integer             | NOT NULL, min: 1   |
| priceAtPurchase | BigDecimal          | NOT NULL, min: 0   |
| order           | ManyToOne → Order   | NOT NULL           |
| product         | ManyToOne → Product | NOT NULL           |

---

## Enums
| Enum        | Values                      |
|-------------|-----------------------------|
| Role        | ADMIN, USER                 |
| OrderStatus | PENDING, SHIPPED, DELIVERED |
