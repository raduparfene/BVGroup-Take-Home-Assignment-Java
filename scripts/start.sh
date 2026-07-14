#!/bin/sh

set -eu

cd "$(dirname "$0")/.."

case "${1:-}" in
  full)
    docker compose up --build -d
    ;;
  local)
    docker compose stop processing-app repository-app > /dev/null 2>&1 || true
    docker compose up -d --wait --wait-timeout 120 kafka
    docker compose run --rm --no-deps kafka-topic-setup
    ;;
  *)
    echo "Usage: sh scripts/start.sh <full|local>" >&2
    echo "  full  - runs Kafka and both applications in Docker" >&2
    echo "  local - runs Kafka in Docker; start both applications from the IDE" >&2
    exit 1
    ;;
esac
