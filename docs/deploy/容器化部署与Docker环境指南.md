# SynPharm 容器化部署与 Docker 环境指南

> 版本：v2.0（企业级容器化重构）
> 更新日期：2026-07-31
> 适用范围：开发 / 测试 / 组员协作环境

---

## 1. 背景与目标

本项目早期将 Docker 相关文件散落在多个位置（根目录 `docker/`、各服务目录、多个硬编码配置），导致：

- 组员 clone 后无法一键启动；
- 前端未容器化（compose 中缺失 frontend 服务）；
- 密钥/密码硬编码在配置与编排文件中，存在安全隐患；
- FastAPI 存在致命导入错误，容器即使构建成功也无法启动；
- 健康检查配置无效（使用了不存在的 `curl` 与错误的探测端点）。

本重构按**企业级开发范式**对全部 Docker/环境文件进行了统一治理，目标：

| 目标 | 落地方式 |
|---|---|
| 干净隔离 | 每个服务独立容器，互不污染宿主机 |
| 组员可复现 | 一个命令一键启动，环境变量模板化 |
| 可配置 | 全部敏感配置收口到 `deploy/.env` |
| 可运维 | Actuator/curl 健康检查、日志、数据卷 |
| 安全 | 非 root 运行、多阶段构建、密钥不入库 |

---

## 2. 架构总览

```
┌────────────────────────────────────────────────────────────┐
│                      宿主机 / 组员机器                       │
│                                                            │
│  浏览器 ──▶ http://localhost:80                            │
│                │                                           │
│   frontend (nginx-unprivileged, 非root)                    │
│     │  /api/* 反代                                          │
│     ▼                                                      │
│   backend (SpringBoot, JRE17, 非root)  ──▶ MySQL 8         │
│     │  调用 /v1/predict  (X-API-Key)                       │
│     ▼                           │                          │
│   fastapi (Python3.11, 非root)   │                          │
│     │                            └──▶ Redis 7             │
│     └── 模型文件挂载 ./models                               │
└────────────────────────────────────────────────────────────┘
```

> 端口均可通过 `deploy/.env` 的 `*_PORT` 修改；括号内为本项目当前 `.env` 实际值。

| 服务 | 镜像基础 | 端口(宿主) | 健康检查 |
|---|---|---|---|
| mysql | mysql:8.0 | 3306（当前 13307） | mysqladmin ping |
| redis | redis:7-alpine | 6379（当前 6380） | redis-cli ping |
| rabbitmq | rabbitmq:3.13-management-alpine | 15672(仅内网) | rabbitmq-diagnostics ping |
| backend | eclipse-temurin:17-jre | 8080（当前 7000） | `/actuator/health` |
| fastapi | python:3.11-slim | 8000（当前 9000） | `/health` |
| frontend | nginx-unprivileged:1.27 | 80 | `GET /` |

---

## 3. 目录结构（重构后）

```
SynPharm/
├── deploy/                          ★ 全部部署/环境文件统一入口
│   ├── docker-compose.yml           # 服务编排（6 个服务，含 RabbitMQ）
│   ├── .env.example                 # 环境变量模板（复制为 .env）
│   ├── scripts/
│   │   ├── start.bat / start.sh     # 一键启动（跨平台）
│   │   └── stop.bat / stop.sh       # 一键停止
│   └── 环境配置说明.md               # 环境变量与快速开始说明
│
├── synpharm-backend/
│   ├── Dockerfile                   # 多阶段：Maven17 构建 → JRE17 运行
│   ├── .dockerignore
│   └── src/main/resources/application-docker.yml   # 全部走环境变量
│
├── synpharm-fastapi/
│   ├── Dockerfile                   # 多阶段：pip 依赖 → 精简运行镜像
│   ├── .dockerignore
│   ├── config.py                    # pydantic-settings，支持环境变量
│   └── models/                      # 模型文件（不进镜像，卷挂载）
│
├── synpharm-frontend/
│   ├── Dockerfile                   # 多阶段：node20 构建 → nginx 运行
│   ├── .dockerignore
│   └── nginx.conf                   # SPA 路由 + /api 反代后端
│
└── docs/deploy/                     # 本文档
```

**设计决策说明：**

| 决策 | 理由 |
|---|---|
| Dockerfile 跟随各服务源码 | 构建上下文干净、Dockerfile 与代码同版本同分支（业界主流） |
| 编排/配置/脚本集中在 `deploy/` | "运行描述"与"代码"分离，组员只需关注一个入口 |
| 前端使用 nginx-unprivileged | 非 root 运行 Nginx，符合最小权限原则 |
| 模型文件不进镜像 | 模型体积大且更新频繁，统一卷挂载便于替换 |

---

## 4. 环境准备（组员）

### 4.1 安装 Docker

**Windows（二选一）：**

- 方案 A：安装 **Docker Desktop**（官方，需 WSL2 + 虚拟化开启）
- 方案 B：在 WSL2 中安装 Ubuntu 后安装 Docker Engine：
  ```bash
  sudo apt update && sudo apt install -y docker.io docker-compose-v2
  sudo systemctl enable --now docker
  ```

**Linux / macOS：** 直接安装 Docker Engine 或 Docker Desktop。

验证：`docker version` 与 `docker compose version` 均正常输出。

### 4.2 获取代码

```bash
git clone <仓库地址> SynPharm
cd SynPharm/deploy
```

---

## 5. 快速开始

### 5.1 启动

```bash
cd deploy

# Windows：直接双击，或在终端执行
scripts\start.bat

# Linux / macOS / WSL
chmod +x scripts/start.sh && ./scripts/start.sh
```

**无需手动创建 `.env`。** `start.bat` 会自动：

- 定位 `docker` 命令（PATH 未刷新的旧终端也能用）
- 未运行时拉起 Docker Desktop 并等待就绪
- 创建并修复 `.env`：**只补空值和 `change-me*` 占位值，不会覆盖你自己设过的密码**
- 端口预检 → 校验配置 → 启动（已有容器时跳过端口检查）

### 5.2 耗时

| 场景 | 耗时 |
|---|---|
| 首次（无镜像） | 10~30 分钟：拉基础镜像 + Maven 编译 + 装 torch（约 2~3 GB） |
| 之后（镜像已存在） | 十几秒：跳过构建，只 `up -d` |

RabbitMQ / MySQL / Redis 使用官方镜像（无需构建），随 compose 一起启动。

改了源码需要重建时，执行：

```bash
scripts\start.bat rebuild
```

> ⚠️ **不要把 `.env.example` 复制覆盖到已有的 `.env` 上**：数据卷里的密码是首次初始化时写死的，
> 覆盖后密码对不上，之后所有启动都会失败。详见 `deploy/环境配置说明.md` 的
> “启动失败的三大真实原因”。

### 5.3 验证

```bash
docker compose ps                            # 全部 healthy
curl http://localhost:7000/actuator/health   # {"status":"UP"}
curl http://localhost:9050/health/           # {"status":"healthy"}
```

访问：
- 前端：http://localhost
- 后端文档：http://localhost:7000/doc.html
- FastAPI 文档：http://localhost:9050/docs

---

## 6. 环境变量参考（.env）

| 变量 | 默认 | 说明 |
|---|---|---|
| `SYNPHARM_VERSION` | 1.0.0 | 镜像 tag |
| `MYSQL_ROOT_PASSWORD` | - | **必填**，MySQL root 密码 |
| `MYSQL_DATABASE` | synpharm | 库名 |
| `MYSQL_USER` / `MYSQL_PASSWORD` | synpharm / - | 应用账号（**密码必填**） |
| `MYSQL_PORT` | 13307 | 宿主映射端口（3307-3406 属 Windows/Hyper-V 保留段，不可用） |
| `REDIS_PORT` | 6380 | 宿主映射端口（避开本机已有的 6379） |
| `REDIS_PASSWORD` | 空 | 生产建议设置 |
| `RABBITMQ_USER` | synpharm | RabbitMQ 用户名（**密码必填**） |
| `RABBITMQ_PASSWORD` | - | **必填**，RabbitMQ 密码 |
| `JWT_SECRET` | - | **必填**，≥32 字节随机串 |
| `JWT_EXPIRATION` | 86400000 | Token 有效期(ms) |
| `FASTAPI_PORT` | 8000 | 宿主映射端口 |
| `FASTAPI_DEVICE` | cpu | cpu / cuda:0 |
| `FASTAPI_BATCH_SIZE` | 50 | 批量推理大小 |
| `FASTAPI_API_KEYS` | 空 | 逗号分隔 API Key；空=关闭认证（仅内网） |
| `FASTAPI_LOG_LEVEL` | INFO | 日志级别 |
| `BACKEND_PORT` | 8080 | 宿主映射端口 |
| `FRONTEND_PORT` | 80 | 宿主映射端口 |
| `QQ_EMAIL` / `QQ_EMAIL_AUTH_CODE` | 空 | 验证码邮件（可选） |

---

## 7. 镜像构建（企业级要点）

### 7.1 多阶段构建

| 服务 | 构建阶段 | 运行阶段 |
|---|---|---|
| backend | `maven:3.9.6-eclipse-temurin-17` → 打包 jar | `eclipse-temurin:17-jre` |
| fastapi | `python:3.11-slim` → pip `--prefix=/install` | `python:3.11-slim` 拷贝依赖产物 |
| frontend | `node:20-alpine` → `npm ci && npm run build` | `nginxinc/nginx-unprivileged:1.27-alpine` |

运行镜像中**不包含**编译工具链（Maven/npm/pip 缓存），显著减小体积并减少攻击面。

### 7.2 非 root 运行

三个应用镜像均创建 `appuser`（uid 10001）并以 `USER appuser` 运行：

```dockerfile
RUN useradd --system --uid 10001 --create-home appuser \
    && chown -R appuser:appuser /app
USER appuser
```

### 7.3 .dockerignore

各服务目录均配置 `.dockerignore`，排除 `target/`、`node_modules/`、`__pycache__/`、`venv/`、日志、`.env` 等，防止构建上下文臃肿与敏感信息泄漏。

---

## 8. 健康检查设计

| 服务 | 探测命令 | 说明 |
|---|---|---|
| mysql | `mysqladmin ping` | 官方客户端，可靠 |
| redis | `redis-cli ping` | 官方客户端 |
| backend | `curl -fsS /actuator/health` | 依赖 spring-boot-starter-actuator（pom 已加） |
| fastapi | `curl -fsS /health` | 应用内建健康端点 |
| frontend | `wget -qO- /` | nginx-alpine 自带 wget |

> 重构修复了原配置中"用 curl 探测 POST 接口 + 运行镜像无 curl"的失效问题；后端与 FastAPI 运行镜像均显式安装了 `curl`。

---

## 9. 数据持久化

| 数据 | 卷/挂载 |
|---|---|
| MySQL 数据 | `mysql_data`（命名卷） |
| Redis 数据 | `redis_data`（命名卷） |
| 后端上传文件 | `backend_uploads:/app/uploads` |
| 后端结果文件 | `backend_results:/app/results` |
| FastAPI 模型 | `../synpharm-fastapi/models:/app/models:ro`（只读挂载） |
| FastAPI 日志 | `fastapi_logs:/app/logs` |

数据库建表由 MySQL 首次初始化自动执行 `synpharm-backend/sql/` 下脚本（挂载到 `/docker-entrypoint-initdb.d`）。

---

## 10. 常见问题排查

| 症状 | 排查步骤 |
|---|---|
| 后端一直 unhealthy | `docker compose logs backend`；检查 DB 密码/JWT_SECRET 是否正确 |
| FastAPI 一直 unhealthy | `docker compose logs fastapi`；首次构建安装 torch 较慢，耐心等待 |
| 端口被占用 | 修改 `.env` 对应端口后重启 |
| 前端打不开 | 确认 `FRONTEND_PORT`、后端是否 healthy；`docker compose ps` |
| MySQL 初始化脚本未执行 | 删除 `mysql_data` 卷后重启：`docker compose down -v && scripts/start` |
| 构建报 `failed to fetch anonymous token` / `auth.docker.io:443` 超时 | 见下方「受限网络下拉取镜像」 |
| 容器启动报 `bind: ... forbidden by its access permissions` | 见下方「Windows 保留端口段」 |

### 10.1 受限网络下拉取镜像失败

**症状**：`docker compose up --build` 在 `FROM` 阶段就失败，报
`failed to fetch anonymous token` 或 `dial tcp ...:443: connectex: ... did not properly respond`。
此时应用代码根本没被执行，是基础镜像没拉下来。

**原因**：Docker Desktop **不会**自动读取 Windows 的系统代理设置。
当本机无法直连 Docker Hub 时，引擎只能裸连，必然超时。

**三种解法，任选其一：**

1. **配置镜像加速源（推荐，不依赖代理）**

   编辑 `%USERPROFILE%\.docker\daemon.json`（Linux/macOS 为 `/etc/docker/daemon.json`）：

   ```json
   {
     "registry-mirrors": [
       "https://docker.m.daocloud.io",
       "https://docker.1ms.run",
       "https://docker.1panel.live"
     ]
   }
   ```

   然后重启引擎：Windows 用 `docker desktop restart`，Linux 用 `sudo systemctl restart docker`。

   验证：

   ```bash
   docker info --format "{{json .RegistryConfig}}"   # 应能看到 Mirrors 列表
   docker pull alpine:latest                          # 用一个未缓存的镜像验证
   ```

2. **给 Docker 配置代理**（需自备可用代理）
   Docker Desktop → Settings → Resources → Proxies 填代理地址。
   ⚠️ 引擎运行在 WSL2 虚拟机内，代理地址必须写 `http://host.docker.internal:<端口>`，
   **不能写 `127.0.0.1`**（在 VM 里它指向 VM 自己）。

3. **换到可直连 Docker Hub 的网络**后重试。

> Python 依赖同样需要国内源：`synpharm-fastapi/Dockerfile` 默认使用阿里云 PyPI 与
> PyTorch CPU wheel 镜像，如需改回官方源，可用 `--build-arg` 覆盖
> `PIP_INDEX_URL` / `PYTORCH_WHEELS_URL`。

### 10.2 Windows 保留端口段导致容器无法启动

**症状**：镜像构建成功，但容器启动时报

```text
Error response from daemon: ports are not available: exposing port TCP 0.0.0.0:3307 -> ...:
listen tcp 0.0.0.0:3307: bind: An attempt was made to access a socket in a way forbidden by its access permissions.
```

**原因**：Windows 会把成段的端口保留给 Hyper-V / WSL，落在保留段内的端口任何进程都无法绑定。

**处理**：

```powershell
# 查看当前保留端口段
netsh interface ipv4 show excludedportrange protocol=tcp

# 用仓库自带的前置检查脚本，自动判断 .env 中的端口是否可用
powershell -NoProfile -File scripts\preflight.ps1 -Ports 80,7000,9050,13307,6380 -Names frontend,backend,fastapi,mysql,redis
```

把 `.env` 中冲突的 `*_PORT` 改到保留段之外再重启即可
（本项目 MySQL 已因此从 3307 改用 13307）。

---

## 11. 安全基线（生产部署必读）

- [ ] `.env` 使用强随机密码与 32 字节以上 JWT 密钥，且不提交版本库
- [ ] 开启 FastAPI 认证：`.env` 设置 `FASTAPI_API_KEYS=key1,key2`，后端自动携带 `X-API-Key`
- [ ] 生产环境不将 MySQL/Redis 端口暴露到公网（仅内网或 localhost）
- [ ] 生产环境建议关闭 Swagger/Knife4j 文档暴露
- [ ] 移除/禁用调试后门接口 `POST /api/auth/debug/login`
- [ ] 定期更新基础镜像与依赖版本

---

## 12. CI/CD 建议（后续演进）

当前实现"本地构建 + compose 启动"，已满足组员协作。更进一步的企业化方向：

1. **镜像仓库**：GitHub Actions / GitLab CI 在推送时构建并推送 `synpharm/backend|fastapi|frontend:<tag>` 到仓库；
2. **组员拉取**：`docker compose pull` 即可，无需本地构建（避免版本漂移）；
3. **环境分层**：`docker-compose.override.yml`（开发热重载）与生产 compose 分离；
4. **编排升级**：规模扩大后迁移 Kubernetes（现有 `deploy/` 可演进为 `deploy/k8s/`）。
