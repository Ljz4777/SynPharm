@echo off
chcp 65001 >nul
cd /d "%~dp0"
echo ==========================================
echo   SynPharm Docker 环境启动脚本
echo ==========================================
echo.

echo [1/4] 检查 Docker 是否运行...
docker info >nul 2>&1
if %errorlevel% neq 0 (
    echo ERROR: Docker 未运行，请先启动 Docker Desktop
    pause
    exit /b 1
)
echo OK: Docker 运行正常
echo.

echo [2/4] 构建并启动服务...
echo 这可能需要几分钟时间，请耐心等待...
echo.
docker compose up -d --build

echo.
echo [3/4] 检查服务启动状态...
echo 等待 MySQL 和 Redis 健康检查通过...
timeout /t 40 /nobreak >nul

echo.
echo [4/4] 验证服务状态...
echo ------------------------------------------
echo MySQL:
docker inspect synpharm-mysql --format='{{.State.Health.Status}}'
echo Redis:
docker inspect synpharm-redis --format='{{.State.Health.Status}}'
echo FastAPI:
docker inspect synpharm-fastapi --format='{{.State.Health.Status}}'
echo Spring Boot:
docker inspect synpharm-backend --format='{{.State.Health.Status}}'
echo ------------------------------------------

echo.
echo ==========================================
echo   服务启动完成！
echo ==========================================
echo.
echo 访问地址：
echo   FastAPI 文档: http://localhost:8000/docs
echo   Spring Boot 文档: http://localhost:8080/doc.html
echo.
echo 测试命令：
echo   curl http://localhost:8000/health
echo   curl http://localhost:8080/api/auth/captcha/send -H "Content-Type: application/json" -d "{\"email\":\"test@qq.com\",\"type\":\"login\"}"
echo.
echo 停止服务：docker compose down
echo 查看日志：docker compose logs -f
echo ==========================================