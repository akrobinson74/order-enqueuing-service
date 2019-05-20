# SERVICE_NAME = $(basename $PWD)
# DOCKERREPO := $(shell aws ecr --region eu-central-1 describe-repositories --repository-names $(SERVICE_NAME) --max-items 1 --query 'repositories[0].repositoryUri' --output text)
# VERSION ?= $(shell git describe --tags --always --abbrev=7 --dirty=-dirty-$(USER)-$(shell date -u +"%Y%m%dT%H%M%SZ"))
service-up-compose = docker-compose -f docker/base.yml

clean:
	./gradlew clean

build: clean
	./gradlew shadowJar

build-docker: ecr
	docker build -f docker/app/Dockerfile -t $(DOCKERREPO) .

build-local:
	docker build -f docker/app/Dockerfile -t latest_local .

build-local-and-run: build-local
	docker run -d --name oes_latest_local -p 9080:9080 latest_local

compose.service.up: build
	$(service-up-compose) build
	$(service-up-compose) up -d

compose.service.down:
	$(service-up-compose) down

# Authenticates docker client to registry
ecr:
	eval $(shell aws ecr get-login --region eu-central-1 --no-include-email)

# Tags and push "latest" image to ECS registry
release: build-docker
	docker tag $(DOCKERREPO):latest $(DOCKERREPO):$(VERSION)
	docker push $(DOCKERREPO):$(VERSION)
	docker push $(DOCKERREPO):latest

rollout-dev:
	cd terraform/states/dev && terraform init -upgrade && terraform apply -auto-approve -var 'version=$(VERSION)'

rollout-staging:
	cd terraform/states/staging && terraform init -upgrade && terraform apply -auto-approve -var 'version=$(VERSION)'

rollout-prod:
	cd terraform/states/prod && terraform init -upgrade && terraform apply -auto-approve -var 'version=$(VERSION)'
