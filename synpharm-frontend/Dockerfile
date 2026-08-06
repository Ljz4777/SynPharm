# =============================================================================
# SynPharm 前端镜像 - 企业级多阶段构建
#
# 阶段一（build）：Node 构建静态资源（Vite + vue-tsc 类型检查）
# 阶段二（runtime）：nginx-unprivileged 以非 root 运行
#
# 构建：docker build -t synpharm/frontend:1.0.0 .
# =============================================================================

# ---------- 阶段一：构建 ----------
FROM node:20-alpine AS build

WORKDIR /build

# 先复制依赖清单，利用 Docker 层缓存
COPY package.json package-lock.json ./
RUN npm ci

# 复制源码
COPY . .

# 构建参数：生产构建走真实 API（同源，由 nginx 反代），关闭 mock
ARG VITE_API_BASE_URL=
ENV VITE_API_BASE_URL=$VITE_API_BASE_URL
ARG VITE_ENABLE_MOCK=false
ENV VITE_ENABLE_MOCK=$VITE_ENABLE_MOCK

# 直接使用 vite 构建（vue-tsc 类型检查在 CI 阶段执行，避免容器内 OOM 与拖慢构建）
RUN npx vite build

# ---------- 阶段二：运行 ----------
FROM nginxinc/nginx-unprivileged:1.27-alpine

WORKDIR /usr/share/nginx/html

# 拷贝构建产物
COPY --from=build /build/dist ./

# 默认 nginx 配置（前端 SPA + /api 反代到后端）
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 8080

# 健康检查（使用 127.0.0.1 避免 localhost 解析到 IPv6 ::1 导致探测失败）
HEALTHCHECK --interval=30s --timeout=5s --start-period=10s --retries=3 \
  CMD wget -qO- http://127.0.0.1:8080/ >/dev/null 2>&1 || exit 1

CMD ["nginx", "-g", "daemon off;"]
