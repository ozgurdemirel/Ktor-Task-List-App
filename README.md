# Ktor Task Application

A task management web application built with Ktor, Kotlin, and Exposed SQL framework, following Hexagonal Architecture principles. This application allows users to create, view, edit, and delete tasks through a web interface.

## Features

* **Task Management**: Full CRUD (Create, Read, Update, Delete) operations for tasks
* **Rich Task Data**: Tasks include title, description, optional long-form description, and completion status
* **Web Interface**: Clean UI built with Thymeleaf templates, Fomantic UI, and Alpine.js for interactivity
* **Pagination**: Browse tasks efficiently with pagination support on the main task list page
* **Database Integration**: Utilizes Exposed ORM for database operations. Supports H2 for development/testing and PostgreSQL for production. Uses HikariCP for connection pooling
* **Database Migrations**: Automatic database schema management using Liquibase
* **Input Validation**: Server-side validation for task creation and updates
* **Hexagonal Architecture**: Code structured with distinct adapters (web, templates), application core (services), and domain/infrastructure (repository, models, DTOs)
* **Testing**: Includes unit tests (Service, Repository) and integration tests (Routes, Application)

## Tech Stack

| Technology            | Purpose                                            |
|:----------------------|:---------------------------------------------------|
| **Framework**         |                                                    |
| Ktor                  | Kotlin asynchronous web framework                  |
| **Language**          |                                                    |
| Kotlin                | Primary programming language                        |
| **Database**          |                                                    |
| Exposed               | Kotlin SQL framework/ORM                           |
| H2 Database           | In-memory database for development/testing         |
| PostgreSQL            | Relational database for production                 |
| HikariCP              | High-performance JDBC connection pool              |
| Liquibase             | Database schema version control                    |
| **Templating**        |                                                    |
| Thymeleaf             | Server-side Java/Kotlin template engine            |
| Thymeleaf Layout Dialect | For creating reusable layout templates          |
| **Serialization**     |                                                    |
| kotlinx.serialization | Kotlin multiplatform / JSON serialization          |
| **Frontend**          |                                                    |
| Fomantic UI           | CSS Framework (fork of Semantic UI)                |
| Alpine.js             | Minimalist JavaScript framework for interactivity  |
| jQuery                | JavaScript library (required by Fomantic UI)       |
| **Build & Testing**   |                                                    |
| Gradle                | Build automation tool                              |
| MockK                 | Mocking library for Kotlin                         |
| Ktor Testing          | Utilities for testing Ktor applications            |
| JSoup                 | Java HTML Parser (for testing HTML content)        |
| **Logging**           |                                                    |
| Logback               | Logging framework                                  |
| **Containerization**  |                                                    |
| Docker                | Containerization platform                          |
| Docker Compose        | Tool for defining and running multi-container apps |

## Project Structure

```
src/
├── main/
│   ├── kotlin/         # Application source code
│   │   ├── adapters/   # Hexagonal architecture adapters
│   │   ├── application/ # Application services
│   │   ├── domain/     # Domain models and interfaces
│   │   ├── infrastructure/ # Infrastructure implementations
│   │   └── Application.kt # Main entry point
│   └── resources/
│       ├── application.conf # Ktor configuration
│       ├── db/
│       │   └── changelog/ # Liquibase migration files
│       ├── logback.xml   # Logging configuration
│       └── templates/    # Thymeleaf HTML templates
└── test/                # Test source code
```

## Getting Started

### Prerequisites

* JDK 17 or higher
* Gradle 8.4 or higher
* PostgreSQL (for production deployment)

### Development Setup

1. Clone the repository:

2. Run the application using the Gradle wrapper with the embedded H2 database:
   ```bash
   ./gradlew run
   ```

3. The application will be available at `http://localhost:8080/tasks`

### Production Setup

1. Ensure you have a PostgreSQL database running and configured.

2. Set the required environment variables:
   ```bash
   DATABASE_ENVIRONMENT=prod
   DATABASE_JDBC_URL=jdbc:postgresql://[host]:[port]/[database]
   DATABASE_USER=[username]
   DATABASE_PASSWORD=[password]
   ```

3. Build and run the application:
   ```bash
   ./gradlew buildFatJar
   java -jar build/libs/*-fat.jar
   ```

### Docker Setup

The application includes full Docker support for easy deployment:

1. **Build and run with Docker Compose:**
   ```bash
   docker-compose up --build
   ```

   This will:
   - Build the application container
   - Start a PostgreSQL database container
   - Configure all necessary environment variables
   - Make the application available at `http://localhost:8080`

2. **Environment Variables**: The Docker configuration uses these default values:
   - Database name: `ozgurclub`
   - Database user: `ozgurclub`
   - Database password: `password`

   Modify the `docker-compose.yml` file to change these values if needed.

## Database Migrations (Liquibase)

Database schema changes are managed using Liquibase:

* **Automatic Migrations**: Applied automatically on application startup
* **Changelog Files**: Located in `src/main/resources/db/changelog/`

## Web Endpoints

| Path                  | Method | Description                           | Template    |
|:----------------------|:-------|:--------------------------------------|:------------|
| `/`                   | GET    | Redirects to `/tasks`                 | N/A         |
| `/tasks`              | GET    | List all tasks (paginated)            | `index.html` |
| `/tasks/create`       | GET    | Show the form to create a new task    | `create.html` |
| `/tasks`              | POST   | Submit the new task form              | (Redirects) |
| `/tasks/{id}`         | GET    | Show details of a specific task       | `show.html` |
| `/tasks/{id}/edit`    | GET    | Show the form to edit a task          | `edit.html` |
| `/tasks/{id}/edit`    | POST   | Submit the task edit form             | (Redirects) |
| `/tasks/{id}/toggle`  | POST   | Toggle the completion status of a task| (Redirects) |
| `/tasks/{id}/delete`  | POST   | Delete a specific task                | (Redirects) |

## Building & Running (Gradle Tasks)

| Task                 | Description                                      |
|:---------------------|:-------------------------------------------------|
| `./gradlew run`      | Run the application (uses development settings)  |
| `./gradlew build`    | Build the project and run tests                  |
| `./gradlew test`     | Run unit and integration tests                   |
| `./gradlew buildFatJar` | Build a fat JAR with all dependencies         |

## License

This project is licensed under the Apache License - see the LICENSE file for details.

