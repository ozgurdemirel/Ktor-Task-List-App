# Makefile for Ktor Task List App

.PHONY: run test clean build

# Default target
all: build

# Run the application in development mode
run:
	./gradlew run -Pdevelopment=true

# Run all tests
test:
	./gradlew test

# Clean build artifacts
clean:
	./gradlew clean

# Build the application
build:
	./gradlew build

# Run the application with Docker Compose
docker-run:
	docker-compose up -d

# Stop Docker containers
docker-stop:
	docker-compose down

# Help command
help:
	@echo "Available targets:"
	@echo "  run         - Run the application in development mode"
	@echo "  test        - Run all tests"
	@echo "  clean       - Clean build artifacts"
	@echo "  build       - Build the application"
	@echo "  docker-run  - Run the application with Docker Compose"
	@echo "  docker-stop - Stop Docker containers" 