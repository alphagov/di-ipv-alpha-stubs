#!/usr/bin/env bash
set -eu

echo "Building DI IPV orchestrator stub"
./gradlew clean build

echo "Starting DI IPV orchestrator stub"
./gradlew run