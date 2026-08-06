@echo off
chcp 65001 >nul
setlocal enabledelayedexpansion
cd /d "%~dp0\.."

echo ==========================================
echo   SynPharm Docker 环境 - 一键启动
echo ==========================================
echo.

REM ---- 读取 .env 中的端口（用于提示，读不到用默认值）----
set "FRONTEND_PORT=80"
set "BACKEND_PORT=8080"
set "FASTAPI_PORT=8000"
set "MYSQL_PORT=3306"
if exist ".env" (
    for /f "usebackq tokens=1,* delims==" %%a in (".env") do (
        if "%%a"=="FRONTEND_PORT" set "FRONTEND_PORT=%%b"
        if "%%a"=="BACKEND_PORT" set "BACKEND_PORT=%%b"
        if "%%a"=="FASTAPI_PORT" set "FASTAPI_PORT=%%b"
        if "%%a"=="MYSQL_PORT" set "MYSQL_PORT=%%b"
    )
)

REM ---- 1. 检查 Docker ----
docker info >nul 2>&1
if %errorlevel% neq 0 (
    echo [ERROR] Docker 未运行。
    echo         请先启动 Docker Desktop，或在 WSL 中启动 Docker 守护进程。
    pause
    exit /b 1
)

REM ---- 2. 检查 .env ----
if not exist ".env" (
    echo [WARN] 未找到 deploy\.env，正在从模板创建...
    copy ".env.example" ".env" >nul
    echo [WARN] 请务必编辑 deploy\.env 修改 MYSQL 密码和 JWT_SECRET 后重新执行本脚本！
)

REM ---- 3. 构建并启动 ----
echo [1/3] 构建并启动服务（首次构建可能需要较长时间）...
docker compose up -d --build
if %errorlevel% neq 0 (
    echo [ERROR] 服务启动失败，请查看上方日志。
    pause
    exit /b 1
)

REM ---- 4. 等待后端健康检查 ----
echo [2/3] 等待后端健康检查通过（最多 5 分钟）...
set /a n=0
:wait_loop
set /a n+=1
if !n! geq 60 (
    echo [WARN] 等待超时。请查看日志: docker compose logs backend
    goto done
)
for /f "delims=" %%i in ('docker inspect -f "{{.State.Health.Status}}" synpharm-backend 2^>nul') do set "HEALTH=%%i"
if "!HEALTH!"=="healthy" (
    echo [OK] 后端已就绪！
    goto done
)
timeout /t 5 /nobreak >nul
goto wait_loop

:done
echo [3/3] 服务状态：
docker compose ps
echo.
echo ==========================================
echo   启动完成！访问地址：
echo   ----------------------------------------
echo   前端页面:     http://localhost:%FRONTEND_PORT%
echo   SpringBoot:   http://localhost:%BACKEND_PORT%/doc.html
echo   FastAPI 文档: http://localhost:%FASTAPI_PORT%/docs
echo   健康检查:     http://localhost:%BACKEND_PORT%/actuator/health
echo   MySQL:        localhost:%MYSQL_PORT%
echo   ----------------------------------------
echo   停止服务: scripts\stop.bat
echo   查看日志: docker compose logs -f
echo ==========================================
endlocal
