# Order Enqueuing Service

## Description

This service receives, validates, stores and enqueues orders for subsequent
processing.  Simply put, it stores valid orders in an as yet undetermined
datasource and generates messages for a message queue whose where each event
will be consumed by instances of the **Order Processing Service**.

## Dev Environment Setup

To build, package, run and deploy this service the following software is required:
- Docker
- SDKMan
    - Java 8+
    - Kotlin 1.3.30+
    - Gradle 4.10.2+
- IntelliJ (necessary to make changes to the codebase)

_N.B. where 'X.y.z+' means greater than or equal to version X.y.z of said
software_

## Installation Guide

### Docker
We already assume you have successfully installed Docker on your local machine.
If you haven't done so, do so now.  If you can't get it working it's time to
consider new career options.

#### Mac OS


### SDKMAN!
SDKMAN! is a command-line utility that allows you to install and run mulitple
versions of Java, Kotlin, and Gradle (at a minimum).  Please follow the
installation instructions here: https://sdkman.io/install

```
$ curl -s "https://get.sdkman.io" | bash
$ source "/${YOUR_HOME_DIR}/.sdkman/bin/sdkman-init.sh"

# to verify installation
$ sdk version
```

#### Java 8+
This will install the OpenJDK equivalent of the LTS Java SE
(as of 5.6.2019 that is 11.0.3-zulu):
```
sdk install java
```

#### Kotlin 1.3.30+
```
# installs version 1.3.31 as of 5.6.2019
sdk install kotlin
```

#### Gradle 4.10.2+
```
# installs version 5.4.1 as of 5.6.2019
sdk install gradle
```

### IntelliJ

Go to https://www.jetbrains.com/idea/download/#section=mac and download the
appropriate binary for your platform and then install.

## How to run the application

### From the terminal/command-line

Go to the project's parent directory and run:
```
$ cd $PROJECT_ROOT
$ make compose.service.up
```

Conversely, ```$ make compose.service.down``` will shutdown the service and its
accompanying containers.

## Build and Deployment

Any commits pushed to the stage or master branch of this repository will trigger
the bitbucket pipeline which, if successful, will result in a docker image based
on the last changeset to this project being pushed to its Elastic Container
Registry (ECR) endpoint (and tagged 'latest' incidentally).

### Pipeline Steps
1. Code compilation
2. [Kotlin Lint](https://ktlint.github.io/) - style analysis of all kotlin source files; style errors
result in pipeline failure
3. [Detekt](https://github.com/arturbosch/detekt) - static analysis of code to detect code smells; a code smell of 10
results in a pipeline failure
4. Unit and Integration Test runs - again, a test failure triggers a pipeline
failure
5. Fatjar generated - a runnable .jar file with all dependencies is created
6. Docker build - the instruction in ./docker/app/Dockerfile are used to
generate a docker image.  The image is tagged 'latest'
7. Docker image push to AWS ECR
8. Deployment (still in progress).  In lieu of an this step, at the moment, a
shell script is run on each EC2 instance that pulls the latest image of the
project; terminates the currently running containers; and finally, starts a new
container based on the 'latest' image

### Deployment - Under Construction