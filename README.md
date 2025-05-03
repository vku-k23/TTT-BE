# CineVibe - Backend Application

CineVibe is a social movie platform that allows users to discover, review, and share their movie experiences with friends. This repository contains the backend API for the CineVibe application.

## 📋 Overview

CineVibe backend provides a comprehensive REST API for the mobile application, enabling functionalities such as:

- User authentication via Firebase
- Movie reviews and ratings
- Social connections (follow/friend system)
- Watchlist creation and management
- Comments on reviews
- User preferences and favorite genres

## 🛠️ Technology Stack

- **Framework**: Spring Boot
- **Language**: Java 17
- **Database**: MySQL
- **Authentication**: Firebase Authentication
- **Database Migration**: Flyway
- **Documentation**: SpringDoc (OpenAPI/Swagger)
- **Containerization**: Docker
- **CI/CD**: Jenkins
- **Security**: Spring Security

## 🚀 Getting Started

### Prerequisites

- JDK 17+
- MySQL 8.0+
- Docker and Docker Compose (for containerized deployment)
- Firebase project (for authentication)

### Local Development Setup

1. **Clone the repository**:
   ```bash
   git clone https://github.com/vku-k23/cinevibe-be.git
   cd cinevibe-be
   ```

2. **Configure Firebase**:
   - Place your `firebase-service-account.json` in the `src/main/resources` directory
   - Or use the development profile which disables Firebase authentication

3. **Database Configuration**:
   - The default configuration connects to a MySQL database named `cinevibe`
   - Update `application.yml` or use environment variables to customize the database connection

4. **Build and Run**:
   ```bash
   ./mvnw clean install
   ./mvnw spring-boot:run
   ```
   
   For development profile (disables Firebase auth):
   ```bash
   ./mvnw spring-boot:run -Dspring.profiles.active=dev
   ```

5. **Access the API Documentation**:
   - Swagger UI: http://localhost:8081/swagger-ui.html
   - OpenAPI JSON: http://localhost:8081/v3/api-docs

### Docker Deployment

1. **Build the Docker image**:
   ```bash
   docker build -t cinevibe:latest .
   ```

2. **Run with Docker Compose**:
   ```bash
   docker-compose up -d
   ```

## 📚 API Documentation

The API documentation is available via Swagger UI when the application is running. Key API endpoints include:

- `/api/auth/*` - Authentication endpoints
- `/api/user/*` - User management
- `/api/connections/*` - Social connections (follow/friend system)
- `/api/reviews/*` - Movie reviews
- `/api/comments/*` - Comments on reviews
- `/api/watchlists/*` - User watchlists
- `/api/info/*` - Application information

## 📦 Database Migration

The application uses Flyway for database migrations. Migration scripts are located in `src/main/resources/db/migration` and are automatically executed on application startup.

## 🔒 Security

- The application uses Firebase Authentication for user authentication
- API endpoints are secured using Spring Security
- CORS is configured to allow requests from any origin for development purposes

## 🚢 CI/CD Pipeline

The repository includes a Jenkins pipeline configuration in `Jenkinsfile` for automated building, testing, and deployment:

- Builds the application with Maven
- Runs tests
- Builds and pushes Docker images
- Deploys to the target environment

## 👥 Contributing

For information about how to contribute to this project, please contact the project maintainers.

## 📜 License

This project is licensed under the Apache License 2.0.

## 📞 Contact

- **Developer**: Nguyen Quoc Viet
- **Email**: vietnq23ceb@vku.udn.vn
- **GitHub**: [https://github.com/vku-k23/cinevibe-be](https://github.com/vku-k23/cinevibe-be)