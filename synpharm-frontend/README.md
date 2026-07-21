# SynPharm - AI药物发现平台

> 基于AI的智能药物研发平台，支持药物-靶点相互作用预测、分子可视化等功能

## 项目简介

SynPharm是一个AI驱动的智能药物研发平台，致力于通过人工智能技术加速药物研发过程。

### 核心功能

| 功能模块 | 描述 |
|---------|------|
| **PPI预测** | 蛋白质-蛋白质相互作用预测 |
| **DTI预测** | 药物-靶点相互作用预测 |
| **DDI预测** | 药物-药物相互作用预测 |
| **3D可视化** | 分子结构交互式3D展示 |
| **任务管理** | 预测任务创建、追踪与管理 |
| **结果管理** | 预测结果查看、筛选与导出 |
| **个人中心** | 用户信息管理与偏好设置 |

## 技术栈

### 前端
- **框架**: Vue 3.4+
- **语言**: TypeScript 5.x
- **状态管理**: Pinia 2.x
- **路由**: Vue Router 4.x
- **UI组件**: Element Plus 2.x
- **图表**: ECharts 5.x
- **构建工具**: Vite 5.x

### 后端
- **框架**: Spring Boot 3.2.x
- **语言**: Java 21
- **ORM**: MyBatis Plus 3.5.x
- **数据库**: MySQL 8.0+
- **安全**: Spring Security 6.2.x + JWT
- **API文档**: Knife4j 4.4.x

### AI服务
- **框架**: FastAPI
- **深度学习**: PyTorch
- **分子处理**: RDKit

## 项目结构

```
SynPharm-PC/
├── frontend/          # 前端Vue项目
│   ├── src/
│   │   ├── components/    # 公共组件
│   │   ├── views/         # 页面视图
│   │   ├── router/        # 路由配置
│   │   ├── stores/        # 状态管理
│   │   ├── types/         # 类型定义
│   │   └── utils/         # 工具函数
│   └── package.json
├── backend/           # 后端Spring Boot项目
│   ├── src/main/java/com/synpharm/
│   │   ├── controller/    # REST API控制器
│   │   ├── service/       # 业务逻辑层
│   │   ├── repository/    # 数据访问层
│   │   ├── model/         # 数据模型
│   │   ├── dto/           # 数据传输对象
│   │   ├── config/        # 配置类
│   │   ├── utils/         # 工具类
│   │   └── exception/     # 异常处理
│   └── pom.xml
├── TECHNICAL_DOCUMENTATION.md  # 技术文档
└── README.md
```

## 快速开始

### 环境要求
- Node.js 20+
- JDK 21
- Maven 3.8+
- MySQL 8.0+

### 后端启动

```bash
# 1. 进入后端目录
cd backend

# 2. 配置数据库连接
# 修改 src/main/resources/application.yml
# 设置数据库用户名和密码

# 3. 创建数据库
# CREATE DATABASE synpharm DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

# 4. 启动后端服务
mvn spring-boot:run

# 访问API文档: http://localhost:8080/doc.html
```

### 前端启动

```bash
# 1. 进入前端目录
cd frontend

# 2. 安装依赖
npm install

# 3. 启动开发服务器
npm run dev

# 访问前端: http://localhost:5173
```

## API接口

### 认证接口
- `POST /api/auth/login` - 用户登录
- `POST /api/auth/register` - 用户注册

### 预测接口
- `POST /api/predict/dti` - DTI预测
- `POST /api/predict/ppi` - PPI预测
- `POST /api/predict/ddi` - DDI预测

### 任务接口
- `GET /api/tasks` - 获取任务列表
- `GET /api/tasks/{taskNo}` - 获取任务详情

### 结果接口
- `GET /api/results` - 获取结果列表
- `GET /api/results/{resultNo}` - 获取结果详情

## 目录结构说明

| 目录 | 说明 |
|------|------|
| `frontend/src/components/` | 可复用的Vue组件 |
| `frontend/src/views/` | 页面级组件 |
| `frontend/src/stores/` | Pinia状态管理模块 |
| `backend/src/main/java/com/synpharm/controller/` | REST API控制器 |
| `backend/src/main/java/com/synpharm/service/` | 业务逻辑层 |
| `backend/src/main/java/com/synpharm/repository/` | 数据访问层 |

## 开发指南

### 代码规范
- 前端遵循Vue官方风格指南
- 后端遵循阿里巴巴Java开发手册
- 使用ESLint和Prettier进行代码检查

### 提交规范
- `feat:` 新增功能
- `fix:` 修复bug
- `docs:` 文档更新
- `style:` 代码格式调整
- `refactor:` 代码重构

## 许可证

MIT License

## 联系我们

如有问题或建议，请通过以下方式联系：
- 邮箱: contact@synpharm.com
- 项目地址: https://github.com/synpharm/synpharm-pc

---

**文档版本**: v1.0  
**最后更新**: 2026年6月