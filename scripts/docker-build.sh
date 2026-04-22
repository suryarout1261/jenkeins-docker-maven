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

