# z-meta

> 元数据管理平台

数据库元数据 + API 元数据集中管理, 含数据源动态配置、模块管理

---

## 📋 基本信息

| 字段 | 值 |
|------|-----|
| **项目** | z-meta |
| **分类** | 基础设施 · 元数据 |
| **父项目** | z-opc (com.zifang:z-opc:1.0.0-SNAPSHOT) |
| **默认端口** | `8087` |
| **文档维护** | z-opc-foundation |
| **最近更新** | 2026-09-06 |

---

## 🎯 核心功能

数据库元数据 + API 元数据集中管理, 含数据源动态配置、模块管理

详细功能特性详见各子模块 README 或源码注释。

---

## 🏗️ 项目结构

```
z-meta/
├── pom.xml                      # 根 POM (引用 z-opc 父项目)
├── README.md                    # 本文档
├── MODULE_NOTE.md               # 来源说明 (从 z-opc 拆分)
```

子模块列表:

| 模块 | 职责 |
|------|------|
| `z-meta-api/` | 对外 API 接口 |
| `z-meta-core/` | 元数据核心: 数据源/模块管理 |
| `z-meta-web/` | Web 控制层 |
| `_frontend/` | 前端 (React/Vue) |
| `z-meta-admin/` | Spring Boot 启动模块 |

---

## 🔧 技术栈

- Spring Boot
- React
- MySQL

---

## 🚀 快速开始

### 前置条件

- JDK 8+ (推荐 JDK 17)
- Maven 3.6+
- 端口 `8087` 未被占用

### 编译

```bash
# 在 z-opc 父项目下编译 (推荐)
cd /Users/zifang/workplace/idea_workplace/z-opc
mvn clean install -pl :z-opc -am -DskipTests

# 单独编译本模块 (需 ../pom.xml 父项目可用)
cd /Users/zifang/workplace/ceo_workplace/z-opc-foundation/z-meta
mvn clean compile
```

### 运行

```bash
# 启动主服务 (根据项目类型选择)
mvn -pl <启动模块> spring-boot:run
# 或
java -jar <启动模块>/target/*.jar
```

---

## 📦 模块说明

z-meta 由以下子模块组成:

| `z-meta-api/` | 对外 API 接口 |
| `z-meta-core/` | 元数据核心: 数据源/模块管理 |
| `z-meta-web/` | Web 控制层 |
| `_frontend/` | 前端 (React/Vue) |
| `z-meta-admin/` | Spring Boot 启动模块 |

各模块职责详见各子目录下的 `pom.xml` 和源码。

---

## 🧪 测试

```bash
mvn test
```

测试覆盖:
- 单元测试: 各核心服务类
- 集成测试: 端到端调用链路
- 性能测试: 详见 `/src/test` 下的 `*PerformanceTest.java`

---

## 🔌 API 接口

API 接口定义在各子模块的 `controller` 包下。

启动后访问 `http://localhost:8087/swagger-ui.html` 或 `/doc.html` (knife4j) 查看完整 API 文档。

---

## 🐳 部署

### Docker

```bash
# 构建镜像
docker build -t z-meta:latest .

# 运行容器
docker run -d -p 8087:8087 --name z-meta z-meta:latest
```

### 配置

主要配置文件:
- `application.yml` - Spring Boot 配置
- `logback.xml` - 日志配置
- 环境变量: `JAVA_OPTS`, `SPRING_PROFILES_ACTIVE`

---

## 📚 相关文档

- [MODULE_NOTE.md](./MODULE_NOTE.md) - 从 z-opc 拆分说明
- [z-opc 父项目](https://github.com/yuku123/z-opc) - 完整源码

---

## 📝 版本历史

| 版本 | 日期 | 变更 |
|------|------|------|
| 1.0.0 | 2026-09-06 | 从 z-opc monorepo 拆分独立仓, 文档补齐 |

---

## 📄 License

Internal use only. 版权属于 z-biz。

_Maintained by z-opc-foundation organization._


## 文档目录

本项目文档统一收口在 `_doc/` 下:

- [`_doc/001_arch/`](_doc/001_arch/) — 架构文档 (项目总览 / 模块结构 / 接口清单 / DB schema / 前端 / 能力 / roadmap):
  - [`00-overview.md`](_doc/001_arch/00-overview.md)
  - [`01-module-structure.md`](_doc/001_arch/01-module-structure.md)
  - [`技术方案.md`](_doc/001_arch/技术方案.md)
  - [`设计文档.md`](_doc/001_arch/设计文档.md)

- [`_doc/002_deploy/`](_doc/002_deploy/) — 部署文档 (含 `init.sql`):
  - [`init.sql`](_doc/002_deploy/init.sql)

- [`_doc/003_script/`](_doc/003_script/) — 运维脚本:
  - [`build.sh`](_doc/003_script/build.sh)
  - [`package.sh`](_doc/003_script/package.sh)
  - [`start.sh`](_doc/003_script/start.sh)

各文档详细说明见各子目录。
