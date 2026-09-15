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
  echo "[WARN] 已创建 deploy/.env，请修改 MYSQL 密码和 JWT_SECRET 后重新执行本脚本！"
  exit 1
fi

# ---- 3. 校验 Compose 配置 ----
if ! docker compose config --quiet; then
  echo "[ERROR] deploy/.env 或 Docker Compose 配置无效。"
  exit 1
fi

# ---- 4. 端口预检（仅提示，不阻断）----
echo "[1/3] 端口预检..."
busy=""
for p in $(grep -E '^(FRONTEND|BACKEND|FASTAPI|MYSQL|REDIS)_PORT=' .env 2>/dev/null | cut -d= -f2 || true); do
  if command -v ss >/dev/null 2>&1; then
    if ss -lnt "sport = :$p" 2>/dev/null | grep -q .; then busy="$busy $p"; fi
  elif command -v netstat >/dev/null 2>&1; then
    if netstat -lnt 2>/dev/null | grep -q ":$p "; then busy="$busy $p"; fi
  fi
done
if [ -n "$busy" ]; then
  echo "[WARN] 以下端口已被占用（若为本项目容器在运行，可忽略）：$busy"
else
  echo "[OK] 端口无冲突。"
fi

# ---- 5. 构建并启动（--wait 会等到服务全部 healthy 才返回）----
echo ""
echo "[2/3] 构建并启动服务（首次需拉镜像+编译，约 10-20 分钟，请耐心等待）..."
if ! docker compose up -d --build --wait --wait-timeout 600; then
  echo ""
  echo "[ERROR] 启动失败，常见原因与排查："
  echo "        1) 拉取镜像超时 / failed to fetch anonymous token  -> 检查网络与 Docker 镜像加速源"
  echo "        2) 端口无法绑定                                    -> 检查 .env 中的 *_PORT 与占用进程"
  echo "        3) 应用启动报错                                    -> docker compose logs backend"
  exit 1
fi

echo ""
echo "[3/3] 服务状态："
docker compose ps

# 读取端口用于提示
frontend_port="$(grep -E '^FRONTEND_PORT=' .env | cut -d= -f2 || echo 80)"
backend_port="$(grep -E '^BACKEND_PORT=' .env | cut -d= -f2 || echo 8080)"
fastapi_port="$(grep -E '^FASTAPI_PORT=' .env | cut -d= -f2 || echo 8000)"

# 端口 80 是浏览器默认端口，URL 中省略更整洁
frontend_url="http://localhost"
if [ "${frontend_port}" != "80" ]; then
  frontend_url="http://localhost:${frontend_port}"
fi

echo ""
echo "=========================================="
echo "  启动完成！"
echo "  ----------------------------------------"
echo "  前端页面:     ${frontend_url}"
echo "  后端接口文档: http://localhost:${backend_port}/doc.html"
echo "  FastAPI 文档: http://localhost:${fastapi_port}/docs"
echo "  RabbitMQ:     http://localhost:15672"
echo "  健康检查:     http://localhost:${backend_port}/actuator/health"
echo "  ----------------------------------------"
echo "  停止服务: scripts/stop.sh"
echo "  查看日志: docker compose logs -f"
echo "=========================================="

# 自动打开浏览器（设置 NO_BROWSER=1 可跳过）
if [ "${NO_BROWSER:-0}" != "1" ]; then
  if command -v xdg-open >/dev/null 2>&1; then
    xdg-open "${frontend_url}" >/dev/null 2>&1 &
  elif command -v open >/dev/null 2>&1; then
    open "${frontend_url}" >/dev/null 2>&1 &
  else
    echo "请手动打开: ${frontend_url}"
  fi
fi
