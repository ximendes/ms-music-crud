#!/usr/bin/env bash

set -euo pipefail

IMAGE_NAME="${1:-my-app}"
IMAGE_TAG="${2:-v1.0}"

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

cd "$SCRIPT_DIR"

echo "Build do projeto"
./gradlew clean build


echo "Build da imagem docker"
docker build \
  -t "${IMAGE_NAME}:${IMAGE_TAG}" \
  -f Docker/Dockerfile \
  .

echo "Run docker image"
docker run -d -p 8080:8080 \
  --name crud-container \
  --network docker_default \
  -e AWS_DYNAMODB_ENDPOINT=http://localstack:4566 \
  my-app:v1.0