#!/usr/bin/env bash

set -euo pipefail

ARTIFACT1_PID_FILE="/tmp/c2-artifact1.pid"
ARTIFACT2_PID_FILE="/tmp/c2-artifact2.pid"

stop_from_pid_file() {
  local name="$1"
  local pid_file="$2"

  if [[ ! -f "${pid_file}" ]]; then
    echo "${name}: pid file not found (${pid_file})"
    return
  fi

  local pid
  pid="$(cat "${pid_file}")"

  if [[ -z "${pid}" ]]; then
    echo "${name}: empty pid file (${pid_file})"
    return
  fi

  if kill -0 "${pid}" 2>/dev/null; then
    kill "${pid}"
    echo "${name}: stopped PID ${pid}"
  else
    echo "${name}: PID ${pid} is not running"
  fi

  rm -f "${pid_file}"
}

stop_from_pid_file "artifact1" "${ARTIFACT1_PID_FILE}"
stop_from_pid_file "artifact2" "${ARTIFACT2_PID_FILE}"
