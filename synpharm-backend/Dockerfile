# =============================================================================
# SynPharm 后端镜像 - 企业级多阶段构建
#
# 阶段一（build）：Maven 编译打包
# 阶段二（runtime）：精简 JRE + 非 root 运行
#
# 构建：docker build -t synpharm/backend:1.0.0 .
# =============================================================================

# ---------- 阶段一：构建 ----------
FROM maven:3.9.6-eclipse-temurin-17 AS build

WORKDIR /build

# 先复制 pom.xml，利用 Docker 层缓存避免每次重新下载依赖
COPY pom.xml .
RUN mvn -B dependency:go-offline

# 复制源码并打包（跳过测试，测试在 CI 阶段单独执行）
COPY src ./src
RUN mvn -B clean package -DskipTests

# ---------- 阶段二：运行 ----------
FROM eclipse-temurin:17-jre

# 非 root 用户 + 健康检查工具 curl
RUN useradd --system --uid 10001 --create-home appuser \
    && apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app

COPY --from=build /build/target/synpharm-backend-1.0.0.jar app.jar

# 数据目录（上传/结果），赋予运行用户权限
RUN mkdir -p /app/uploads /app/results \
    && chown -R appuser:appuser /app

USER appuser

EXPOSE 8080

# 企业级健康检查：依赖 spring-boot-starter-actuator
HEALTHCHECK --interval=30s --timeout=5s --start-period=90s --retries=3 \
  CMD curl -fsS http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]