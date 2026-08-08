#!/usr/bin/env bash
# 在构建机构建并推送前后端镜像到私有仓库 10.120.20.201:5000（HTTP insecure）
#
# 前置：
#   - Docker 已安装且当前用户可 docker
#   - 已配置 insecure-registry（见下方提示）
#   - 代码已同步到本机仓库根（含 backend/ frontend/）
#
# 用法：
#   cd ~/jianghu_ling
#   bash scripts/k8s/02-build-push.sh
#   TAG=v1 bash scripts/k8s/02-build-push.sh

set -euo pipefail

ROOT="$(cd "$(dirname "$0")/../.." && pwd)"
cd "$ROOT"

REGISTRY="${REGISTRY:-10.120.20.201:5000}"
TAG="${TAG:-latest}"
BACKEND_IMAGE="${REGISTRY}/jianghu-ling-backend:${TAG}"
FRONTEND_IMAGE="${REGISTRY}/jianghu-ling-frontend:${TAG}"

echo "=============================================="
echo " 私有仓库为 HTTP，Docker 需配置 insecure-registry"
echo " 在构建机编辑 /etc/docker/daemon.json，例如："
echo '  {'
echo '    "insecure-registries": ["10.120.20.201:5000"]'
echo '  }'
echo " 然后：sudo systemctl restart docker"
echo " 节点侧（containerd/CRI）也需允许拉取该 HTTP 仓库，"
echo " 否则 kubelet 拉镜像会失败。"
echo "=============================================="
echo

if ! docker info >/dev/null 2>&1; then
  echo "Docker 不可用，请检查守护进程与权限" >&2
  exit 1
fi

# 探测 insecure 配置（尽力提示，不强制解析 JSON）
if ! docker info 2>/dev/null | grep -qi "10.120.20.201:5000"; then
  echo "警告: docker info 未显示 insecure registry 10.120.20.201:5000"
  echo "若 push 报 http: server gave HTTP response to HTTPS client，请按上文配置。"
  echo
fi

# 可选：机上 mvn package（Dockerfile 多阶段已自带 Maven，一般不需要）
if [[ "${HOST_MVN_PACKAGE:-0}" == "1" ]]; then
  if [[ -z "${JAVA_HOME:-}" || ! -x "${JAVA_HOME}/bin/java" ]]; then
    echo "HOST_MVN_PACKAGE=1 但 JAVA_HOME 无效。可先："
    echo "  bash scripts/k8s/01-setup-temp-jdk.sh"
    echo "  eval \"\$(bash scripts/k8s/01-setup-temp-jdk.sh --print-exports)\""
    exit 1
  fi
  echo "==> 使用临时 JAVA_HOME=${JAVA_HOME} 机上打包"
  (cd backend && mvn -B -DskipTests clean package)
fi

echo "==> 构建后端: ${BACKEND_IMAGE}"
docker build -t "${BACKEND_IMAGE}" -f backend/Dockerfile backend

echo "==> 构建前端: ${FRONTEND_IMAGE}"
docker build -t "${FRONTEND_IMAGE}" -f frontend/Dockerfile frontend

echo "==> 推送 ${BACKEND_IMAGE}"
docker push "${BACKEND_IMAGE}"

echo "==> 推送 ${FRONTEND_IMAGE}"
docker push "${FRONTEND_IMAGE}"

echo
echo "==> 完成"
echo "    ${BACKEND_IMAGE}"
echo "    ${FRONTEND_IMAGE}"
