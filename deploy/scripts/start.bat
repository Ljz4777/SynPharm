@echo off
chcp 65001 >nul
setlocal enabledelayedexpansion
cd /d "%~dp0\.."

echo ==========================================
echo   SynPharm Docker 环境 - 一键启动
echo ==========================================
echo.

REM ---- 检查 Docker ----
docker info >nul 2>&1
if %errorlevel% neq 0 (
    echo [ERROR] Docker 未运行。
    echo         请先启动 Docker Desktop，或在 WSL 中启动 Docker 守护进程。
    pause
    exit /b 1
)

REM ---- 检查 .env ----
if not exist ".env" (
    echo [WARN] 未找到 deploy\.env，正在从模板创建...
    copy ".env.example" ".env" >nul
    echo [WARN] 已创建 deploy\.env，请修改密码和 JWT_SECRET 后重新执行。
    pause
    exit /b 1
)

REM ---- 读取 .env 中的端口（用于预检与提示）----
set "FRONTEND_PORT=80"
set "BACKEND_PORT=7000"
set "FASTAPI_PORT=9050"
set "MYSQL_PORT=13307"
set "REDIS_PORT=6380"
if exist ".env" (
    for /f "usebackq tokens=1,* delims==" %%a in (".env") do (
        if "%%a"=="FRONTEND_PORT" set "FRONTEND_PORT=%%b"
        if "%%a"=="BACKEND_PORT" set "BACKEND_PORT=%%b"
        if "%%a"=="FASTAPI_PORT" set "FASTAPI_PORT=%%b"
        if "%%a"=="MYSQL_PORT" set "MYSQL_PORT=%%b"
        if "%%a"=="REDIS_PORT" set "REDIS_PORT=%%b"
    )
)

REM ---- 1. 前置检查：Docker 守护进程 + 端口可绑定性 ----
echo [1/4] 前置检查（Docker 守护进程、端口占用与保留段）...
powershell -NoProfile -ExecutionPolicy Bypass -File "scripts\preflight.ps1" -Ports %FRONTEND_PORT%,%BACKEND_PORT%,%FASTAPI_PORT%,%MYSQL_PORT%,%REDIS_PORT% -Names frontend,backend,fastapi,mysql,redis
if !errorlevel! neq 0 (
    echo.
    echo [ERROR] 前置检查未通过，已中止（避免构建十几分钟后才发现端口冲突）。
    pause
    exit /b 1
)

REM ---- 2. 校验 Compose 配置 ----
echo.
echo [2/4] 校验 docker-compose 配置...
docker compose config --quiet
if %errorlevel% neq 0 (
    echo [ERROR] deploy\.env 或 Docker Compose 配置无效。
    pause
    exit /b 1
)

REM ---- 3. 构建并启动（--wait 会等到服务全部 healthy 才返回）----
echo.
echo [3/4] 构建并启动服务（首次需拉镜像+编译，约 10-20 分钟，请耐心等待）...
docker compose up -d --build --wait --wait-timeout 600
if !errorlevel! neq 0 (
    echo.
    echo [ERROR] 启动失败，常见原因与排查：
    echo         1^) 拉取镜像超时 / failed to fetch anonymous token   --^> 检查网络与 Docker 镜像加速源
    echo         2^) 端口无法绑定                                    --^> 运行 scripts\preflight.ps1 查看原因
    echo         3^) 应用启动报错                                    --^> docker compose logs backend
    pause
    exit /b 1
)

REM ---- 4. 汇总并打开浏览器 ----
REM 端口 80 是浏览器默认端口，URL 中省略更整洁
set "FRONTEND_URL=http://localhost"
if not "%FRONTEND_PORT%"=="80" set "FRONTEND_URL=http://localhost:%FRONTEND_PORT%"

echo.
echo [4/4] 服务状态：
docker compose ps
echo.
echo ==========================================
echo   启动完成！正在打开浏览器...
echo   ----------------------------------------
echo   前端页面:     %FRONTEND_URL%
echo   后端接口文档: http://localhost:%BACKEND_PORT%/doc.html
echo   FastAPI 文档: http://localhost:%FASTAPI_PORT%/docs
echo   RabbitMQ:     http://localhost:15672
echo   健康检查:     http://localhost:%BACKEND_PORT%/actuator/health
echo   ----------------------------------------
echo   停止服务: scripts\stop.bat
echo   查看日志: docker compose logs -f
echo ==========================================
REM 等 2 秒让上面的状态输出可见（用 ping 而非 timeout，避免非交互终端下报错）
ping -n 3 127.0.0.1 >nul 2>&1
start "" "%FRONTEND_URL%"
endlocal
