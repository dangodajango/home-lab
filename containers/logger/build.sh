#!/usr/bin/env bash
set -e

IMAGE_NAME="dangodajango/logger"
IMAGE_TAG="1.0.1"

docker build -t "$IMAGE_NAME:$IMAGE_TAG" .

# We need to first login into Dockerhub.
# Docker will save the credentials in the $HOME/.docker/config.json,
# for future operations with the registry we don't need to reauthenticate.
docker push "$IMAGE_NAME:$IMAGE_TAG"

