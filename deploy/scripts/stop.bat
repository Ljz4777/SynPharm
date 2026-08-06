@echo off
chcp 65001 >nul
cd /d "%~dp0\.."

echo ==========================================
echo   SynPharm Docker 环境 - 停止服务
echo ==========================================
echo.

docker compose down

echo.
echo 服务已停止。
echo 数据卷已保留（数据库、Redis、上传文件）。如需彻底清除数据，请执行:
echo   docker compose down -v
echo ==========================================
