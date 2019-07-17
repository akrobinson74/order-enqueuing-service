SERVICE_NAME = order-enqueuing-service
VERSION ?= $(shell git describe --tags --always --abbrev=7 --dirty=-dirty-$(USER)-$(shell date -u +"%Y%m%dT%H%M%SZ"))
DOCKERREPO := $(shell aws ecr --region eu-central-1 describe-repositories --repository-names $(SERVICE_NAME) --max-items 1 --query 'repositories[0].repositoryUri' --output text)
service-up-compose = docker-compose -f docker/base.yml

clean:
	./gradlew clean

build: clean
	./gradlew shadowJar

build-docker: ecr
	echo "DOCKERREPO: $(DOCKERREPO)"
	docker build -f docker/app/Dockerfile -t $(DOCKERREPO) ./docker/app

build-local:
	docker build -f docker/app/Dockerfile -t latest_local ./docker/app

build-local-and-run: build-local
	docker run -d --name oes_latest_local -p 9080:9080 latest_local

compose.deps.down:
	$(service-up-compose) down
	docker rm $(docker stop localmongo1 elasticmq) || true

compose.deps.up: build
	$(service-up-compose) build
	$(service-up-compose) up elasticmq mongo1

compose.service.up: build
	docker rm $(docker stop localmongo1 elasticmq) || true
	$(service-up-compose) build
	$(service-up-compose) up -d

compose.service.down:
	$(service-up-compose) down

# Authenticates docker client to registry
ecr:
	eval $(shell aws ecr get-login --region eu-central-1 --no-include-email)

# Tags and push "stage" image to ECS registry
release: build-docker
	docker tag $(DOCKERREPO):latest $(DOCKERREPO):$(VERSION)
	docker tag $(DOCKERREPO):latest $(DOCKERREPO):stage
	docker push $(DOCKERREPO):$(VERSION)
	docker push $(DOCKERREPO):stage

rollout-dev:
	cd terraform/states/dev && terraform init -upgrade && terraform apply -auto-approve -var 'version=$(VERSION)'

rollout-staging:
	cd terraform/states/staging && terraform init -upgrade && terraform apply -auto-approve -var 'version=$(VERSION)'

rollout-prod:
	cd terraform/states/prod && terraform init -upgrade && terraform apply -auto-approve -var 'version=$(VERSION)'
