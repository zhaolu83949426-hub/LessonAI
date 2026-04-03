#!/usr/bin/env bash

set -euo pipefail

APP_NAME="lesson-ai"
APP_PORT="${APP_PORT:-8080}"
APP_HOME="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
JAR_PATH="${JAR_PATH:-$(find "$APP_HOME/target" -maxdepth 1 -type f -name '*.jar' ! -name '*original*.jar' | head -n 1)}"
PID_FILE="$APP_HOME/$APP_NAME.pid"
LOG_FILE="$APP_HOME/$APP_NAME.log"

if [[ -z "$JAR_PATH" || ! -f "$JAR_PATH" ]]; then
  echo "未找到可启动的 jar，请先在 $APP_HOME/target 下构建产物。"
  exit 1
fi

if [[ -f "$PID_FILE" ]]; then
  EXISTING_PID="$(cat "$PID_FILE")"
  if [[ -n "$EXISTING_PID" ]] && kill -0 "$EXISTING_PID" 2>/dev/null; then
    echo "$APP_NAME 已在运行，PID=$EXISTING_PID"
    exit 1
  fi
  rm -f "$PID_FILE"
fi

export LESSON_AI_DB_URL="${LESSON_AI_DB_URL:-jdbc:postgresql://192.168.12.22:5432/lessonai}"
export LESSON_AI_DB_USERNAME="${LESSON_AI_DB_USERNAME:-lessonai_user}"
export LESSON_AI_DB_PASSWORD="${LESSON_AI_DB_PASSWORD:-lessonai}"
export LESSON_AI_LLM_DEFAULT_NAME="${LESSON_AI_LLM_DEFAULT_NAME:-默认模型}"
export LESSON_AI_LLM_PROVIDER_TYPE="${LESSON_AI_LLM_PROVIDER_TYPE:-OPENAI_COMPATIBLE}"
export LESSON_AI_LLM_BASE_URL="${LESSON_AI_LLM_BASE_URL:-http://192.168.12.22:7898/v1}"
export LESSON_AI_LLM_API_KEY="${LESSON_AI_LLM_API_KEY:-sk-dd5fcc6f18bcc70fbc56c5f6565de507bf37198e61e5f1a7302d81adeda80c9e}"
export LESSON_AI_LLM_MODEL_CODE="${LESSON_AI_LLM_MODEL_CODE:-gpt-4.1}"
export SERVER_PORT="$APP_PORT"

nohup java -jar "$JAR_PATH" > "$LOG_FILE" 2>&1 &
echo $! > "$PID_FILE"

echo "$APP_NAME 已启动"
echo "JAR: $JAR_PATH"
echo "PID: $(cat "$PID_FILE")"
echo "日志: $LOG_FILE"
