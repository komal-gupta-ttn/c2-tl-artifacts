#!/usr/bin/env bash

set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ARTIFACT1_DIR="${ROOT_DIR}/artifact-1-openspec-feature"
ARTIFACT2_DIR="${ROOT_DIR}/artifact-2-ai-infra/rag-assistant"
ARTIFACT1_PID_FILE="/tmp/c2-artifact1.pid"
ARTIFACT2_PID_FILE="/tmp/c2-artifact2.pid"
ARTIFACT1_LOG="/tmp/artifact1.log"
ARTIFACT2_LOG="/tmp/artifact2.log"
OPENJDK21_HOME="/opt/homebrew/opt/openjdk/libexec/openjdk.jdk/Contents/Home"

ensure_java17_plus() {
  local detected_version
  detected_version="$(java -version 2>&1 | awk -F\" '/version/ {print $2}')"
  local major
  major="$(echo "${detected_version}" | awk -F. '{print $1}')"

  if [[ "${major}" -lt 17 ]]; then
    if [[ -d "${OPENJDK21_HOME}" ]]; then
      export JAVA_HOME="${OPENJDK21_HOME}"
      export PATH="${JAVA_HOME}/bin:${PATH}"
    else
      echo "Java 17+ is required. Current version: ${detected_version}"
      echo "Set JAVA_HOME to JDK 17+ and rerun."
      exit 1
    fi
  fi
}

assert_port_free() {
  local port="$1"
  if lsof -iTCP:"${port}" -sTCP:LISTEN >/dev/null 2>&1; then
    echo "Port ${port} is already in use. Stop the existing process and rerun."
    exit 1
  fi
}

ensure_java17_plus
assert_port_free 8081
assert_port_free 8082

echo "Building both artifacts with Maven tests..."
(cd "${ARTIFACT1_DIR}" && mvn clean test package -q)
(cd "${ARTIFACT2_DIR}" && mvn clean test package -q)

ARTIFACT1_JAR="${ARTIFACT1_DIR}/target/artifact-1-openspec-feature-1.0.0.jar"
ARTIFACT2_JAR="${ARTIFACT2_DIR}/target/artifact-2-rag-assistant-1.0.0.jar"

if [[ ! -f "${ARTIFACT1_JAR}" || ! -f "${ARTIFACT2_JAR}" ]]; then
  echo "Build output JARs not found. Check Maven build logs."
  exit 1
fi

echo "Starting artifact 1 on port 8081..."
nohup java -jar "${ARTIFACT1_JAR}" > "${ARTIFACT1_LOG}" 2>&1 &
echo $! > "${ARTIFACT1_PID_FILE}"

echo "Starting artifact 2 on port 8082..."
nohup java -jar "${ARTIFACT2_JAR}" > "${ARTIFACT2_LOG}" 2>&1 &
echo $! > "${ARTIFACT2_PID_FILE}"

echo "Started."
echo "Artifact 1: http://localhost:8081/artifact1 (PID $(cat "${ARTIFACT1_PID_FILE}"))"
echo "Artifact 2: http://localhost:8082/artifact2 (PID $(cat "${ARTIFACT2_PID_FILE}"))"
echo "Logs: ${ARTIFACT1_LOG}, ${ARTIFACT2_LOG}"
