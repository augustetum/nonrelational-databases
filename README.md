# nonrelational databases

A TaskRabbit-like service platform that connects clients with freelance service providers. The purpose was to build API endpoints for MongoDB.

## Overview

This REST API enables clients to find and book freelancers for various services (e.g., cleaning, plumbing, moving). Freelancers can register their services, set their rates, and manage bookings, while clients can search for services, make bookings, and leave reviews.

## Tech Stack

- Java 17
- Spring Boot 3.5.6
- MongoDB (via MongoDB Java Driver)
- Spring Security with JWT authentication
- Lombok
- Maven

## Core Features

### User Management
- Dual user roles: Clients and Freelancers
- JWT-based authentication and authorization
- User profiles with ratings

### Freelancer Services
- Freelancers can create and manage multiple workfields
- Each workfield includes category, description, and hourly rate
- Service categories for organization

### Booking System
- Clients can create bookings with freelancers
- Booking details include time, address, and service details
- Validation for booking conflicts and requirements

### Review System
- Clients and freelancers can review each other
- Reviews include timestamps and detailed feedback

## Project Structure

```
src/main/java/
├── entity/          # Domain models (User, Client, Freelancer, Booking, Workfield, Review)
├── repository/      # MongoDB data access layer
├── service/         # Business logic and validation
├── controller/      # REST API endpoints
├── dto/             # Data transfer objects
├── security/        # JWT authentication and security configuration
├── config/          # MongoDB configuration
└── util/            # Helper utilities
```

## Getting Started

### Prerequisites
- Java 17 or higher
- Maven
- MongoDB instance

### Running the Application

1. Configure MongoDB connection in `config/MongoDbContext.java`
2. Build the project:
   ```bash
   ./mvnw clean install
   ```
3. Run the application:
   ```bash
   ./mvnw spring-boot:run
   ```
