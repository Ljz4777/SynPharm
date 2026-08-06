# =============================================================================
# SynPharm FastAPI 算法引擎镜像 - 企业级多阶段构建
#
# 阶段一（deps）：安装 Python 依赖（torch 等大体积依赖隔离在构建层）
# 阶段二（runtime）：精简运行镜像 + 非 root
#
# 构建：docker build -t synpharm/fastapi:1.0.0 .
# =============================================================================

# ---------- 阶段一：安装依赖 ----------
FROM python:3.11-slim AS deps

WORKDIR /deps
COPY requirements.txt .
# 先安装 CPU 版 PyTorch（从 PyTorch CPU 索引，约 170MB，避免拉取 ~2GB 的 CUDA 依赖）
RUN pip install --no-cache-dir --prefix=/install torch==2.1.0 --index-url https://download.pytorch.org/whl/cpu
# 再安装其余依赖（requirements.txt 已不含 torch）
RUN pip install --no-cache-dir --prefix=/install -r requirements.txt

# ---------- 阶段二：运行 ----------
FROM python:3.11-slim

# 非 root 用户 + 健康检查工具 curl
RUN useradd --system --uid 10001 --create-home appuser \
    && apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app

# 拷贝依赖产物
COPY --from=deps /install /usr/local

# 拷贝应用代码（models/ 不进镜像，由 compose 卷挂载）
COPY . .

# 数据目录（模型、日志），赋予运行用户权限
RUN mkdir -p /app/models /app/logs \
    && chown -R appuser:appuser /app

USER appuser

EXPOSE 8000

# 企业级健康检查
HEALTHCHECK --interval=30s --timeout=5s --start-period=30s --retries=3 \
  CMD curl -fsS http://localhost:8000/health || exit 1

CMD ["uvicorn", "main:app", "--host", "0.0.0.0", "--port", "8000"]
