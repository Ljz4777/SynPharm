@echo off
REM =====================================================================
REM  SynPharm - one-click start (Docker)
REM
REM  KEEP THIS FILE ASCII-ONLY. Do not put Chinese text in here.
REM  cmd.exe mis-parses a .bat containing non-ASCII bytes: REM lines and even
REM  echo commands get chopped up and run as bogus commands. Verified under
REM  codepage 65001 and 936, with and without `chcp`.
REM  Chinese documentation lives in deploy\ (see the .md files there).
REM =====================================================================
setlocal enabledelayedexpansion
cd /d "%~dp0\.."

echo ==========================================
echo   SynPharm Docker Environment - Start
echo ==========================================
echo.

REM ---------------------------------------------------------------------
REM 1. Locate docker.exe
REM    A terminal opened before Docker was installed keeps a stale PATH and
REM    reports "Docker not running" even though the engine is perfectly fine.
REM ---------------------------------------------------------------------
echo [1/6] Locating docker...
set "DOCKER="
for /f "delims=" %%i in ('where docker 2^>nul') do if not defined DOCKER set "DOCKER=%%i"
if not defined DOCKER if exist "%LOCALAPPDATA%\Programs\DockerDesktop\resources\bin\docker.exe" set "DOCKER=%LOCALAPPDATA%\Programs\DockerDesktop\resources\bin\docker.exe"
if not defined DOCKER if exist "%ProgramFiles%\Docker\Docker\resources\bin\docker.exe" set "DOCKER=%ProgramFiles%\Docker\Docker\resources\bin\docker.exe"
if not defined DOCKER if exist "%ProgramW6432%\Docker\Docker\resources\bin\docker.exe" set "DOCKER=%ProgramW6432%\Docker\Docker\resources\bin\docker.exe"
if not defined DOCKER (
    echo.
    echo [ERROR] docker.exe not found. Install Docker Desktop first:
    echo         https://www.docker.com/products/docker-desktop/
    pause
    exit /b 1
)
REM NOTE: write %%~dpi - the variable letter must come LAST. %%~dp would yield only "C:".
for %%i in ("%DOCKER%") do set "PATH=%%~dpi;!PATH!"
echo       found: %DOCKER%

REM ---------------------------------------------------------------------
REM 2. Make sure the Docker engine is running (launch Docker Desktop if not)
REM ---------------------------------------------------------------------
echo.
echo [2/6] Checking Docker engine...
docker info >nul 2>&1
if !errorlevel! equ 0 goto :engine_ready

set "DOCKER_DESKTOP="
if exist "%LOCALAPPDATA%\Programs\DockerDesktop\Docker Desktop.exe" set "DOCKER_DESKTOP=%LOCALAPPDATA%\Programs\DockerDesktop\Docker Desktop.exe"
if not defined DOCKER_DESKTOP if exist "%ProgramFiles%\Docker\Docker\Docker Desktop.exe" set "DOCKER_DESKTOP=%ProgramFiles%\Docker\Docker\Docker Desktop.exe"
if not defined DOCKER_DESKTOP if exist "%ProgramW6432%\Docker\Docker\Docker Desktop.exe" set "DOCKER_DESKTOP=%ProgramW6432%\Docker\Docker\Docker Desktop.exe"
if not defined DOCKER_DESKTOP (
    echo.
    echo [ERROR] Docker engine is not running and "Docker Desktop.exe" was not found.
    echo         Start Docker Desktop manually, wait for "Engine running", then retry.
    pause
    exit /b 1
)

echo       engine not running - launching Docker Desktop...
start "" "%DOCKER_DESKTOP%"

set /a WAITED=0
:wait_engine
ping -n 4 127.0.0.1 >nul 2>&1
set /a WAITED+=3
docker info >nul 2>&1
if !errorlevel! equ 0 goto :engine_ready
if !WAITED! geq 180 (
    echo.
    echo [ERROR] Docker engine did not become ready within 180s.
    echo         Open Docker Desktop, wait for "Engine running", then retry.
    pause
    exit /b 1
)
echo       waiting for engine ... !WAITED!s
goto :wait_engine

:engine_ready
echo       engine ready.

REM ---------------------------------------------------------------------
REM 3. Check and auto-repair .env
REM
REM    Two failure modes this fixes:
REM      a) JWT_SECRET / RABBITMQ_PASSWORD are empty
REM         -> docker compose aborts with
REM            "required variable XXX is missing a value" and never starts.
REM      b) MYSQL_ROOT_PASSWORD / MYSQL_PASSWORD reverted to change-me-*
REM         -> MySQL refuses to connect: the data volume was initialised with a
REM            DIFFERENT password, so the healthcheck keeps failing and
REM            --wait finally times out with a vague "startup failed".
REM
REM    Rule: ONLY "empty" or "change-me*" entries are touched, everything else
REM    is left alone. Real values are read back from the existing containers,
REM    because those are the values that match the existing data volumes.
REM ---------------------------------------------------------------------
echo.
echo [3/6] Checking .env...
if not exist ".env" (
    if not exist ".env.example" (
        echo [ERROR] .env is missing and .env.example was not found.
        pause
        exit /b 1
    )
    copy ".env.example" ".env" >nul
    echo       created .env from .env.example
)

set "ENV_FIXED="
for /f "usebackq delims=" %%i in (`powershell -NoProfile -ExecutionPolicy Bypass -Command "$f='.env';$L=[IO.File]::ReadAllLines($f);$C=@{};foreach($s in @('synpharm-mysql','synpharm-rabbitmq','synpharm-backend')){$j=docker inspect $s --format '{{json .Config.Env}}' 2>$null;if($j){$C[$s]=@{};foreach($i in ($j|ConvertFrom-Json)){$q=$i -split '=',2;$C[$s][$q[0]]=$q[1]}}};$M=@('MYSQL_ROOT_PASSWORD:synpharm-mysql:MYSQL_ROOT_PASSWORD','MYSQL_PASSWORD:synpharm-mysql:MYSQL_PASSWORD','MYSQL_DATABASE:synpharm-mysql:MYSQL_DATABASE','MYSQL_USER:synpharm-mysql:MYSQL_USER','RABBITMQ_USER:synpharm-rabbitmq:RABBITMQ_DEFAULT_USER','RABBITMQ_PASSWORD:synpharm-rabbitmq:RABBITMQ_DEFAULT_PASS','JWT_SECRET:synpharm-backend:JWT_SECRET');$n=0;foreach($e in $M){$p=$e -split ':';$k=$p[0];$s=$p[1];$ck=$p[2];for($i=0;$i -lt $L.Count;$i++){$r=$L[$i] -split '=',2;if($r.Count -lt 2){continue};if($r[0].Trim() -ne $k){continue};$v=$r[1].Trim();if($v -ne '' -and $v -notlike 'change-me*'){break};$nv='';if($C.ContainsKey($s) -and $C[$s].ContainsKey($ck)){$nv=[string]$C[$s][$ck]}elseif($k -eq 'JWT_SECRET' -or $k -eq 'RABBITMQ_PASSWORD'){$b=New-Object byte[] 32;([Security.Cryptography.RandomNumberGenerator]::Create()).GetBytes($b);$nv=[BitConverter]::ToString($b).Replace('-','')};if($nv -eq ''){break};$L[$i]=$k+'='+$nv;$n=$n+1;break}};if($n -gt 0){[IO.File]::WriteAllLines($f,$L,(New-Object Text.UTF8Encoding($false)))};Write-Output $n"`) do set "ENV_FIXED=%%i"

if not defined ENV_FIXED (
    echo [ERROR] failed to inspect .env - PowerShell call returned nothing.
    pause
    exit /b 1
)
if not "%ENV_FIXED%"=="0" (
    echo       repaired %ENV_FIXED% credential entries that were empty or change-me placeholders
    echo       values were taken from the existing containers to match the data volumes
    echo       existing logins are invalidated - just sign in again
)
echo       .env ok.

REM ---------------------------------------------------------------------
REM Read the host ports out of .env (used by the preflight step and the summary)
REM ---------------------------------------------------------------------
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

REM ---------------------------------------------------------------------
REM 4. Host port preflight
REM    Skipped when containers are already running: the ports would be reported
REM    as "busy" because our own containers hold them. `up -d` is idempotent and
REM    will start whatever is still missing.
REM ---------------------------------------------------------------------
set "RUNNING_COUNT=0"
for /f %%n in ('docker compose ps -q 2^>nul ^| find /c /v ""') do set "RUNNING_COUNT=%%n"

if not "%RUNNING_COUNT%"=="0" goto :skip_preflight

echo.
echo [4/6] Checking host ports...
powershell -NoProfile -ExecutionPolicy Bypass -File "scripts\preflight.ps1" -Ports %FRONTEND_PORT%,%BACKEND_PORT%,%FASTAPI_PORT%,%MYSQL_PORT%,%REDIS_PORT% -Names frontend,backend,fastapi,mysql,redis
if !errorlevel! neq 0 (
    echo.
    echo [ERROR] port preflight failed - see the output above.
    pause
    exit /b 1
)
goto :preflight_done

:skip_preflight
echo.
echo [4/6] %RUNNING_COUNT% container^(s^) already running - skipping port check

:preflight_done

REM ---------------------------------------------------------------------
REM 5. Validate the compose configuration
REM ---------------------------------------------------------------------
echo.
echo [5/6] Validating docker-compose config...
docker compose config --quiet
if !errorlevel! neq 0 (
    echo [ERROR] deploy\.env or docker-compose.yml is invalid.
    pause
    exit /b 1
)

REM ---------------------------------------------------------------------
REM 6. Build only when needed, then start
REM    Always passing --build re-runs the heavy layers (pip install torch alone
REM    downloads ~185MB) whenever the build cache has been evicted, which costs
REM    10+ minutes for no reason. Rebuild explicitly after changing source:
REM        scripts\start.bat rebuild
REM ---------------------------------------------------------------------
set "DO_BUILD="
if /i "%~1"=="rebuild" set "DO_BUILD=--build"
docker image inspect synpharm/backend:1.0.0 >nul 2>&1 || set "DO_BUILD=--build"
docker image inspect synpharm/fastapi:1.0.0 >nul 2>&1 || set "DO_BUILD=--build"
docker image inspect synpharm/frontend:1.0.0 >nul 2>&1 || set "DO_BUILD=--build"

echo.
if defined DO_BUILD (
    echo [6/6] Building images and starting services:
    echo       first build pulls images and compiles, roughly 10-30 minutes.
) else (
    echo [6/6] Starting services - images already built, no rebuild needed.
    echo       after changing source code run:  scripts\start.bat rebuild
)
docker compose up -d %DO_BUILD% --wait --wait-timeout 600
if !errorlevel! neq 0 (
    echo.
    echo [ERROR] startup failed. Common causes:
    echo         1^) image pull blocked    -^> check the Docker registry mirrors
    echo         2^) port cannot be bound  -^> run scripts\preflight.ps1 manually
    echo         3^) application error     -^> docker compose logs backend
    pause
    exit /b 1
)

REM ---------------------------------------------------------------------
REM Summary
REM ---------------------------------------------------------------------
set "FRONTEND_URL=http://localhost"
if not "%FRONTEND_PORT%"=="80" set "FRONTEND_URL=http://localhost:%FRONTEND_PORT%"

echo.
echo Services:
docker compose ps
echo.
echo ==========================================
echo   Started. Opening the browser...
echo   ----------------------------------------
echo   Frontend:      %FRONTEND_URL%
echo   Backend API:   http://localhost:%BACKEND_PORT%/doc.html
echo   FastAPI docs:  http://localhost:%FASTAPI_PORT%/docs
echo   RabbitMQ:      http://localhost:15672
echo   Health check:  http://localhost:%BACKEND_PORT%/actuator/health
echo   ----------------------------------------
echo   Stop:          scripts\stop.bat
echo   Logs:          docker compose logs -f
echo   Force rebuild: scripts\start.bat rebuild
echo ==========================================
ping -n 3 127.0.0.1 >nul 2>&1
start "" "%FRONTEND_URL%"
endlocal
