Backend application for managing products, inventory, and order processing built with Java Spring Boot.
The project demonstrates a real-world inventory workflow with JWT authentication, role-based authorization, transactional stock management, RESTful APIs, and Swagger documentation.
# Overview
The system provides APIs and a web interface for:
- User authentication and authorization
- Product management
- Inventory tracking
- Order creation and processing
- Stock reservation, deduction, and restoration using database transactions
The application supports two user roles:
- Admin: Manage products, Update inventory, Manage system data
- User: Register account, Browse products, Create and manage orders
# Key features
## Authentication & Authorization
- JWT-based authentication
- Secure API access with Bearer Token
- Role-based authorization (Admin, User)
- User registration and login
## Product Management
Admin can:
- Create products
- Update product information
- Delete products
- Check duplicated SKU
- Search products
- Pagination support
## Inventory Management
System supports:
- Inventory tracking
- Stock update
- Stock availability checking
- Transaction-safe stock operations
## Order Processing Workflow
The order system supports a complete lifecycle:
- Create order
- Confirm order
- Cancel order
- Delete order
## Transaction Management

# Running locally
## Requirements
- Java 21
- Maven
## Swagger Documentation
http://localhost:8080/swagger-ui.html
## Frontend pages
Login: http://localhost:8080/
Dashboard: http://localhost:8080/dashboard.html
Products: http://localhost:8080/products.html
Inventory: http://localhost:8080/inventory.html
Orders: http://localhost:8080/orders.html
## Database
H2 Console: http://localhost:8080/h2-console
Database is temporary and will reset after restarting the application.
## Default Accounts
Admin:
username: admin
password: admin123

User:
username: user
password: user123
## Docker Deployment
docker compose up --build
