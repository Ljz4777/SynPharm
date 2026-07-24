<div align="center">

# SynPharm · 智互药研

### AI 驱动的药物相互作用预测平台

[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://adoptium.net/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2-green.svg)](https://spring.io/projects/spring-boot)
[![Vue](https://img.shields.io/badge/Vue-3.5-brightgreen.svg)](https://vuejs.org/)
[![TypeScript](https://img.shields.io/badge/TypeScript-5.6-blue.svg)](https://www.typescriptlang.org/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

**DTI · PPI · DDI 三大药物相互作用预测 · 开箱即用的前后端一体化方案**

</div>

---

## 📖 项目简介

**SynPharm（智互药研）** 是一个基于 AI 的药物相互作用预测平台，支持 **药物-靶点相互作用（DTI）**、**蛋白质-蛋白质相互作用（PPI）** 和 **药物-药物相互作用（DDI）** 三种预测类型。平台采用前后端分离架构，提供完整的用户认证体系、任务管理和结果可视化功能，适用于药物研发辅助、学术研究和教学演示场景。

### ✨ 核心特性

- 🔐 **安全的用户认证** — QQ 邮箱验证码登录，策略模式可扩展多种登录方式
- 🧬 **多类型预测** — 支持 DTI / PPI / DDI 三种药物相互作用预测
- 📊 **任务管理** — 异步任务提交、状态追踪、结果查询
- 🛡️ **企业级安全** — JWT 无状态认证、登录限流、Token 黑名单
- 🎨 **现代化 UI** — Vue 3 + Element Plus 响应式界面
- 📱 **前后端分离** — RESTful API，前后端独立部署

---

## 🛠️ 技术栈

### 后端

| 技术 | 版本 | 用途 |
| :--- | :--- | :--- |
| Java | 17 | 开发语言 |
| Spring Boot | 3.2.x | 应用框架 |
| Spring Security | 6.x | 安全认证 |
| MyBatis-Plus | 3.5.x | ORM 框架 |
| MySQL | 8.0 | 关系型数据库 |
| Redis | 6.x+ | 缓存 / 验证码 / 限流 |
| JWT (jjwt) | 0.12.x | 无状态令牌认证 |
| Knife4j | 4.x | API 文档 |
| Maven | 3.9+ | 依赖管理 |

### 前端

| 技术 | 版本 | 用途 |
| :--- | :--- | :--- |
| Vue | 3.5.x | 前端框架 |
| TypeScript | 5.6.x | 类型系统 |
| Vite | 5.x | 构建工具 |
| Pinia | 2.x | 状态管理 |
| Vue Router | 4.x | 路由管理 |
| Element Plus | 2.x | UI 组件库 |
| Axios | 1.x | HTTP 客户端 |
| SCSS | - | 样式预处理 |

---

## 📁 项目结构

```
SynPharm/
├── synpharm-backend/              # 后端服务
│   ├── src/main/java/com/synpharm/
│   │   ├── SynPharmApplication.java          # 启动类
│   │   ├── api/                              # 控制器层
│   │   │   ├── AuthController.java           #   认证接口
│   │   │   ├── UserController.java           #   用户接口
│   │   │   ├── PredictController.java        #   预测接口
│   │   │   ├── TaskController.java           #   任务接口
│   │   │   └── ResultController.java         #   结果接口
│   │   ├── service/                          # 业务逻辑层
│   │   │   ├── strategy/                     #   策略模式（登录方式）
│   │   │   │   ├── LoginStrategyFactory.java
│   │   │   │   └── QQEmailLoginStrategy.java
│   │   │   ├── impl/                         #   服务实现
│   │   │   ├── LoginStrategy.java            #   登录策略接口
│   │   │   ├── CaptchaService.java           #   验证码服务接口
│   │   │   └── NotifyService.java            #   通知服务接口
│   │   ├── repository/                       # 数据访问层
│   │   │   └── mapper/                       #   MyBatis Mapper
│   │   ├── config/                           # 配置类
│   │   │   ├── SecurityConfig.java           #   Spring Security
│   │   │   └── JwtAuthenticationFilter.java  #   JWT 过滤器
│   │   ├── dto/                              # 数据传输对象
│   │   │   ├── request/                      #   请求 DTO
│   │   │   └── response/                     #   响应 DTO
│   │   ├── model/                            # 数据模型
│   │   │   ├── entity/                       #   实体类
│   │   │   └── enums/                        #   枚举类
│   │   ├── exception/                        # 异常处理
│   │   ├── utils/                            # 工具类
│   │   └── validation/                       # 验证组件
│   ├── src/main/resources/
│   │   ├── application.yml                   # 主配置
│   │   ├── application-dev.yml               # 开发环境配置
│   │   └── mapper/                           # MyBatis XML
│   ├── sql/init.sql                          # 数据库初始化脚本
│   └── pom.xml                               # Maven 依赖
│
├── synpharm-frontend/              # 前端应用
│   ├── src/
│   │   ├── api/                              # API 请求层
│   │   ├── assets/                           # 静态资源
│   │   ├── components/                       # 公共组件
│   │   ├── router/                           # 路由配置
│   │   ├── stores/                           # Pinia 状态管理
│   │   ├── types/                            # TypeScript 类型
│   │   ├── utils/                            # 工具函数
│   │   └── views/                            # 页面组件
│   │       ├── Login.vue                     #   登录/注册
│   │       ├── Home.vue                      #   首页
│   │       ├── Predict.vue                   #   预测页
│   │       ├── History.vue                   #   历史记录
│   │       └── Profile.vue                   #   个人中心
│   ├── vite.config.ts                        # Vite 配置
│   └── package.json                          # npm 依赖
│
└── README.md                       # 项目说明（本文件）
```

---

## 🚀 快速开始

### 环境要求

| 软件 | 最低版本 | 说明 |
| :--- | :--- | :--- |
| JDK | 17 | 推荐 Eclipse Temurin |
| Maven | 3.9+ | 后端依赖管理 |
| Node.js | 18+ | 推荐 20 LTS |
| npm | 9+ | 前端依赖管理 |
| MySQL | 8.0 | 数据库 |
| Redis | 6.x+ | 缓存服务 |

### 1️⃣ 克隆仓库

```bash
git clone https://github.com/你的用户名/SynPharm.git
cd SynPharm
```

### 2️⃣ 初始化数据库

```bash
# 登录 MySQL
mysql -u root -p

# 执行初始化脚本
source synpharm-backend/sql/init.sql;
```

### 3️⃣ 配置后端环境变量

在后端配置中设置以下环境变量（或直接修改 `application-dev.yml`）：

```yaml
# 数据库
DB_PASSWORD=你的数据库密码

# JWT 密钥（至少 32 字节）
JWT_SECRET=你的JWT密钥字符串至少32字节长度

# 邮件服务
MAIL_USERNAME=你的@qq.com
MAIL_PASSWORD=你的QQ邮箱授权码
```

### 4️⃣ 启动后端

```bash
cd synpharm-backend
mvn spring-boot:run
```

后端默认运行在 `http://localhost:8080`

API 文档地址：`http://localhost:8080/doc.html`（Knife4j）

### 5️⃣ 启动前端

```bash
cd synpharm-frontend
npm install
npm run dev
```

前端默认运行在 `http://localhost:5173`

### 6️⃣ 访问应用

浏览器打开 `http://localhost:5173`，使用 QQ 邮箱注册并登录即可。

---

## 📡 API 接口概览

### 认证接口

| 方法 | 路径 | 说明 | 认证 |
| :--- | :--- | :--- | :---: |
| POST | `/api/auth/captcha/send` | 发送邮箱验证码 | ❌ |
| POST | `/api/auth/login` | 登录（登录注册合一） | ❌ |
| POST | `/api/auth/logout` | 登出 | ✅ |

### 用户接口

| 方法 | 路径 | 说明 | 认证 |
| :--- | :--- | :--- | :---: |
| GET | `/api/user/profile` | 获取当前用户信息 | ✅ |
| PUT | `/api/user/profile` | 更新用户信息 | ✅ |

### 预测接口

| 方法 | 路径 | 说明 | 认证 |
| :--- | :--- | :--- | :---: |
| POST | `/api/predict/dti` | DTI 预测 | ✅ |
| POST | `/api/predict/ppi` | PPI 预测 | ✅ |
| POST | `/api/predict/ddi` | DDI 预测 | ✅ |

### 任务接口

| 方法 | 路径 | 说明 | 认证 |
| :--- | :--- | :--- | :---: |
| GET | `/api/tasks` | 获取任务列表 | ✅ |
| GET | `/api/tasks/{id}` | 获取任务详情 | ✅ |
| DELETE | `/api/tasks/{id}` | 取消任务 | ✅ |

> 完整 API 文档请访问 Knife4j：`http://localhost:8080/doc.html`

---

## 🗄️ 数据库设计

### 核心数据表

| 表名 | 说明 |
| :--- | :--- |
| `sys_user` | 系统用户表（邮箱、密码、登录信息等） |
| `sys_login_log` | 登录日志表（登录时间、IP、状态等） |
| `predict_task` | 预测任务表（任务类型、状态、参数等） |
| `predict_result` | 预测结果表（预测得分、置信度等） |

---

## 🏗️ 架构设计

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

### Docker 部署（推荐）

```bash
# 构建后端镜像
cd synpharm-backend
mvn clean package -DskipTests
docker build -t synpharm-backend .

# 构建前端镜像
cd ../synpharm-frontend
npm run build
docker build -t synpharm-frontend .

# 使用 docker-compose 启动
docker-compose up -d
```

### 传统部署

```bash
# 后端打包
cd synpharm-backend
mvn clean package -DskipTests
java -jar target/synpharm-backend-1.0.0.jar

# 前端打包
cd synpharm-frontend
npm run build
# 将 dist/ 目录部署到 Nginx
```

---

## 📝 开发规范

- **提交规范**：遵循 [Conventional Commits](https://www.conventionalcommits.org/)
  - `feat:` 新功能
  - `fix:` 修复 Bug
  - `docs:` 文档更新
  - `refactor:` 代码重构
  - `chore:` 构建/工具变更
- **分支策略**：`master` 为主分支，开发使用 `dev` 分支，功能使用 `feature/*` 分支
- **代码风格**：后端遵循阿里巴巴 Java 规范，前端遵循 ESLint + Prettier 配置

---

## 📄 文档

| 文档 | 路径 | 说明 |
| :--- | :--- | :--- |
| 用户认证模块技术设计文档 | [synpharm-backend/用户认证模块技术设计文档.md](file:///d:/SynPharm/synpharm-backend/用户认证模块技术设计文档.md) | 认证模块完整设计说明（架构、流程、安全机制） |
| 用户认证模块开发指南 | [synpharm-backend/用户认证模块开发指南.md](file:///d:/SynPharm/synpharm-backend/用户认证模块开发指南.md) | 认证模块技术实现细节（策略模式、安全机制、扩展方法） |
| AI 预测核心模块开发指南 | [synpharm-backend/AI预测核心模块开发指南.md](file:///d:/SynPharm/synpharm-backend/AI预测核心模块开发指南.md) | 预测模块设计文档（DTI/PPI/DDI、任务管理、扩展指南） |
| 代码审查日志 | [log/用户认证模块代码审查日志_20260722.md](file:///d:/SynPharm/log/用户认证模块代码审查日志_20260722.md) | 代码审查报告（问题清单和修复建议） |

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
- [Vue.js](https://vuejs.org/) — 前端框架
- [Element Plus](https://element-plus.org/) — UI 组件库
- [MyBatis-Plus](https://baomidou.com/) — MyBatis 增强工具
- [Knife4j](https://doc.xiaominfo.com/) — API 文档增强

---

<div align="center">

**⭐ 如果这个项目对你有帮助，请点个 Star！**

Made with ❤️ by SynPharm Team

</div>
