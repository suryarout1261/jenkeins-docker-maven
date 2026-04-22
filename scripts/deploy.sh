#!/bin/bash
set -e

DOCKER_IMAGE=$1
DOCKER_TAG=$2
APP_PORT=$3

CONTAINER_NAME="TestingJenkinsMavenDocker"

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

