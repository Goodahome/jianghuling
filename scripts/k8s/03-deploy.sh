#!/usr/bin/env bash
# 应用 k8s 清单并等待 rollout（独立 Redis；MySQL 用平台现有库）
#
# 用法：
#   cd ~/jianghu_ling
#   bash scripts/k8s/03-deploy.sh

set -euo pipefail

ROOT="$(cd "$(dirname "$0")/../.." && pwd)"
K8S_DIR="${ROOT}/k8s"
NS="jianghu-ling"
HOST="jianghu.kssrol.com"

if ! command -v kubectl >/dev/null 2>&1; then
  echo "未找到 kubectl。请在 master(10.120.20.17) 执行，或配置本机 kubeconfig。" >&2
  exit 1
fi

echo "==> kubectl 上下文: $(kubectl config current-context 2>/dev/null || echo 'N/A')"
echo "==> apply ${K8S_DIR}"

for f in \
  00-namespace.yaml \
  01-secrets.yaml \
  02-configmap.yaml \
  10-redis.yaml \
  20-backend.yaml \
  21-frontend.yaml \
  30-ingress.yaml
do
  echo "  - $f"
  kubectl apply -f "${K8S_DIR}/${f}"
done

# TLS Secret 需在本 ns；若不存在则提示复制
if ! kubectl -n "$NS" get secret kssrol-com-tls >/dev/null 2>&1; then
  echo
  echo "警告: namespace ${NS} 中未找到 Secret kssrol-com-tls"
  echo "请从证书所在 ns 复制到 ${NS}，例如："
  echo "  kubectl get secret kssrol-com-tls -n <源namespace> -o yaml \\"
  echo "    | sed 's/namespace: .*/namespace: ${NS}/' \\"
  echo "    | kubectl apply -f -"
  echo
fi

echo "==> 等待 Redis / Backend / Frontend Ready ..."
kubectl -n "$NS" rollout status deployment/redis --timeout=180s
kubectl -n "$NS" rollout status deployment/backend --timeout=300s
kubectl -n "$NS" rollout status deployment/frontend --timeout=180s

echo
echo "==> Pods"
kubectl -n "$NS" get pods -o wide
echo
echo "==> Services / Ingress"
kubectl -n "$NS" get svc,ingress
echo
echo "==> 健康检查："
echo "  kubectl -n $NS exec deploy/backend -- curl -fsS http://127.0.0.1:8080/actuator/health"
echo
echo "==> 浏览器访问：https://${HOST}/"
echo "  DNS/解析：${HOST} → 10.120.20.200"
echo
echo "说明：未部署本 ns 内 MySQL；后端连接 mysql.ks-platform-dev.svc.cluster.local"
echo "可选清单 k8s/11-mysql.yaml 仅作备用，默认不 apply。"
