#!/usr/bin/env bash
# 从本机（Git Bash / WSL / Linux / macOS）同步到构建机
# 用法（仓库根目录）：
#   bash scripts/k8s/sync.sh
#   INCLUDE_GIT=1 bash scripts/k8s/sync.sh

set -euo pipefail

BUILD_HOST="${BUILD_HOST:-kssrol@100.66.0.3}"
REMOTE_DIR="${REMOTE_DIR:-~/jianghu_ling}"
INCLUDE_GIT="${INCLUDE_GIT:-0}"

ROOT="$(cd "$(dirname "$0")/../.." && pwd)"
cd "$ROOT"

echo "==> 同步源: $ROOT"
echo "==> 目标:   ${BUILD_HOST}:${REMOTE_DIR}"

ssh "$BUILD_HOST" "mkdir -p $REMOTE_DIR"

EXCLUDES=(
  --exclude 'node_modules'
  --exclude 'frontend/node_modules'
  --exclude 'backend/target'
  --exclude 'frontend/dist'
  --exclude '.idea'
  --exclude '.vscode'
  --exclude '*.iml'
  --exclude 'Thumbs.db'
  --exclude '.DS_Store'
  --exclude 'docker/.env'
  --exclude '.env'
)
if [[ "$INCLUDE_GIT" != "1" ]]; then
  EXCLUDES+=(--exclude '.git')
fi

if command -v rsync >/dev/null 2>&1; then
  rsync -avz --delete "${EXCLUDES[@]}" ./ "${BUILD_HOST}:${REMOTE_DIR}/"
else
  echo "未找到 rsync，回退 scp（不含 --delete）"
  TMP="$(mktemp -d)"
  trap 'rm -rf "$TMP"' EXIT
  # 简易拷贝并排除常见大目录
  tar \
    --exclude='node_modules' \
    --exclude='backend/target' \
    --exclude='frontend/dist' \
    --exclude='.idea' \
    --exclude='.vscode' \
    $( [[ "$INCLUDE_GIT" != "1" ]] && echo --exclude='.git' ) \
    -cf - . | (cd "$TMP" && tar -xf -)
  scp -r "$TMP"/* "${BUILD_HOST}:${REMOTE_DIR}/"
fi

echo "==> 同步完成"
echo "    ssh $BUILD_HOST"
echo "    cd $REMOTE_DIR && bash scripts/k8s/02-build-push.sh && bash scripts/k8s/03-deploy.sh"
