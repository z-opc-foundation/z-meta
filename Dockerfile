# 基础镜像：Eclipse Temurin 17 JDK（支持amd64/arm64架构，M系列Mac可用）
#
# ⚠ 2026-11-06 底镜像 Java 8 → 17（Boot3 迁移遗留修）：
#   z-meta 已随 Boot3 火车迁到 JDK17（父 pom = z-boot-parent ⇒ class major 61），
#   而底镜像还停在 8-jdk ⇒ 容器起来即 UnsupportedClassVersionError
#   (class file version 61.0, this version only recognizes up to 52.0) ⇒ **崩溃循环**。
#   ⇒ 运行时底镜像必须与产物 class major 同一档（17）。
FROM eclipse-temurin:17-jdk

# 维护者信息
LABEL maintainer="zifang"

# 设置时区 + 装 wget (供 HEALTHCHECK 使用)
RUN apt-get update && apt-get install -y --no-install-recommends tzdata wget && \
    ln -sf /usr/share/zoneinfo/Asia/Shanghai /etc/localtime && \
    echo "Asia/Shanghai" > /etc/timezone && \
    apt-get remove -y tzdata && apt-get autoremove -y && apt-get clean

# 工作目录
WORKDIR /app

# 复制SpringBoot jar包
COPY z-meta-admin/target/z-meta-admin-1.0.0-SNAPSHOT.jar app.jar

# 暴露端口：HTTP 8080 (context-path=/meta, doc.html 在 /meta/doc.html)
EXPOSE 8080

# 健康检查（FEATURE034 T4）
HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 \
  CMD wget -q -O- http://127.0.0.1:8080/meta/doc.html || exit 1

# 启动命令
ENTRYPOINT ["java", "-jar", "app.jar"]