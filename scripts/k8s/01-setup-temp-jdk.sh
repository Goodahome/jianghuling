#!/usr/bin/env bash
# 在构建机下载 Temurin JDK 17 到 ~/tools/jdk-17-tmp
# 仅打印 export 语句，不改系统默认 java。
#
# 用法：
#   bash scripts/k8s/01-setup-temp-jdk.sh          # 安装（若已存在则跳过下载）
#   source <(bash scripts/k8s/01-setup-temp-jdk.sh --print-exports)
#   eval "$(bash scripts/k8s/01-setup-temp-jdk.sh --print-exports)"

set -euo pipefail

JDK_HOME="${HOME}/tools/jdk-17-tmp"
PRINT_ONLY=0
FORCE=0

for arg in "$@"; do
  case "$arg" in
    --print-exports) PRINT_ONLY=1 ;;
    --force) FORCE=1 ;;
  esac
done

print_exports() {
  cat <<EOF
export JAVA_HOME="${JDK_HOME}"
export PATH="\${JAVA_HOME}/bin:\${PATH}"
EOF
}

if [[ "$PRINT_ONLY" -eq 1 ]]; then
  print_exports
  exit 0
fi

mkdir -p "$(dirname "$JDK_HOME")"

if [[ -x "${JDK_HOME}/bin/java" && "$FORCE" -eq 0 ]]; then
  echo "==> 已存在: ${JDK_HOME}"
else
  ARCH="$(uname -m)"
  case "$ARCH" in
    x86_64|amd64) ADOPT_ARCH="x64" ;;
    aarch64|arm64) ADOPT_ARCH="aarch64" ;;
    *)
      echo "不支持的架构: $ARCH" >&2
      exit 1
      ;;
  esac

  # Eclipse Temurin 17（Adoptium API）
  API="https://api.adoptium.net/v3/binary/latest/17/ga/linux/${ADOPT_ARCH}/jdk/hotspot/normal/eclipse?project=jdk"
  TMP_TGZ="$(mktemp /tmp/temurin17.XXXXXX.tar.gz)"
  echo "==> 下载 Temurin 17 (${ADOPT_ARCH}) ..."
  curl -fsSL -o "$TMP_TGZ" -L "$API"

  EXTRACT_DIR="$(mktemp -d /tmp/temurin17.XXXXXX)"
  tar -xzf "$TMP_TGZ" -C "$EXTRACT_DIR"
  INNER="$(find "$EXTRACT_DIR" -maxdepth 1 -type d -name 'jdk-17*' | head -n1)"
  if [[ -z "$INNER" ]]; then
    echo "解压后未找到 jdk-17* 目录" >&2
    exit 1
  fi

  rm -rf "$JDK_HOME"
  mv "$INNER" "$JDK_HOME"
  rm -rf "$EXTRACT_DIR" "$TMP_TGZ"
  echo "==> 已安装到 ${JDK_HOME}"
fi

echo
echo "==> 当前临时 JDK 版本："
"${JDK_HOME}/bin/java" -version
echo
echo "==> 请在当前 shell 执行（不改系统默认 java）："
print_exports
echo
echo "也可一行生效："
echo "  eval \"\$(bash scripts/k8s/01-setup-temp-jdk.sh --print-exports)\""
