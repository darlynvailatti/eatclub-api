# EatClub API

A Spring Boot REST API for managing restaurant availability and deals.

## Project Structure

```
eatclub-api/
├── src/
│   ├── main/
│   │   ├── java/com/eatclub/
│   │   │   ├── App.java                          # Main application entry point
│   │   │   ├── common/
│   │   │   │   └── Constants.java                # Application constants
│   │   │   ├── config/
│   │   │   │   └── WebConfig.java                # Web configuration
│   │   │   ├── controller/
│   │   │   │   └── RestaurantController.java     # REST API endpoints
│   │   │   ├── mapper/
│   │   │   │   ├── IRestaurantMapper.java        # Mapper interface
│   │   │   │   └── RestaurantMapper.java         # DTO mapping implementation
│   │   │   ├── model/
│   │   │   │   ├── Deal.java                     # Deal entity
│   │   │   │   ├── DealAtRestaurant.java         # Deal at restaurant entity
│   │   │   │   ├── PeakTimeWindow.java           # Peak time window entity
│   │   │   │   ├── Restaurant.java               # Restaurant entity
│   │   │   │   ├── dtos/                         # Data Transfer Objects
│   │   │   │   │   ├── AvailableRestaurantsDTO.java
│   │   │   │   │   ├── DealDTO.java
│   │   │   │   │   ├── ErrorDTO.java
│   │   │   │   │   └── PeakTimeDTO.java
│   │   │   │   └── ec/                           # External API DTOs
│   │   │   │       ├── DealDTO.java
│   │   │   │       ├── RestaurantDTO.java
│   │   │   │       └── RestaurantsDTO.java
│   │   │   ├── repository/
│   │   │   │   ├── ILocalRepository.java         # Repository interface
│   │   │   │   └── InMemoryRestaurantRepository.java  # In-memory implementation
│   │   │   └── service/
│   │   │       ├── IRestaurantService.java       # Service interface
│   │   │       └── RestaurantServiceImpl.java    # Service implementation
│   │   └── resources/
│   │       └── application.properties            # Application configuration
│   └── test/
│       └── java/com/eatclub/
│           ├── AppTest.java
│           ├── controller/
│           │   └── RestaurantControllerTest.java
│           ├── mapper/
│           │   └── RestaurantMapperTest.java
│           ├── repository/
│           │   └── InMemoryRestaurantRepositoryTest.java
│           └── service/
│               └── RestaurantServiceImplTest.java
└── pom.xml                                       # Maven configuration
```

## Prerequisites

- Java 25
- Maven 3.6+ (or use Maven Wrapper if available)

## Running the Application

### Using Maven

```bash
mvn spring-boot:run
```

### Using Java directly

First, build the application:

```bash
mvn clean package
```

Then run the JAR file:

```bash
java -jar target/eatclub-api-1.0-SNAPSHOT.jar
```

The application will start on the default Spring Boot port (usually `8080`). The API base path is `/api/v1`.

## Running Tests

### Run all tests

```bash
mvn test
```

### Run tests with verbose output

```bash
mvn test -X
```

### Run a specific test class

```bash
mvn test -Dtest=RestaurantControllerTest
```

### Run tests and generate coverage report

```bash
mvn clean test
```

Test reports are generated in `target/surefire-reports/` directory.

## API Endpoints

- `GET /api/v1/restaurants/available?timeOfDay=HH:mm` - Get available restaurants at a specific time
- `GET /api/v1/restaurants/peak-time` - Get peak time window information

## Health Check

The application includes Spring Boot Actuator for health monitoring:

- `GET /actuator/health` - Application health status
