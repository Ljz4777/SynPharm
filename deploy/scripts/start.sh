#!/usr/bin/env bash
# =============================================================================
# SynPharm Docker 环境 - 一键启动（Linux / macOS / WSL）
# =============================================================================
set -euo pipefail
cd "$(dirname "$0")/.."

echo "=========================================="
echo "  SynPharm Docker 环境 - 一键启动"
echo "=========================================="

# ---- 1. 检查 Docker ----
if ! docker info >/dev/null 2>&1; then
  echo "[ERROR] Docker 未运行，请先启动 Docker 守护进程。"
  exit 1
fi

# ---- 2. 检查 .env ----
if [ ! -f ".env" ]; then
  echo "[WARN] 未找到 .env，正在从模板创建..."
  cp .env.example .env
  echo "[WARN] 请务必编辑 deploy/.env 修改 MYSQL 密码和 JWT_SECRET 后重新执行本脚本！"
fi

# ---- 3. 构建并启动 ----
echo "[1/3] 构建并启动服务（首次构建可能需要较长时间）..."
docker compose up -d --build

# ---- 4. 等待后端健康检查 ----
echo "[2/3] 等待后端健康检查通过（最多 5 分钟）..."
for i in $(seq 1 60); do
  status="$(docker inspect -f '{{.State.Health.Status}}' synpharm-backend 2>/dev/null || true)"
  if [ "$status" = "healthy" ]; then
    echo "[OK] 后端已就绪！"
    break
  fi
  if [ "$i" = "60" ]; then
    echo "[WARN] 等待超时。请查看日志: docker compose logs backend"
  fi
  sleep 5
done

echo "[3/3] 服务状态："
docker compose ps

# 读取端口用于提示
frontend_port="$(grep -E '^FRONTEND_PORT=' .env | cut -d= -f2 || echo 80)"
backend_port="$(grep -E '^BACKEND_PORT=' .env | cut -d= -f2 || echo 8080)"
fastapi_port="$(grep -E '^FASTAPI_PORT=' .env | cut -d= -f2 || echo 8000)"

echo ""
echo "=========================================="
echo "  启动完成！访问地址："
echo "  ----------------------------------------"
echo "  前端页面:     http://localhost:${frontend_port}"
echo "  SpringBoot:   http://localhost:${backend_port}/doc.html"
echo "  FastAPI 文档: http://localhost:${fastapi_port}/docs"
echo "  健康检查:     http://localhost:${backend_port}/actuator/health"
echo "  ----------------------------------------"
echo "  停止服务: scripts/stop.sh"
echo "  查看日志: docker compose logs -f"
echo "=========================================="
