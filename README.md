# ieškok - an application for everyday tasks

This is a project made for connecting clients, who need help with maintenance or everyday tasks (plumbing, cleaning, assembling furniture, etc.). The application is strongly influenced by <a href=https://www.taskrabbit.com/>Taskrabbit</a>. 

The purpose of this project is to learn 4 non-relational databases - MongoDB, Cassandra, Redis and Neo4j. This project incorporates all of them.

## Overview

The REST API enables these features:
- Clients can find and book freelancers for various services (e.g., cleaning, plumbing, moving).
- Freelancers can register their services, set their rates, and manage bookings
- Clients can search for services, make bookings, and leave reviews.
- Clients can request a leaderboard of the freelancers.
- Clients can get recommendations based on their previous bookings.

## Tech Stack

This project <b>does not</b> use Spring Data. This choice was made for the purpose of actually learning the databases.

The rest of the tech stack is:
- Java 17
- Spring Boot 3.5.6
- MongoDB
- Redis
- Cassandra
- Neo4j
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
- Freelancers are stored in a leaderboard

### Chat System
- Clients and Freelancers can chat with each other (TCP)

### Review System
- Clients are recommended new workfields based on recent bookings

## Project Structure

```
src/main/java/
├── entity/          #Domain models (User, Client, Freelancer, Booking, Workfield, WorkfieldCategory, Review, Conversation, Event)
├── repository/      #Data access layer
├── service/         #Business logic and validation
├── controller/      #REST API endpoints
├── dto/             #Data transfer objects
├── security/        #JWT authentication and security configuration
├── config/          #MongoDB, Redis, Neo4J, Cassandra configuration
└── util/            #Helper utilities, mappers
```

## Getting Started

### Prerequisites
- Java 17 or higher
- Maven
- Docker (optional)
- Redis 
- Cassandra 
- Neo4J 
- MongoDB 

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
