# Inventory Service

## Overview
The inventory service tracks product stock levels across warehouses.

## Database

Primary database: PostgreSQL 15 (managed RDS).
Read replicas: 2 read replicas in us-east-1 for read-heavy queries.
Cache: Redis for hot stock counts (TTL 60s).

## Schema

- `products(id, name, sku, price)`
- `stock(product_id, warehouse_id, quantity, updated_at)`
