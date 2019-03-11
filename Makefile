service-up-compose = docker-compose -f docker/base.yml

clean:
	./gradlew clean

build: clean
	./gradlew shadowJar

compose.service.up: build
	$(service-up-compose) build
	$(service-up-compose) up -d

compose.service.down:
	$(service-up-compose) down

