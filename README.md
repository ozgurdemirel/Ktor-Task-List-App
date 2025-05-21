# Task List App with Ktor 🚀

This project is an educational example demonstrating how to develop a simple task list (to-do list) application with CRUD (Create, Read, Update, Delete) functionality using the [Ktor](https://ktor.io/) web framework. It combines the power of the Kotlin language and the flexibility of Ktor, designed for those looking to get started with building modern web applications or APIs. It also demonstrates how to manage database schema using [Liquibase](https://www.liquibase.org/) integration.

## 📝 Overview

This application allows users to add tasks, view existing tasks, update them, and delete them. Its main purpose is to explain the fundamental concepts of Ktor (Routing, Request/Response Handling, Content Negotiation, etc.) and how to structure a simple web application or RESTful API through a practical example. Database schema changes are managed using Liquibase.

## 🎯 Purpose and Target Audience

The main goals of this project are:

1.  **Introduction to Ktor:** To demonstrate the basic building blocks and usage of the Ktor framework.
2.  **Backend Development with Kotlin:** To showcase how the Kotlin language can be used for developing server-side applications.
3.  **RESTful API Design:** To teach how to design and implement basic API endpoints for simple CRUD operations.
4.  **Database Schema Management:** To show how to version and manage database changes alongside code using Liquibase.
5.  **Basic Web Application Flow:** To understand the process from receiving a web request to processing it and returning a response, specifically within Ktor.

## 🛠️ Technologies Used

The main technologies used in this project are:

* **[Kotlin](https://kotlinlang.org/):** The primary programming language used for developing the application.
* **[Ktor](https://ktor.io/):** An asynchronous and lightweight web framework.
* **[Gradle](https://gradle.org/):** The build automation tool used for dependency management and build processes.
* **[Liquibase](https://www.liquibase.org/):** An open-source tool used for managing and versioning database schema changes.
* **Database:** A database suited to the project's needs, such as **H2**, **PostgreSQL**, **MySQL**, etc.
* **Database Access:** A library/technology like **[Exposed](https://github.com/JetBrains/Exposed)** or **JDBC**.
* **JSON Serialization:** A library like **[kotlinx.serialization](https://github.com/Kotlin/kotlinx.serialization)** or **Jackson** for API usage.

## 💾 Database Management: Liquibase Integration

**Liquibase** is used in this project for the evolution and management of the database schema. Liquibase allows us to track database changes (table creation, adding columns, data updates, etc.) in version control systems (like Git), just like code.

**Why is it Effective?**

* **Version Control:** Database schema changes are versioned along with the codebase. It's clear who made which change and when.
* **Automation:** Database updates can be applied automatically during application startup or within CI/CD pipelines. This reduces errors caused by manual database updates.
* **Consistency:** Ensures the database schema is consistent across different environments (local machine, test server, production).
* **Rollback:** By defining rollback scripts for changes, it becomes easier to revert the database to a previous stable state in case of issues.
* **Team Collaboration:** Facilitates managing conflicts and incompatibilities when multiple developers work on the database.

**How is it Used (Example)?**

In Liquibase, changes are typically defined in `changelog` files (which can be in XML, YAML, JSON, or SQL format). A master `changelog` file (like `db.changelog-master.xml`) includes or references other specific change files (like `001-create-tasks-table.xml`, `002-add-status-column.sql`).

For instance, you might find a structure like this under `src/main/resources/db/changelog/`:

```db/changelog/
├── db.changelog-master.xml
├── changesets/
│   ├── 001-initial-schema.xml
│   └── 002-add-completed-flag.sql
```

When the application starts (or the relevant Gradle task is executed), Liquibase checks its tracking table in the target database and sequentially runs any `changesets` that have not yet been applied, bringing the database schema up to the current version.

## ✨ Features

* **Add Task:** Create new tasks.
* **List Tasks:** View all existing tasks.
* **Update Task:** Modify the content or status of an existing task.
* **Delete Task:** Remove a specific task from the list.

*(Depending on the application structure, it might offer an HTML interface or only API endpoints.)*

## 🚀 Setup and Running

You can follow these steps to run the project on your local machine:

1.  **Prerequisites:**
   * [Git](https://git-scm.com/)
   * [JDK (Java Development Kit)](https://adoptium.net/) (Version 11 or higher is usually recommended)
   * (If not using an in-memory DB like H2) The database used by the project must be installed and running. Database connection details are typically configured in `src/main/resources/application.conf`.

2.  **Clone the Project:**
    ```bash
    git clone [https://github.com/ozgurdemirel/Ktor-Task-List-App.git](https://github.com/ozgurdemirel/Ktor-Task-List-App.git)
    cd Ktor-Task-List-App
    ```

3.  **Build the Project:** (Using Gradle Wrapper)
   * Linux/macOS:
       ```bash
       ./gradlew build
       ```
   * Windows:
       ```bash
       gradlew.bat build
       ```

4.  **Prepare the Database (with Liquibase):**
    Usually, Liquibase applies the schema automatically when the application starts for the first time. However, there might be a Gradle task to run it manually (e.g., `./gradlew updateSQL` or `./gradlew update`). Check the project's specific documentation.

5.  **Run the Application:**
   * Linux/macOS:
       ```bash
       ./gradlew run
       ```
   * Windows:
       ```bash
       gradlew.bat run
       ```

6.  **Access:**
    The application will typically start on `http://localhost:8080` by default (The port number and database settings might be in `src/main/resources/application.conf`). You can access this address from your browser or an API testing tool (like Postman, Insomnia, etc.).

## 📂 Project Structure (Summary)

The basic directory structure of the project is generally as follows:

* `src/main/kotlin`: Contains the main Kotlin code for the application (Application entry point, Routing definitions, Data classes, Services, etc.).
* `src/main/resources`: Holds configuration files (`application.conf`), Liquibase `changelog` files (`db/changelog/`), static files (CSS, JS - if any), HTML templates (if used), etc.
* `build.gradle.kts` (or `build.gradle`): The Gradle file defining project dependencies, plugins (Ktor, Liquibase, etc.), and build settings.

## 🔌 API Endpoints

If the project primarily offers a REST API, likely endpoints might include:

* `GET /tasks`: Lists all tasks.
* `POST /tasks`: Adds a new task (Request body usually contains task details in JSON format).
* `GET /tasks/{id}`: Retrieves a task with a specific ID.
* `PUT /tasks/{id}`: Updates a task with a specific ID (Request body usually contains updated task details in JSON format).
* `DELETE /tasks/{id}`: Deletes a task with a specific ID.

*(It is recommended to check the code for the exact endpoints and request/response formats.)*
