# CampusCross Multi-Wallet Project

A microservices-based multi-currency wallet application that allows users to manage multiple currencies with real-time foreign exchange rates.

**Deployment**: https://campus-cross-multi-wallet-project.vercel.app

## Overview

CampusCross is a modern multi-wallet application built with a microservices architecture, enabling users to create and manage multiple currency wallets with seamless currency conversion capabilities.

## Architecture

The project consists of three main components:

### 1. **Wallet Service** (Backend - Java/Spring Boot)
- Core wallet management functionality
- User authentication and authorization
- Transaction processing
- Multi-currency wallet operations
- RESTful API endpoints

### 2. **FX Service** (Backend - Java/Spring Boot)
- Foreign exchange rate management
- Real-time currency conversion
- Exchange rate APIs integration
- Currency pair management

### 3. **Wallet UI** (Frontend - TypeScript/React)
- Modern, responsive user interface
- Wallet dashboard
- Transaction history
- Currency conversion interface
- Real-time balance updates

## Tech Stack

### Backend
- **Language**: Java
- **Framework**: Spring Boot
- **Build Tool**: Maven
- **Database**: PostgreSQL(Supabase)

### Frontend
- **Language**: TypeScript
- **Framework**: React
- **Styling**: CSS, ShadCN
- **Build Tool**: Vite/Webpack

### DevOps
- **Containerization**: Docker
- **Deployment**: Render (Backend), Vercel (Frontend)

## Project Structure

```
CampusCross-Multi-Wallet-Project/
├── wallet-service/          # Wallet management microservice
│   ├── src/
│   ├── pom.xml/build.gradle
│   └── Dockerfile
├── fx-service/              # Foreign exchange microservice
│   ├── src/
│   ├── pom.xml/build.gradle
│   └── Dockerfile
├── wallet-ui/               # React frontend application
│   ├── src/
│   ├── package.json
│   └── Dockerfile
├── docker-compose.yml       # Docker orchestration
└── .github/workflows/       # CI/CD pipelines
```

## Prerequisites

Before running the application, ensure you have the following installed:

- **Docker** (version 20.x or higher)
- **Docker Compose** (version 2.x or higher)
- **Java JDK** 11 or higher (for local development)
- **Node.js** 16.x or higher (for local frontend development)
- **npm** (for frontend package management)

## Getting Started

**Clone the repository**
   ```
   git clone https://github.com/nashtarena/CampusCross-Multi-Wallet-Project.git
   cd CampusCross-Multi-Wallet-Project
   ```
#### Wallet Service
```
cd wallet-service
./mvnw spring-boot:run
```

#### FX Service
```
cd fx-service
./mvnw spring-boot:run
```

#### Wallet UI
```
cd wallet-ui
npm install
npm run dev
```

## 🔧 Configuration

### Environment Variables

#### Wallet Service
```
spring.application.name=wallet-service

# Database Configuration (Supabase Pooler)
spring.datasource.url=______
spring.datasource.username=_______
spring.datasource.password=_____________
spring.datasource.driver-class-name=org.postgresql.Driver
spring.datasource.hikari.data-source-properties.prepareThreshold=0

# HikariCP for Supabase (pooler mode)
spring.datasource.hikari.maximum-pool-size=3
spring.datasource.hikari.minimum-idle=1
spring.datasource.hikari.connection-timeout=20000
spring.datasource.hikari.idle-timeout=10000
spring.datasource.hikari.max-lifetime=20000
spring.datasource.hikari.pool-name=WalletServicePool

# MUST be true for Supabase pooler to release connections properly
spring.datasource.hikari.auto-commit=true

# Disable leak detection (Supabase pooler cannot handle this)
spring.datasource.hikari.leak-detection-threshold=0

# Faster validation
spring.datasource.hikari.validation-timeout=3000

# JPA Configuration
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
spring.jpa.properties.hibernate.format_sql=true

# JPA Write Optimization
spring.jpa.properties.hibernate.jdbc.batch_size=20
spring.jpa.properties.hibernate.order_inserts=true
spring.jpa.properties.hibernate.order_updates=true
spring.jpa.properties.hibernate.jdbc.batch_versioned_data=true

# Disable all caching (good for Supabase)
spring.jpa.properties.hibernate.cache.use_second_level_cache=false
spring.jpa.properties.hibernate.cache.use_query_cache=false

# Flyway
spring.flyway.enabled=false

# Server
server.port=8085

# JWT
jwt.secret=________________________
jwt.expiration=86400000

# Logging
logging.level.org.springframework.security=DEBUG
logging.level.com.campuscross.wallet=INFO
logging.level.com.campuscross.wallet.config=DEBUG
logging.level.org.springframework.web=INFO

# Tomcat
server.tomcat.threads.max=200
server.tomcat.threads.min-spare=10
server.tomcat.max-connections=10000
server.tomcat.accept-count=100

# Actuator
management.endpoints.web.exposure.include=health,metrics,info
management.endpoint.health.show-details=always
management.metrics.export.simple.enabled=true
```

#### FX Service
```
spring.application.name=fx-service
spring.cache.type=redis
# Connect to your local Docker containers
spring.data.redis.url=_____________________

spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration

#Exchange Rate API key
fx.api.key=_______________________

# Your PostgreSQL database connection settings
spring.datasource.url=________________________________________
spring.datasource.username=_________________________
spring.datasource.password=________________________________
spring.datasource.driver-class-name=org.postgresql.Driver

# Driver definition (fixes "Failed to determine a suitable driver class")


# JPA/Hibernate settings (to automatically create the 'rate_alerts' table)
# Set to 'update' to allow Hibernate to update the schema without dropping data
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true




# Sumsub Configuration
sumsub.api-url=https://api.sumsub.com
sumsub.app-token=______________________________
sumsub.secret-key=__________________________
sumsub.level-name=basic-kyc-level
sumsub.webhook.secret=_______________________

# Airwallex credentials
airwallex.api-url=https://api-demo.airwallex.com
airwallex.client-id=___________________________
airwallex.api-key=_________________________________________
airwallex.enabled=true
```

#### Wallet UI
```
VITE_API_BASE_URL=______________________________
VITE_WALLET_BASE_URL=______________________________
```

## Features

- Multi-currency wallet creation
- Real-time currency conversion
- Transaction history tracking
- User authentication and authorization
- Responsive UI design
- RESTful API architecture
- Microservices architecture
- Containerized deployment





---

**Note**: This is an educational project demonstrating microservices architecture and multi-currency wallet management.
