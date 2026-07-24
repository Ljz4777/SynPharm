# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

---

## [v3.0.0] - 2026-07-25

### 新增

- 微服务分离架构：Spring Boot业务中台 + FastAPI算法引擎
- FastAPI独立项目：`synpharm-fastapi/` 目录，支持单独部署在GPU服务器
- DTI预测服务：`DTIService` 药物-靶点相互作用预测
- PPI预测服务：`PPIService` 蛋白质-蛋白质相互作用预测
- DDI预测服务：`DDIService` 药物-药物相互作用预测
- 批量预测接口：`/v1/predict/batch` 支持批量CSV处理
- WebClient配置：Spring Boot调用FastAPI接口
- PredictRequest DTO：类型安全的预测请求封装
- CsvUtils工具类：CSV文件解析、写入、字段转义
- 异步批量处理：`@Async` + ThreadPoolTaskExecutor
- 进度缓存：ConcurrentHashMap内存维护任务状态
- 健康检查接口：`/health/` 服务监控

### 修改

- PredictServiceImpl：从Mock数据改为调用FastAPI
- BatchProcessServiceImpl：使用CsvUtils处理CSV文件
- WebClientConfig：添加超时配置Bean
- FastApiClient：注入超时配置，添加algo_type参数
- CSV解析：支持引号包裹字段，处理含逗号数据
- CSV写入：根据algoType动态生成列名

### 修复

- application.yml：合并重复的spring节点
- FileReader：使用InputStreamReader指定UTF-8编码
- Map参数：替换为PredictRequest DTO类型安全传递

### 文档

- 更新四个技术文档：总文档、登录模块、FastAPI模块、SpringBoot模块
- 统一文档结构：模块概述、架构设计、API设计、代码实现、部署运行、测试方案、开发规范

---

## [v2.1.0] - 2026-07-20

### 新增

- 批量上传接口：`POST /api/batch/upload`
- 进度查询接口：`GET /api/batch/progress/{batchId}`
- 结果下载接口：`GET /api/batch/download/{batchId}`
- BatchTask实体：批量任务数据库表
- BatchTaskProgress：任务进度管理类
- 分片处理：批量任务按CHUNK_SIZE分片调用FastAPI
- Docker部署配置：支持GPU资源分配
- Docker Compose：一键部署SpringBoot+FastAPI+MySQL

### 修改

- 异步线程池配置：核心线程2，最大线程5
- 文件上传限制：50MB
- FastAPI超时配置：单条60秒，批量600秒

---

## [v2.0.0] - 2026-07-15

### 新增

- DTI预测接口：`POST /api/predict/dti`
- PPI预测接口：`POST /api/predict/ppi`
- DDI预测接口：`POST /api/predict/ddi`
- 预测历史接口：`GET /api/predict/history`
- PredictRecord实体：预测记录数据库表
- PredictService：预测服务接口
- AlgoResponse DTO：FastAPI响应封装
- PredictionMetrics DTO：预测指标封装
- 结果存储：预测结果存入数据库

### 修改

- 项目结构调整：新增api/service/client/dto目录
- 代码解耦：Controller-Service-Client分层

---

## [v1.5.0] - 2026-07-10

### 新增

- 用户注册接口：`POST /api/auth/register`
- 用户信息接口：`GET /api/auth/profile`
- JWT Token刷新机制
- 用户状态管理：禁用/启用
- 密码加密：BCryptPasswordEncoder
- 全局异常处理：GlobalExceptionHandler
- 参数校验：@Valid + @NotBlank

### 修改

- SecurityConfig：配置JWT过滤器
- UserController：新增注册和查询接口
- UserService：新增用户查询方法

---

## [v1.2.0] - 2026-07-05

### 新增

- 用户登录接口：`POST /api/auth/login`
- JWT认证：java-jwt库集成
- User实体：sys_user数据库表
- UserMapper：MyBatis-Plus数据访问
- UserService：用户服务接口
- ApiResponse：统一响应封装
- Spring Security配置：放行登录接口

### 修改

- pom.xml：添加java-jwt依赖
- 数据库初始化：添加sys_user表

---

## [v1.1.0] - 2026-07-01

### 新增

- Spring Boot项目初始化
- Maven依赖配置：web、webflux、security、mybatis-plus、mysql、lombok
- application.yml：数据库连接配置
- MyBatis-Plus配置：驼峰命名映射
- 启动类：SynpharmApplication

---

## [v1.0.0] - 2026-06-25

### 新增

- 项目初始化：SynPharm AI预测核心模块
- 技术文档：AI预测核心模块技术开发文档
- 目录结构：docs/synpharm-backend/synpharm-fastapi
- README.md：项目说明文档

---

[v3.0.0]: https://github.com/synpharm/synpharm/compare/v2.1.0...v3.0.0
[v2.1.0]: https://github.com/synpharm/synpharm/compare/v2.0.0...v2.1.0
[v2.0.0]: https://github.com/synpharm/synpharm/compare/v1.5.0...v2.0.0
[v1.5.0]: https://github.com/synpharm/synpharm/compare/v1.2.0...v1.5.0
[v1.2.0]: https://github.com/synpharm/synpharm/compare/v1.1.0...v1.2.0
[v1.1.0]: https://github.com/synpharm/synpharm/compare/v1.0.0...v1.1.0
[v1.0.0]: https://github.com/synpharm/synpharm/releases/tag/v1.0.0