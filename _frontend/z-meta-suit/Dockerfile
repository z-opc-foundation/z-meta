# z-meta frontend 部署镜像 (FEATURE034 T3)
#
# Build context: z-meta/_frontend/
# 架构: 多阶段构建, node:18-alpine 跑 npm build, nginx:alpine serve dist
#
# ============================================================================
# Stage 1: 前端构建
# ============================================================================
FROM node:18-alpine AS builder

WORKDIR /app

# 先 copy package*.json, 改 package.json 才重装 (Docker layer cache 命中)
COPY package.json package-lock.json* ./
RUN npm install --no-audit --no-fund

COPY . .

# build (vite 默认输出到 dist/)
RUN npm run build && \
    test -f dist/index.html || (echo "build 失败: dist/index.html 不存在" && exit 1)

# ============================================================================
# Stage 2: nginx 运行时
# ============================================================================
FROM nginx:alpine

# 删默认配置, 换成我们的
RUN rm -f /etc/nginx/conf.d/default.conf

# nginx 配置 (SPA fallback)
COPY nginx.conf /etc/nginx/conf.d/default.conf

# 前端 build 产物
COPY --from=builder /app/dist /usr/share/nginx/html

# 时区
RUN apk add --no-cache tzdata && \
    cp /usr/share/zoneinfo/Asia/Shanghai /etc/localtime && \
    echo "Asia/Shanghai" > /etc/timezone && \
    apk del tzdata && \
    rm -rf /var/cache/apk/*

# nginx:alpine 默认用 nginx 用户 (uid 101), 已是非 root
RUN chown -R nginx:nginx /usr/share/nginx/html /var/cache/nginx /var/log/nginx /run

# 健康检查（FEATURE034 T4）— 探测 nginx 是否在响应静态页
HEALTHCHECK --interval=30s --timeout=3s --start-period=10s --retries=3 \
  CMD wget -q -O /dev/null http://127.0.0.1:80/ || exit 1

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]