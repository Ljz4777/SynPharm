<div align="center">

# SynPharm · 智互药研

### AI 驱动的药物相互作用预测平台

[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://adoptium.net/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2-green.svg)](https://spring.io/projects/spring-boot)
[![FastAPI](https://img.shields.io/badge/FastAPI-0.104-teal.svg)](https://fastapi.tiangolo.com/)
[![Vue](https://img.shields.io/badge/Vue-3.5-brightgreen.svg)](https://vuejs.org/)
[![TypeScript](https://img.shields.io/badge/TypeScript-5.6-blue.svg)](https://www.typescriptlang.org/)
[![RabbitMQ](https://img.shields.io/badge/RabbitMQ-3.13-orange.svg)](https://www.rabbitmq.com/)
[![Docker](https://img.shields.io/badge/Docker-Compose-blue.svg)](https://www.docker.com/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

**DTI · PPI · DDI 三大药物相互作用预测 · RabbitMQ 异步批处理 · Docker 一键部署**

</div>

---

## 📖 项目简介

**SynPharm（智互药研）** 是一个基于 AI 的药物相互作用预测平台，支持 **药物-靶点相互作用（DTI）**、**蛋白质-蛋白质相互作用（PPI）** 和 **药物-药物相互作用（DDI）** 三种预测类型。

平台采用 **SpringBoot 业务中台 + FastAPI 算法引擎 + Vue3 前端** 的三端分离架构，提供完整的用户认证体系、单条实时预测、**RabbitMQ 消息队列驱动的批量预测**、结果与任务管理、3D 可视化与靶点库等功能，适用于药物研发辅助、学术研究和教学演示场景。

### ✨ 核心特性

- 🔬 **三大预测类型** — 支持 DTI / PPI / DDI，单条实时预测 + 批量 CSV 预测
- 📨 **RabbitMQ 异步批处理** — 消息持久化、死信队列、幂等消费、任务状态以 DB 为权威
- 🔐 **多策略用户认证** — 邮箱验证码 / 密码 / 游客登录，JWT 无状态认证、登录限流、Token 黑名单
- 🧬 **3D 可视化** — 交互式分子结构展示（卡通 / 球体 / 棍状 / 表面）
- 🎯 **靶点库** — 按靶点类型 / 蛋白家族 / 主要通路 / 疾病领域精细分类
- 🚀 **Docker Compose 一键部署** — MySQL / Redis / RabbitMQ / 后端 / FastAPI / 前端 6 服务

---

## 🛠️ 技术栈

### 后端业务中台（synpharm-backend）

| 技术 | 版本 | 用途 |
| :--- | :--- | :--- |
| Java | 17 | 开发语言 |
| Spring Boot | 3.2.x | 应用框架 |
| Spring Security | 6.x | 安全认证 |
| MyBatis-Plus | 3.5.x | ORM 框架 |
| MySQL | 8.0 | 关系型数据库 |
| Redis | 7.x | 缓存 / 验证码 / 限流 / 进度缓存 |
| **RabbitMQ** | 3.13 | 批量任务消息队列（死信/幂等/手动 ack） |
| JWT (jjwt) | 0.12.x | 无状态令牌认证 |
| Knife4j | 4.x | API 文档 |

### 算法引擎（synpharm-fastapi）

| 技术 | 版本 | 用途 |
| :--- | :--- | :--- |
| Python | 3.11 | 算法引擎语言 |
| FastAPI | 0.104.x | AI 推理服务框架 |
| PyTorch | 2.x | 深度学习推理 |
| Pydantic | 2.x | 数据校验 |

### 前端（synpharm-frontend）

| 技术 | 版本 | 用途 |
| :--- | :--- | :--- |
| Vue | 3.5.x | 前端框架 |
| TypeScript | 5.6.x | 类型系统 |
| Vite | 5.x | 构建工具 |
| Pinia | 2.x | 状态管理 |
| Vue Router | 4.x | 路由管理 |
| Element Plus | 2.x | UI 组件库 |
| Axios | 1.x | HTTP 客户端 |

---

## 🏗️ 整体架构

```
┌────────────┐      ┌──────────────────────────────────────────────┐
│  浏览器前端  │ ───▶ │               Nginx 反向代理                  │
│  Vue3 SPA  │ HTTP │   静态资源 + /api 反代 + HTTPS 终止            │
└────────────┘      └──────────────────────┬───────────────────────┘
                                           ▼
┌───────────────────────────────────────────────────────────────────┐
│                 SpringBoot 业务中台（端口默认 8080）                │
│  认证/用户 │ 预测(管道) │ 结果/任务 │ 批量上传 → RabbitMQ 生产者      │
│                                                                   │
│  ┌─────────┐   ┌─────────────────┐   ┌─────────────────────────┐  │
│  │  MySQL  │   │  RabbitMQ       │   │  FastAPI 算法引擎       │  │
│  │  Redis  │◀─▶│  batch.task.queue│──▶│  /v1/predict/single    │  │
│  │         │   │  batch.task.dlq │   │  /v1/predict/batch     │  │
│  └─────────┘   └─────────────────┘   └─────────────────────────┘  │
└───────────────────────────────────────────────────────────────────┘
```

> 批量预测链路：前端上传 CSV → 后端落库并投递消息 → 消费者异步消费 → 状态/结果落库 → 前端轮询进度 → 下载结果。任务状态以 `batch_task` 表为权威，消费失败进入死信队列。

---

## 📁 项目结构

```
SynPharm/
├── synpharm-backend/              # 后端业务中台（SpringBoot）
│   ├── sql/                       # 数据库初始化脚本（00 建库 ~ 08 批量迁移）
│   └── src/main/java/com/synpharm/
│       ├── SynPharmApplication.java
│       ├── api/                   # 控制器层（Auth/User/Predict/Result/Task/BatchUpload）
│       ├── service/               # 业务逻辑（登录策略、预测、批量）
│       ├── mq/                    # RabbitMQ（RabbitConfig/Producer/Consumer/Message）
│       ├── repository/mapper/     # MyBatis Mapper（含 XML）
│       ├── config/                # SecurityConfig / RabbitConfig 等
│       ├── dto/                   # 请求 / 响应 DTO
│       ├── model/entity/          # 实体类
│       ├── pipeline/              # 预测管道（输入解析/算法执行/输出格式化）
│       ├── exception/ utils/      # 异常与工具
│       └── resources/             # application*.yml + mapper XML
│
├── synpharm-fastapi/              # AI 算法引擎（FastAPI）
│   ├── main.py / config.py        # 入口与配置
│   ├── api/v1/                    # /health、/v1/predict/single、/v1/predict/batch
│   ├── core/                      # auth / loader / schemas / base_algo
│   └── services/                  # dti / ppi / ddi / batch 推理服务
│
├── synpharm-frontend/             # 前端应用（Vue3）
│   └── src/
│       ├── api/                   # 请求层（auth/predict/batch）
│       ├── components/            # 公共组件（Sidebar/ResultCard）
│       ├── router/ stores/        # 路由 / 状态管理
│       ├── types/ utils/          # 类型 / 工具
│       └── views/                 # 页面（Home/Login/Dashboard/Predict/Results/Tasks/Targets/Visualization/Profile）
│
├── deploy/                        # 部署
│   ├── docker-compose.yml         # 6 服务编排（mysql/redis/rabbitmq/backend/fastapi/frontend）
│   ├── .env.example               # 环境变量模板（复制为 .env）
│   └── scripts/                   # 一键启停脚本（start/stop）
│
└── docs/                          # 技术文档（架构/部署/接口/模块）
```

---

## 🚀 快速开始

### 方式一：Docker Compose 一键部署（推荐）

环境要求：**Docker Desktop（WSL2）/ Docker Engine 24+、Docker Compose 2+**

```bash
cd deploy
cp .env.example .env          # 复制环境变量模板
# 编辑 .env：必填 MYSQL_ROOT_PASSWORD、MYSQL_PASSWORD、JWT_SECRET、RABBITMQ_PASSWORD
docker compose up -d --build
```

启动后访问：

| 服务 | 地址 | 说明 |
| :--- | :--- | :--- |
| 前端 | http://localhost | 主站点（端口默认 80，`.env` 可改） |
| 后端 API / Knife4j | http://localhost:8080 | `http://localhost:8080/doc.html` |
| FastAPI | http://localhost:8000 | `http://localhost:8000/docs` |
| RabbitMQ 管理台 | http://localhost:15672 | 仅内网/本机，账号见 `.env` |

> 端口均通过 `deploy/.env` 的 `*_PORT` 变量配置；数据库首次启动自动执行 `sql/` 初始化脚本。

### 方式二：本地开发（可选）

需要先启动 MySQL / Redis / RabbitMQ（可用 Docker 或本地安装），然后：

```bash
# 后端（端口 8080）
cd synpharm-backend
mvn spring-boot:run

# 算法引擎（端口 8000，模型缺失时自动 mock 降级）
cd synpharm-fastapi
pip install -r requirements.txt
uvicorn main:app --host 0.0.0.0 --port 8000

# 前端
cd synpharm-frontend
npm install
npm run dev   # http://localhost:5173
```

---

## ⚙️ 环境变量（.env 关键项）

| 变量 | 必填 | 说明 |
| :--- | :---: | :--- |
| `MYSQL_ROOT_PASSWORD` / `MYSQL_PASSWORD` | ✅ | 数据库密码 |
| `JWT_SECRET` | ✅ | ≥32 字节随机串（`openssl rand -hex 32`） |
| `RABBITMQ_USER` / `RABBITMQ_PASSWORD` | ✅ | 消息队列账号 |
| `QQ_EMAIL` / `QQ_EMAIL_AUTH_CODE` | 生产 | 邮箱验证码（QQ SMTP 授权码）；留空时需 `CAPTCHA_DEV_MODE=true` 才能本地发码 |
| `CAPTCHA_DEV_MODE` | 本地 | `true` 时验证码直接回显（仅限开发）；生产必须 `false` |
| `FASTAPI_API_KEY` / `FASTAPI_API_KEYS` | 生产 | FastAPI 认证密钥 |

---

## 📡 API 接口概览

> 完整契约见 [docs/api/接口文档.md](docs/api/接口文档.md)

### 认证

| 方法 | 路径 | 说明 |
| :--- | :--- | :--- |
| POST | `/api/auth/captcha/send` | 发送邮箱验证码 |
| POST | `/api/auth/register` | 注册 |
| POST | `/api/auth/login` | 登录（qq_email / password / guest） |
| POST | `/api/auth/logout` | 登出 |
| POST | `/api/auth/password/reset` | 重置密码 |

### 预测 / 结果 / 任务

| 方法 | 路径 | 说明 |
| :--- | :--- | :--- |
| POST | `/api/predict/dti` / `ppi` / `ddi` | 单条预测 |
| GET | `/api/predict/history` | 单条预测历史 |
| GET | `/api/results` | 结果列表（分页） |
| GET/DELETE | `/api/results/{id}` | 结果详情 / 删除 |
| GET | `/api/tasks` | 任务列表 |
| GET/DELETE | `/api/tasks/{id}` | 任务详情 / 取消 |

### 批量（RabbitMQ）

| 方法 | 路径 | 说明 |
| :--- | :--- | :--- |
| POST | `/api/batch/upload` | 上传 CSV，落库并投递消息 |
| GET | `/api/batch/status/{batchId}` | 查询进度（归属校验） |
| GET | `/api/batch/download/{batchId}` | 下载结果（归属校验） |

### FastAPI 算法引擎（内网，X-API-Key 认证）

| 方法 | 路径 | 说明 |
| :--- | :--- | :--- |
| GET | `/health` | 健康检查 |
| POST | `/v1/predict/single` | 单条推理 |
| POST | `/v1/predict/batch` | 批量推理 |

---

## 🗄️ 数据库设计

初始化脚本位于 `synpharm-backend/sql/`（`00_create_database.sql` ~ `08_batch_task_alter.sql`）。

| 表名 | 说明 |
| :--- | :--- |
| `sys_user` | 系统用户表（邮箱、密码、登录信息等） |
| `sys_login_log` | 登录日志表 |
| `predict_task` | 单条预测任务表 |
| `predict_result` | 预测结果表 |
| `user_favorite` | 用户收藏表 |
| `batch_task` | 批量任务表（含 `algo_type`、`deleted`，状态 0待处理/1处理中/2成功/3失败） |

---

## 🔐 认证与安全

### 认证模块架构（策略模式）

```
                    ┌─────────────────┐
                    │  AuthController │
                    └────────┬────────┘
                             │
                    ┌────────▼────────┐
                    │  AuthService    │
                    │  (限流/日志)     │
                    └────────┬────────┘
                             │
                    ┌────────▼────────┐
                    │ LoginStrategy   │  ← 接口
                    │    Factory      │
                    └────────┬────────┘
                             │
          ┌──────────────────┼──────────────────┐
          │                  │                  │
┌─────────▼─────────┐ ┌─────▼─────┐  ┌─────────▼─────────┐
│ QQEmailLogin      │ │  手机号    │  │  微信登录          │
│    Strategy       │ │  (待扩展)  │  │  (待扩展)          │
└───────────────────┘ └───────────┘  └───────────────────┘
```

**扩展登录方式**：只需实现 `LoginStrategy` 接口并注册到工厂，无需修改现有代码。

### 安全机制

| 机制 | 说明 |
| :--- | :--- |
| JWT 认证 | 无状态令牌，包含用户 ID 和过期时间 |
| 登录限流 | 5 次失败锁定 15 分钟 |
| Token 黑名单 | 登出后 Token 立即失效 |
| 验证码一次性 | 使用后立即删除，防重放攻击 |
| BCrypt 加密 | 密码加盐哈希存储 |
| 常量时间比较 | 验证码比较防时序攻击 |

---

## 📦 部署

### Docker Compose 部署（推荐）

```bash
cd deploy
cp .env.example .env        # 配置密码 / 密钥 / RabbitMQ
docker compose up -d --build

# 查看状态（6 个服务全部 healthy）
docker compose ps
```

> 改动代码后重建：`docker compose build backend|frontend` 再 `docker compose up -d`。生产部署前请阅读 [上线流程详细版](docs/deploy/上线流程详细版.md)。

### 传统部署（本地）

```bash
# 后端
cd synpharm-backend
mvn clean package -DskipTests
java -jar target/synpharm-backend-1.0.0.jar

# 算法引擎
cd synpharm-fastapi
pip install -r requirements.txt
uvicorn main:app --host 0.0.0.0 --port 8000

# 前端
cd synpharm-frontend
npm run build
# 将 dist/ 部署到 Nginx
```

---

## 📝 开发规范

- **提交规范**：遵循 [Conventional Commits](https://www.conventionalcommits.org/)
  - `feat:` 新功能 / `fix:` 修复 Bug / `docs:` 文档更新 / `refactor:` 重构 / `chore:` 构建
- **分支策略**：`main` 为主分支，开发使用 `dev` 分支，功能使用 `feature/*` 分支
- **代码风格**：后端遵循阿里巴巴 Java 规范，前端遵循 ESLint + Prettier

---

## 📄 文档

| 文档 | 路径 |
| :--- | :--- |
| API 接口文档 | [docs/api/接口文档.md](docs/api/接口文档.md) |
| 部署指南 | [docs/deploy/部署指南.md](docs/deploy/部署指南.md) |
| 容器化部署与 Docker 环境指南 | [docs/deploy/容器化部署与Docker环境指南.md](docs/deploy/容器化部署与Docker环境指南.md) |
| 上线流程详细版 | [docs/deploy/上线流程详细版.md](docs/deploy/上线流程详细版.md) |
| 架构设计文档 | [docs/architecture/架构设计文档.md](docs/architecture/架构设计文档.md) |
| 双后端框架技术设计文档 | [docs/architecture/基于Springboot和FastAPI双后端框架的预测核心模块技术设计文档 .md](docs/architecture/基于Springboot和FastAPI双后端框架的预测核心模块技术设计文档%20.md) |
| 整体架构培训文档 | [docs/architecture/SynPharm项目整体架构培训文档.md](docs/architecture/SynPharm项目整体架构培训文档.md) |
| 用户认证模块技术设计文档 | [docs/modules/auth/用户认证模块技术设计文档.md](docs/modules/auth/用户认证模块技术设计文档.md) |
| SpringBoot 预测核心模块开发文档 | [docs/modules/predict/AI预测核心模块-SpringBoot后端技术开发文档.md](docs/modules/predict/AI预测核心模块-SpringBoot后端技术开发文档.md) |
| FastAPI 算法引擎开发文档 | [docs/modules/predict/AI预测核心模块-FastAPI算法引擎技术开发文档.md](docs/modules/predict/AI预测核心模块-FastAPI算法引擎技术开发文档.md) |
| 批量处理技术设计文档（RabbitMQ） | [docs/modules/predict/批量处理技术设计文档.md](docs/modules/predict/批量处理技术设计文档.md) |

---

## 🤝 贡献

欢迎提交 Issue 和 Pull Request！

1. Fork 本仓库
2. 创建功能分支：`git checkout -b feature/AmazingFeature`
3. 提交更改：`git commit -m 'feat: add some AmazingFeature'`
4. 推送分支：`git push origin feature/AmazingFeature`
5. 提交 Pull Request

---

## 📜 License

本项目采用 [MIT License](LICENSE) 开源协议。

---

## 🙏 致谢

- [Spring Boot](https://spring.io/projects/spring-boot) — 后端框架
- [FastAPI](https://fastapi.tiangolo.com/) — AI 算法引擎
- [Vue.js](https://vuejs.org/) — 前端框架
- [Element Plus](https://element-plus.org/) — UI 组件库
- [RabbitMQ](https://www.rabbitmq.com/) — 消息队列
- [MyBatis-Plus](https://baomidou.com/) — MyBatis 增强工具
- [Knife4j](https://doc.xiaominfo.com/) — API 文档增强

---

<div align="center">

**⭐ 如果这个项目对你有帮助，请点个 Star！**

Made with ❤️ by SynPharm Team

</div>
