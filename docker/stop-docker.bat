@echo off
chcp 65001 >nul
cd /d "%~dp0"
echo ==========================================
echo   SynPharm Docker 环境停止脚本
echo ==========================================
echo.

echo [1/2] 停止服务...
docker compose down

echo.
echo [2/2] 清理资源（可选）...
echo 如需清理数据卷，请运行: docker compose down -v
echo.
echo ==========================================
echo   服务已停止！
echo ==========================================