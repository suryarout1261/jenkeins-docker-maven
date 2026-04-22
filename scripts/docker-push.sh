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

