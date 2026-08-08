# 江湖互助平台 · 后端镜像（多阶段：Maven 构建 + JRE 运行）
# 目标：Java 17（与 pom.xml java.version 一致）

FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /build

COPY pom.xml .
# 预拉依赖，加速后续构建（pom 未变时可命中层缓存）
RUN mvn -B -q -DskipTests dependency:go-offline || true

COPY src ./src
RUN mvn -B -DskipTests clean package \
    && JAR="$(find target -maxdepth 1 -type f -name '*.jar' ! -name '*.original' | head -n1)" \
    && test -n "$JAR" \
    && cp "$JAR" /build/app.jar

FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/* \
    && mkdir -p /data/uploads \
    && groupadd --system jhl \
    && useradd --system --gid jhl --home-dir /app --shell /usr/sbin/nologin jhl \
    && chown -R jhl:jhl /app /data/uploads

COPY --from=build /build/app.jar /app/app.jar

ENV UPLOAD_DIR=/data/uploads \
    SPRING_PROFILES_ACTIVE=dev \
    JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 \
  CMD curl -fsS http://127.0.0.1:8080/actuator/health || exit 1

USER jhl
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]
