.PHONY: run test clean build

all: build

run:
	./gradlew run -Pdevelopment=true

test:
	./gradlew test

clean:
	./gradlew clean

build:
	./gradlew build

docker-run:
	docker-compose up -d

docker-stop:
	docker-compose down

help:
	@echo "Available targets:"
	@echo "  run         - Run the application in development mode"
	@echo "  test        - Run all tests"
	@echo "  clean       - Clean build artifacts"
	@echo "  build       - Build the application"
	@echo "  docker-run  - Run the application with Docker Compose"
	@echo "  docker-stop - Stop Docker containers" 