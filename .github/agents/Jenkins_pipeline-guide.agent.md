# 🤖 Jenkins Pipeline Agent — Markdown Generator

> **Purpose:** Use this markdown agent as a blueprint to generate all necessary files for an automated Jenkins CI/CD pipeline for any Spring Boot + Maven + Docker project.

---

## 📋 HOW TO USE THIS AGENT

1. **Read** each section below — it describes a file you need to create
2. **Copy** the code block content into the specified file path
3. **Customize** placeholders marked with `{{PLACEHOLDER}}` for your project
4. **Commit** all files to your repository

---

## 🔧 PLACEHOLDERS — Replace These First

| Placeholder | Description | Example |
|---|---|---|
| `{{APP_NAME}}` | Your application/artifact name | `TestingJenkinsMavenDocker` |
| `{{APP_PORT}}` | Port your app runs on | `8080` |
| `{{JAVA_VERSION}}` | Java version from pom.xml | `21` |
| `{{MAVEN_VERSION}}` | Maven version to use | `3.9` |
| `{{DOCKER_REGISTRY}}` | Your Docker registry URL | `docker.io/myuser` |
| `{{DOCKER_CREDENTIALS_ID}}` | Jenkins credentials ID for Docker | `docker-registry-credentials` |
| `{{JENKINS_JDK_TOOL}}` | JDK tool name in Jenkins config | `JDK-21` |
| `{{JENKINS_MAVEN_TOOL}}` | Maven tool name in Jenkins config | `Maven-3` |

---

## 📂 FILES TO GENERATE

---

### 1️⃣ `Jenkinsfile` (Project Root)

> Declarative pipeline definition with all CI/CD stages.

```groovy
pipeline {
    agent any

    environment {
        DOCKER_IMAGE = '{{APP_NAME}}'
        DOCKER_TAG = "${env.BUILD_NUMBER}"
        DOCKER_REGISTRY = '{{DOCKER_REGISTRY}}'
        APP_PORT = '{{APP_PORT}}'
    }

    tools {
        maven '{{JENKINS_MAVEN_TOOL}}'
        jdk '{{JENKINS_JDK_TOOL}}'
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build') {
            steps {
                sh 'chmod +x ./scripts/build.sh'
                sh './scripts/build.sh'
            }
        }

        stage('Test') {
            steps {
                sh 'chmod +x ./scripts/test.sh'
                sh './scripts/test.sh'
            }
            post {
                always {
                    junit allowEmptyResults: true, testResults: 'target/surefire-reports/*.xml'
                }
            }
        }

        stage('Package') {
            steps {
                sh 'chmod +x ./scripts/package.sh'
                sh './scripts/package.sh'
            }
            post {
                success {
                    archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
                }
            }
        }

        stage('Docker Build') {
            steps {
                sh 'chmod +x ./scripts/docker-build.sh'
                sh "./scripts/docker-build.sh ${DOCKER_IMAGE} ${DOCKER_TAG}"
            }
        }

        stage('Docker Push') {
            when {
                branch 'main'
            }
            steps {
                withCredentials([usernamePassword(
                    credentialsId: '{{DOCKER_CREDENTIALS_ID}}',
                    usernameVariable: 'DOCKER_USER',
                    passwordVariable: 'DOCKER_PASS'
                )]) {
                    sh 'chmod +x ./scripts/docker-push.sh'
                    sh "./scripts/docker-push.sh ${DOCKER_REGISTRY} ${DOCKER_IMAGE} ${DOCKER_TAG} ${DOCKER_USER} ${DOCKER_PASS}"
                }
            }
        }

        stage('Deploy') {
            when {
                branch 'main'
            }
            steps {
                sh 'chmod +x ./scripts/deploy.sh'
                sh "./scripts/deploy.sh ${DOCKER_IMAGE} ${DOCKER_TAG} ${APP_PORT}"
            }
        }
    }

    post {
        always {
            cleanWs()
        }
        success {
            echo 'Pipeline completed successfully!'
        }
        failure {
            echo 'Pipeline failed!'
        }
    }
}
```

---

### 2️⃣ `Dockerfile` (Project Root)

> Multi-stage Docker build: compile with Maven, run with minimal JRE.

```dockerfile
# ---- Stage 1: Build ----
FROM maven:{{MAVEN_VERSION}}-eclipse-temurin-{{JAVA_VERSION}}-alpine AS build
WORKDIR /app
COPY ../TestingJenkinsMavenDocker/pom.xml .
RUN mvn dependency:go-offline -B
COPY ../TestingJenkinsMavenDocker/src ./src
RUN mvn clean package -DskipTests -B

# ---- Stage 2: Runtime ----
FROM eclipse-temurin:{{JAVA_VERSION}}-jre-alpine
WORKDIR /app

RUN addgroup -S appgroup && adduser -S appuser -G appgroup

COPY --from=build /app/target/*.jar app.jar

RUN chown appuser:appgroup app.jar
USER appuser

EXPOSE {{APP_PORT}}

HEALTHCHECK --interval=30s --timeout=3s --retries=3 \
    CMD wget -qO- http://localhost:{{APP_PORT}}/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]
```

---

### 3️⃣ `.dockerignore` (Project Root)

> Keeps Docker build context small and fast.

```
target/
!.mvn/wrapper/maven-wrapper.jar
*.class
*.jar
*.log
.idea/
*.iml
.DS_Store
docker-compose.override.yml
```

---

### 4️⃣ `docker-compose.yml` (Project Root)

> Run the app locally or in prod with one command.

```yaml
version: '3.8'

services:
  app:
    build:
      context: .
      dockerfile: Dockerfile
    image: {{APP_NAME}}:latest
    container_name: {{APP_NAME}}
    ports:
      - "{{APP_PORT}}:{{APP_PORT}}"
    environment:
      - SPRING_PROFILES_ACTIVE=prod
      - JAVA_OPTS=-Xmx512m -Xms256m
    healthcheck:
      test: ["CMD", "wget", "-qO-", "http://localhost:{{APP_PORT}}/actuator/health"]
      interval: 30s
      timeout: 3s
      retries: 3
    restart: unless-stopped
```

---

### 5️⃣ `scripts/build.sh`

> Compiles the source code.

```bash
#!/bin/bash
set -e
echo "====== BUILDING APPLICATION ======"
mvn clean compile -B
echo "====== BUILD COMPLETE ======"
```

---

### 6️⃣ `scripts/test.sh`

> Runs all unit/integration tests.

```bash
#!/bin/bash
set -e
echo "====== RUNNING TESTS ======"
mvn test -B
echo "====== TESTS COMPLETE ======"
```

---

### 7️⃣ `scripts/package.sh`

> Creates the deployable JAR artifact.

```bash
#!/bin/bash
set -e
echo "====== PACKAGING APPLICATION ======"
mvn package -DskipTests -B
echo "====== PACKAGING COMPLETE ======"
ls -lh target/*.jar
```

---

### 8️⃣ `scripts/docker-build.sh`

> Builds and tags the Docker image. Usage: `./docker-build.sh <image> <tag>`

```bash
#!/bin/bash
set -e

DOCKER_IMAGE=$1
DOCKER_TAG=$2

if [ -z "$DOCKER_IMAGE" ] || [ -z "$DOCKER_TAG" ]; then
    echo "Usage: docker-build.sh <image-name> <tag>"
    exit 1
fi

echo "====== BUILDING DOCKER IMAGE ======"
echo "Image: ${DOCKER_IMAGE}:${DOCKER_TAG}"

docker build -t "${DOCKER_IMAGE}:${DOCKER_TAG}" .
docker tag "${DOCKER_IMAGE}:${DOCKER_TAG}" "${DOCKER_IMAGE}:latest"

echo "====== DOCKER BUILD COMPLETE ======"
docker images | grep "${DOCKER_IMAGE}"
```

---

### 9️⃣ `scripts/docker-push.sh`

> Authenticates and pushes image to registry. Usage: `./docker-push.sh <registry> <image> <tag> <user> <pass>`

```bash
#!/bin/bash
set -e

DOCKER_REGISTRY=$1
DOCKER_IMAGE=$2
DOCKER_TAG=$3
DOCKER_USER=$4
DOCKER_PASS=$5

if [ -z "$DOCKER_REGISTRY" ] || [ -z "$DOCKER_IMAGE" ] || [ -z "$DOCKER_TAG" ]; then
    echo "Usage: docker-push.sh <registry> <image-name> <tag> <username> <password>"
    exit 1
fi

echo "====== PUSHING DOCKER IMAGE ======"
echo "$DOCKER_PASS" | docker login "$DOCKER_REGISTRY" -u "$DOCKER_USER" --password-stdin

FULL_IMAGE="${DOCKER_REGISTRY}/${DOCKER_IMAGE}"

docker tag "${DOCKER_IMAGE}:${DOCKER_TAG}" "${FULL_IMAGE}:${DOCKER_TAG}"
docker tag "${DOCKER_IMAGE}:${DOCKER_TAG}" "${FULL_IMAGE}:latest"

docker push "${FULL_IMAGE}:${DOCKER_TAG}"
docker push "${FULL_IMAGE}:latest"

echo "====== DOCKER PUSH COMPLETE ======"
```

---

### 🔟 `scripts/deploy.sh`

> Stops old container, starts new one, verifies health. Usage: `./deploy.sh <image> <tag> <port>`

```bash
#!/bin/bash
set -e

DOCKER_IMAGE=$1
DOCKER_TAG=$2
APP_PORT=$3

CONTAINER_NAME="{{APP_NAME}}"

echo "====== DEPLOYING APPLICATION ======"

if docker ps -a --format '{{.Names}}' | grep -q "^${CONTAINER_NAME}$"; then
    echo "Stopping existing container..."
    docker stop "$CONTAINER_NAME" || true
    docker rm "$CONTAINER_NAME" || true
fi

echo "Starting new container: ${DOCKER_IMAGE}:${DOCKER_TAG} on port ${APP_PORT}"
docker run -d \
    --name "$CONTAINER_NAME" \
    -p "${APP_PORT}:8080" \
    -e SPRING_PROFILES_ACTIVE=prod \
    -e JAVA_OPTS="-Xmx512m -Xms256m" \
    --restart unless-stopped \
    "${DOCKER_IMAGE}:${DOCKER_TAG}"

echo "====== DEPLOYMENT COMPLETE ======"
echo "Application running at http://localhost:${APP_PORT}"

sleep 10
if docker ps --format '{{.Names}}' | grep -q "^${CONTAINER_NAME}$"; then
    echo "Container is running successfully."
else
    echo "ERROR: Container failed to start!"
    docker logs "$CONTAINER_NAME"
    exit 1
fi
```

---

## ✅ JENKINS SETUP CHECKLIST

### Required Plugins
- [ ] **Pipeline** (`workflow-aggregator`)
- [ ] **Docker Pipeline** (`docker-workflow`)
- [ ] **Git** (`git`)
- [ ] **JUnit** (`junit`)

### Global Tool Configuration (Manage Jenkins → Tools)
- [ ] Add JDK installation named `{{JENKINS_JDK_TOOL}}` → version `{{JAVA_VERSION}}`
- [ ] Add Maven installation named `{{JENKINS_MAVEN_TOOL}}` → version `{{MAVEN_VERSION}}.x`

### Credentials (Manage Jenkins → Credentials)
- [ ] Add **Username/Password** credential with ID `{{DOCKER_CREDENTIALS_ID}}`

### Create the Pipeline Job
- [ ] New Item → **Pipeline**
- [ ] Pipeline Definition: **Pipeline script from SCM**
- [ ] SCM: **Git** → Repository URL → your repo
- [ ] Script Path: `Jenkinsfile`
- [ ] Save and **Build Now**

---

## 🗂️ EXPECTED PROJECT STRUCTURE

```
{{APP_NAME}}/
├── Jenkinsfile
├── Dockerfile
├── .dockerignore
├── docker-compose.yml
├── pom.xml
├── mvnw / mvnw.cmd
├── scripts/
│   ├── build.sh
│   ├── test.sh
│   ├── package.sh
│   ├── docker-build.sh
│   ├── docker-push.sh
│   └── deploy.sh
└── src/
    ├── main/
    └── test/
```

---

## 🔄 PIPELINE FLOW

```
┌──────────┐   ┌───────┐   ┌──────┐   ┌─────────┐   ┌──────────────┐   ┌─────────────┐   ┌────────┐
│ Checkout │──▶│ Build │──▶│ Test │──▶│ Package │──▶│ Docker Build │──▶│ Docker Push │──▶│ Deploy │
└──────────┘   └───────┘   └──────┘   └─────────┘   └──────────────┘   └─────────────┘   └────────┘
   SCM          compile     mvn test   JAR artifact   image:tag          registry          container
                                                                        (main only)       (main only)
```

---

> **💡 Tip:** Do a global find-and-replace for all `{{PLACEHOLDER}}` values with your project-specific values, then create each file in the listed order.

