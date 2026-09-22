# 更新日志

## [v3.3.0] - 2026-09-22

### 今日主题

把算法引擎依赖对齐到 numpy 2 生态（解决 PPI 完全不可用），并修复批处理链路的四处断点。

### 修复

1. **PPI（FlashPPI）完全不可用 —— 依赖版本错位（根本原因）**
   - 现象：权重已下载（2796 MB）却加载失败，日志为
     `Disabling PyTorch because PyTorch >= 2.5 is required but found 2.1.0+cpu`
   - 原因：`requirements.txt` 中 `transformers>=4.40` 未锁版本，实际装成 5.x；
     而 5.x 硬性要求 `torch>=2.5`，`Dockerfile` 却把 torch 钉在 `2.1.0+cpu`
   - 处理：torch → `2.5.1+cpu`；numpy → `>=2.1,<3`；pandas / scikit-learn 同步抬高下限
     （旧 wheel 按 numpy 1.x 编译，在 numpy 2 下 import 即报 ABI 错误）；
     transformers 加 `<6` 上限，防止再次跨大版本静默漂移

2. **DDI 与 PPI 实为同一根因**：容器 numpy 锁在 1.26，而项目权重文件本身是 numpy 2 时代产物
   - DDI 加载报 `No module named 'numpy._core'`，此前靠 `_patch_numpy_legacy_aliases()` 别名补丁绕过
   - numpy 升到 2.x 后该补丁成为空操作，**已移除**，并回归验证 DDI 仍为 `0.662937`、白名单 `1323`
   - 注：torch 上限刻意停在 2.5.x —— 2.6 起 `torch.load` 的 `weights_only` 默认改为 `True`，
     会直接击穿 DTI/DDI 的整包 pickle 权重

3. **批处理任务永远停在 PENDING**
   - `RabbitConfig` 的 `@EnableRabbit` 与 `BatchTaskConsumer` 的 `@RabbitListener` 都被注释；
     而 `processBatch` 全仓只有该消费者一个调用方 → 上传写库成功但无人消费
   - 处理：恢复两处注解，并把 javadoc 改成醒目的依赖说明

4. **批次结果 CSV 数据行全空**
   - `BatchProcessServiceImpl.toResultMap` 写 camelCase 键，`CsvUtils.formatResultLine`
     读 snake_case 键，两组键**零交集** → 下载的 CSV 只有表头
   - 且输入字段（SMILES / 序列）不在 `PredictResultResponse` 中，只对齐键名仍不够
   - 处理：按算法输出对应 snake_case 键，新增 `resolved` 入参补齐输入列，
     并把 null 统一转 `""`（`getOrDefault` 遇到"键存在且值为 null"时仍会取到 null）

5. **批量失败行被伪装成成功**：DTI / PPI / DDI 三个执行器无条件 `setStatus("success")`
   - 处理：改为按引擎返回的 `error` 键判定，失败行不填 metrics；`AlgoResponse` 补 `errorMessage` 字段

6. **批次统计字段被吞**：`BatchPredictionResponse` 缺 `success` / `failed`，引擎的逐条统计被 Jackson 静默丢弃

7. **DDI 结果伪造靶点标识**：`target_id` 原为常量 `DDI_TARGET`
   - 处理：改为药物对 `{drug_a}-{drug_b}`，与项目自身在 `PredictUtils` 中的 DDI 约定一致

8. **批量上传 100% 失败：MySQL 保留字未转义**（实跑批量时发现，原问题总账未收录）
   - `ROW_NUMBER` 是 MySQL 8.0 保留字；建表脚本里已写 `` `row_number` ``，
     但实体上写的是 `@TableField("row_number")`，少了反引号 →
     MyBatis-Plus 生成 `INSERT INTO batch_task_item ( batch_id, row_number, ... )`
     → `SQLSyntaxErrorException`，表现为"上传直接 400"
   - 处理：改为 `` @TableField("`row_number`") ``
   - 该缺陷此前被 A-01（MQ 监听被注释）掩盖 —— **两处都修，批量才真正跑通**

9. **扩容脚本未应用到已部署环境**：`batch_task_item` 表在 MySQL 里根本不存在
   - 原因：`deploy/` 的 initdb 挂载**只在数据卷首次初始化时执行**，
     后加入的 `sql/*.sql` 不会自动应用，必须手工执行一次
   - 处理：已手工应用 `sql/09_batch_task_item.sql`

### 验证

容器内直接调用适配器 + HTTP 端到端，全部对齐升级前记录的基线：

| 项 | 实测结果 | 基线 |
|---|---|---|
| DTI（KAN-MoDTI） | `(0, 0.000234)`，类别 0 | 类别 0 / 0.0001 |
| DDI（DDI-LLM） | `DB00880+DB09220 → 0.662937` | 0.6629 |
| DDI 白名单 | `1323` | 1323 |
| **PPI（FlashPPI）** | **首次可用**：`contact_score=0.190828`、`clip_score=0.516538`、contact_map 668×668 | — |
| `GET /api/predict/ddi/drugs` | `total=1323`，首项 `DB00006 / Bivalirudin` | 新增 |
| 批量链路 | 上传 3 行 → `SUCCESS`、`successCount=3`、`failCount=0`、`progress=100` | 修复前永久 PENDING |
| 批量结果 CSV | 表头 + 3 行真实数据，首行 `DB00880,DB09220,0.6629,medium` | 修复前只有表头 |

### 新增

- `GET /api/predict/ddi/drugs`：DDI 可预测药物白名单（代理引擎 `GET /v1/ddi/drugs`）。
  DDI-LLM 是转导式模型，只能预测训练图内的药物；该接口把"模型不支持"
  从事后报错变成事前可见，供前端渲染可选药物下拉。

### 文档

- **合并重复的 CHANGELOG**：根 `CHANGELOG.md`（截至 v3.0.0）与 `log/CHANGELOG.md`
  （v3.1.0 起）本为互补关系，现合并为 `log/CHANGELOG.md` 单一版本，历史完整保留
- **删除 7 份冗余文档**：
  - `synpharm-backend/架构设计文档.md` —— 与 `docs/architecture/架构设计文档.md` 逐字节相同
  - `docs/development/已发现问题清单与改进方案.md`、`未实现功能修复技术方案.md`
    —— 《问题总账与整改方案（整合版）》§1 自述已将其并入 §3 / §4 / §7 / §8 / §13 / §14 / §15
  - `docs/modules/predict/algorithm/{DTI,PPI,DDI}数据流开发操作文档.md`
    —— 三个组件均已实现，教程使命完成；总览由 `docs/modules/predict/数据流开发操作文档.md` 承担
  - `docs/modules/predict/AI预测核心模块技术开发文档.md`
    —— 已被拆分为 SpringBoot / FastAPI 两份文档取代，且其内部导航指向的文件名已失效
- 同步修正因上述改动而失效的引用：`docs/deploy/上线流程详细版.md`（`CHANGELOG.md` → `log/CHANGELOG.md`）、
  `docs/architecture/SynPharm项目整体架构培训文档.md`（目录树）
- `docs/modules/predict/AI预测核心模块-FastAPI算法引擎技术开发文档.md`：版本表与 requirements 片段
  同步为 torch 2.5.1 / numpy 2.x，并补充三条版本约束说明

## [v3.2.1] - 2026-09-21

### 今日主题

修复容器化环境"一键启动总是失败"的问题，定位并解决 `.env` 漂移、批处理编码、重复构建三个根因。

### 修复

1. **`.env` 漂移导致启动失败（根本原因）**
   - 现象：`docker compose` 报 `required variable JWT_SECRET is missing a value`；
     补上空值后 MySQL 健康检查仍持续失败，`--wait` 超时报笼统的"启动失败"
   - 原因：`deploy/.env` 被 `.env.example` 覆盖 —— 必填项变空，且 MySQL 密码与
     **容器首次初始化数据卷时**写入的密码不一致（数据卷里的密码不会随 `.env` 变更而同步）
   - 处理：从现存容器 `docker inspect` 读回真实值回填 `.env`，**未删除数据卷、未丢失数据**

2. **`deploy/scripts/start.bat` 重写为纯 ASCII**
   - 原因：cmd.exe 解析含非 ASCII 字节的批处理文件时会错位，把注释甚至 `echo` 当成命令执行。
     实测：UTF-8 + `chcp 65001` ❌、UTF-8 无 chcp ❌、GBK 无 chcp ✅ 但 UTF-8 终端显示乱码
   - 同时修复 `if (...)` 块内 `echo` 含裸括号（`credential(s)`）导致块提前结束的报错

3. **启动脚本健壮性提升**
   - 自动定位 `docker.exe`：终端 PATH 未刷新时不再误报"Docker 未运行"
   - 自动拉起 Docker Desktop 并等待就绪（最多 180 秒）
   - 已有容器运行时跳过端口预检（避免"自己占自己"的误报）
   - **镜像已存在则跳过构建**：此前每次 `--build`，在构建缓存被回收后会重下 185 MB 的 torch，
     实测卡住 12 分钟以上；需要重建时执行 `scripts\start.bat rebuild`

### 文档

- 重写 `deploy/环境配置说明.md`：新增"启动失败的三大真实原因"、`start.bat` 行为说明、
  维护须知（脚本必须保持纯 ASCII）与 `.env` 覆盖警告
- 同步 `README.md`、`docs/deploy/容器化部署与Docker环境指南.md`、`docs/deploy/部署指南.md`：
  移除"先把 `.env.example` 复制为 `.env`"的过时指引（这正是本次故障的成因），
  修正端口（后端 8080→7000、FastAPI 8000→9050）与失效路径（`d:\SynPharm\docker`→`deploy`）

## [v3.2.0] - 2026-08-23

### 今日主题

完成 SynPharm 前后端全面联调，核心业务模块接入真实 API，预测业务闭环完成。

### 新增功能

1. **预测结果管理完善**
   - 接入真实接口 `GET /api/results`、`GET /api/results/{id}`
   - 结果详情由 Mock 改为真实后端数据
   - 支持展示算法类型、靶点、SMILES、结合亲和力、置信度、交互信息

2. **预测结果删除**
   - 接入 `DELETE /api/results/{id}`
   - 前端结果列表增加删除操作
   - 删除成功刷新列表
   - 说明设计逻辑：`PredictTask` 与 `PredictResult` 分离，删除结果不删除任务记录

3. **预测历史**
   - 新增 `GET /api/predict/history`
   - `Predict.vue` 增加预测历史 Tab
   - 展示用户历史预测记录

4. **批量预测联调**
   - 完成 `POST /api/batch/upload`、`GET /api/batch/status/{batchId}`、`GET /api/batch/download/{batchId}`
   - 验证上传、任务创建、状态查询流程
   - 状态 SUCCESS 测试通过

5. **Dashboard 统计修复**
   - Dashboard 已接入真实统计数据
   - 移除统计页面 Mock 依赖

### 修复

- `Predict.vue`、`Results.vue`、`Tasks.vue` 核心页面去 Mock
- DTI/PPI/DDI 字段兼容修复
- `bindingAffinity` 空值保护
- `Visualization.vue` 改为通过 `GET /api/results/{id}` 获取真实数据

### JSON 契约对齐（接口数据流通）

- 预测结果字段统一：`id`/`algoType`/`targetId`/`targetName`/`ligandSmiles`/`bindingAffinity`/`confidenceScore`/`confidenceLevel`/`interactions`/`createdAt`
- 前端 API 与后端响应保持一致

### 测试验证

已通过：
- `POST /api/auth/login`
- `GET /api/users/profile`
- `PUT /api/users/profile`
- `GET /api/tasks`
- `GET /api/results`
- `GET /api/results/{id}`
- `DELETE /api/results/{id}`
- `GET /api/predict/history`
- `POST /api/batch/upload`
- `GET /api/batch/status/{batchId}`

### 待办（未完成）

1. **批量下载问题**
   - `GET /api/batch/download/{batchId}`
   - 当前 CSV 只有表头，没有数据
   - 需要后端检查批量结果落库和查询关联

2. **邮箱验证码**
   - `POST /api/auth/captcha/send`
   - 当前提示邮件服务未配置
   - 影响注册和忘记密码

3. **靶点库**
   - `Targets` 页面仍使用 Mock 数据
   - 待接入真实 `GET /api/targets`、`GET /api/targets/{id}`

4. **保存结果按钮**
   - 当前预测完成后后端已自动保存 `predict_result`
   - 按钮无实际业务
   - 需要删除或改造成收藏功能

---

## [v3.1.0] - 2026-08-07

### 今日主题

完善注册登录、修复 FastAPI 认证、打通前后端 JSON 契约、核心视图接入真实 API、安全加固、产出批量处理技术设计。

### 新增功能

1. **独立注册接口**
   - 新增 `POST /api/auth/register`（邮箱 + 昵称 + 密码 + 验证码，type=register）
   - 注册成功自动登录返回 Token；邮箱/密码唯一性校验、并发兜底

2. **密码登录**
   - 新增 `PasswordLoginStrategy`（loginType=password，BCrypt 校验）
   - 登录页新增"密码登录"入口，与"验证码登录"并列

3. **开发模式验证码回显**
   - 未配置邮件服务时可本地联调：验证码直接返回给前端显示
   - 后加固为显式开关 `CAPTCHA_DEV_MODE`（生产必须 false）

4. **预测核心链路补齐**
   - 新增 `PpiAlgoExecutor` / `DdiAlgoExecutor`（PPI/DDI 单条+批量真实调用 FastAPI）
   - 单条预测结果落库（隐式任务 + `predict_result`）
   - 新增 `GET /api/predict/history` 预测历史
   - `/api/results` 列表/详情/删除从空实现改为真实 DB 查询

5. **批量处理技术设计**
   - 产出 `docs/modules/predict/批量处理技术设计文档.md`（RabbitMQ 方案，含拓扑/消息/可靠性/实施计划，待实施）

### 修复

- **FastAPI API Key 认证 bug**：`APIKeyHeader` 隐式注入未生效导致正确 key 也 401，改为手动读取请求头 `X-API-Key`
- **登录 Token 字段不匹配**：后端返回 `accessToken`，前端误读 `token` → 修复为 `response.accessToken`
- **调试后门**：移除 `POST /api/auth/debug/login`（前端同步移除）
- **任务模块必崩**：补 `PredictTaskMapper.xml`，修复 `GET /api/tasks` 的 `Invalid bound statement`；清理无绑定的 `selectByTaskId`
- **实体与表不对齐**：`PredictResult`/`PredictTask` 去掉表不存在的 `updated_at`，补齐缺失列
- **安全**：JWT 密钥移除硬编码默认值（fail-fast）；`AuthenticationEntryPoint` 统一返回 401；前端 baseURL 生产走相对路径（nginx 同源反代）

### JSON 契约对齐（接口数据流通）

- 后端 ↔ FastAPI：`PredictRequest`/`AlgoResponse` 加 `@JsonNaming(SnakeCaseStrategy)`，修复 camelCase/snake_case 断裂
- 前后端请求：DDI 字段统一为 `drugASmiles/drugBSmiles`
- 前后端响应：`PredictResultResponse` 按前端期望改造（`id`/`ligandSmiles`/`datasetInfo`/interactions 字段）
- 前端 `api/predict.ts`/`types` 对齐；结果列表改分页 `{total,list}`
- 前端核心视图接真实 API 并去 Mock：`Predict.vue`（真实预测）、`Results.vue`（真实结果）、`Tasks.vue`（真实任务）

### 部署 / 文档

- 上线流程文档：创建 → 更新核心问题总览 → 合并为单一文档
- `AI预测核心模块-SpringBoot后端技术开发文档.md`：批量处理架构更新为 RabbitMQ（§2.4）
- 代码多次提交推送 GitHub（`main`）

### 待办（未完成）

- Dashboard / Profile / Targets 统计页仍用 Mock（依赖后端统计接口，待实现）
- 批量处理 RabbitMQ 改造（技术设计已完成，待按实施计划落地）

---

## [v2.3.0] - 2026-07-24

### 新增功能

1. **独立注册接口**
   - 新增 `/api/auth/register` 接口，支持用户通过邮箱验证码注册
   - 注册成功后自动登录，返回JWT Token

2. **验证码有效期调整**
   - 将邮箱验证码有效期从5分钟改为1分钟
   - 提高安全性，减少验证码被滥用的风险

3. **管理员调试登录接口** ⚠️
   - 新增 `/api/auth/debug/login` 接口
   - 输入固定验证码 `zhihuyaoyan` 即可直接登录系统
   - 登录后角色为 `admin`，便于开发调试
   - **注意**：此接口仅用于开发环境，生产环境需删除

4. **前端注册页面更新**
   - 添加验证码输入框（6位数字）
   - 添加发送验证码按钮，支持60秒倒计时
   - 实时表单验证，包含验证码格式校验

### 修改文件

**后端文件**：
- `src/main/java/com/synpharm/dto/request/RegisterRequest.java` - 新增注册请求DTO，包含captcha字段
- `src/main/java/com/synpharm/service/AuthService.java` - 新增register方法接口
- `src/main/java/com/synpharm/service/impl/AuthServiceImpl.java` - 实现注册逻辑
- `src/main/java/com/synpharm/api/AuthController.java` - 新增注册和调试登录接口
- `src/main/java/com/synpharm/service/impl/EmailCaptchaServiceImpl.java` - 验证码有效期改为1分钟

**前端文件**：
- `src/api/auth.ts` - 新增sendCaptcha和debugLogin接口
- `src/stores/auth.ts` - 新增sendCaptcha方法，更新register参数类型
- `src/views/Register.vue` - 添加验证码输入框和发送按钮，实现倒计时功能

**文档文件**：
- `用户认证模块技术设计文档.md` - 更新接口列表、验证码有效期、新增注册和调试接口文档

### 技术实现

- 注册流程：发送验证码 → 输入验证码 → 验证验证码 → 创建用户 → 生成Token
- 调试登录：验证固定验证码 → 查询或创建调试用户 → 生成Token（admin角色）
- 验证码验证：Redis存储，1分钟过期，使用后立即删除

### 安全注意事项

1. 验证码有效期缩短至1分钟，降低暴力破解风险
2. 调试接口仅用于开发，生产环境部署前必须删除
3. 注册密码使用BCrypt加密存储
4. 所有输入参数均经过后端校验

## [v2.2.0] - 2026-07-23

### 初始版本

- 实现QQ邮箱验证码登录注册合一功能
- 策略模式支持多种登录方式扩展
- JWT无状态认证
- 登录限流（5次失败锁定15分钟）
- Token黑名单机制
- Redis缓存验证码和限流数据

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
