# z-meta

> 元数据管理平台 —— 租户 / 应用 / 字典 / 接口清单 / 私有配置的集中登记处，外加 `@BizModel` 业务模型注解采集

一人公司基座里"谁有什么数据、谁调什么接口"的那本账：把租户、应用、数据字典、API 清单、私有配置
落库成可查可改的元数据，并在启动时扫描 `com.zifang` 包下带 `@BizModel` / `@BizField` 的实体，
把字段级模型口径当场采集成可查询的元数据。`z-meta-web` 是可直接嵌进宿主应用的库，
`z-meta-admin` 只是同代码的独立启动壳。

---

## 📋 基本信息

| 字段 | 值 |
|------|-----|
| **仓库** | `z-meta` |
| **Maven 坐标** | `io.github.yuku123:z-meta`（根聚合 POM，`<packaging>pom</packaging>`） |
| **当前版本** | `1.0.0`（根 POM 写字面 `<version>1.0.0</version>`，**不是** `${revision}`；flatten-maven-plugin 1.5.0 + `oss` 模式常开） |
| **父项目** | `io.github.yuku123:z-boot-parent:1.1.0`（`<relativePath/>` 留空，parent 在 repo1 不在磁盘） |
| **Maven Central** | 已发布（repo1 实测 200）：`z-meta` / `z-meta-api` / `z-meta-core` / `z-meta-web` 的 `1.0.0` pom 与 jar 均可拉取；`z-meta-admin`、`z-meta-frontend` 为 404（不在 reactor，不发布）；旧 `com.zifang` namespace 整树 404 |
| **默认端口** | `8080`（**仓内没有任何 yml 设置 `server.port`**，取 Spring Boot 默认值；`Dockerfile` 亦 `EXPOSE 8080`，运维脚本映射宿主机 `8094`） |
| **context-path** | 仓内未设置；前端、nginx、vite 代理都按 `/meta` 前缀对接，需运行时注入 `--server.servlet.context-path=/meta` |
| **运行口径** | Java 8（根 POM `<compile.version>8</compile.version>`）· Spring Boot 2.7.18 |
| **最近更新** | 2026-09-30 |

---

## 🎯 能力清单

每条都能对应到 `z-meta-web/src/main/java/com/zifang/z/meta/web/api/` 下的 Controller 或
`z-meta-core` 里的 Service/实体：

| 能力 | 入口 | 落库/实现 |
|------|------|-----------|
| 租户管理 | `MetaTenantController` | `ZTenantDO` → `z_tenant`（tenantCode / tenantName / tenantType / expireTime / status） |
| 应用管理 | `MetaApplicationController` | `ZApplicationDO` → `z_application`（tenantId / appCode / appName / appType / baseUrl / owner） |
| 字典管理 | `DictController`（`/type` 与 `/item` 两组） | `ZDictTypeDO` → `z_dict_type`、`ZDictItemDO` → `z_dict_item` |
| 接口清单 | `ApiController` | `ZApiDO` → `z_api`（appId / apiPath / apiMethod / apiVersion / authType / deprecated） |
| 私有配置 | `PrivateConfigController`（含 `get-by-key`、按 `configKey` 幂等 upsert） | `ZPrivateConfigDO` → `z_private_config` |
| 业务模型采集 | `BizModelController` + `BizModelCollector`（`@PostConstruct` 扫 `com.zifang`） | 注解定义在 `z-meta-api`：`@BizModel(name/module/description)`、`@BizField(name/type/refModel/refModule/dictCode)`，10 种 `FieldType`；结果驻留内存，**不落库** |
| 健康检查 | `HealthController`（bean 名 `metaHealthController`） | 固定返回 `z-meta is running` |
| 模块数据源装配 | `MetaModuleDataSource`（继承 `z-boot-datasource-starter` 的 `ModuleDataSourceTemplate`） | `dataSourceMeta` + `sqlSessionFactoryMeta`，`@MapperScan("com.zifang.z.meta.core.domain.mapper")` |
| 自动装配 | `ZMetaAutoConfiguration`（`@ComponentScan("com.zifang.z.meta")`） | 经 `z-meta-web/src/main/resources/META-INF/spring.factories` 生效 |

> `00-overview.md` 里的"数据标准 / 数据血缘 / 数据质量"是**愿景**，代码里没有对应实现，README 不把它当功能宣称。

---

## 🏗️ 项目结构

```
z-meta/
├── pom.xml                # 根聚合 POM：继承 z-boot-parent:1.1.0，version 1.0.0
├── Dockerfile             # 后端镜像（eclipse-temurin:8-jdk，EXPOSE 8080）
├── LICENSE                # MIT，Copyright (c) 2026 z-opc-foundation
├── README.md              # 本文档
├── z-meta-api/            # 注解 API：@BizModel / @BizField，零实现零依赖（进 Central）
├── z-meta-core/           # 实体 + MyBatis Mapper + Service + BizModelCollector（进 Central）
├── z-meta-web/            # HTTP 层：7 个 Controller + 自动装配 + 模块数据源（进 Central）
├── z-meta-admin/          # 独立启动壳：不在 <modules>，不参与 reactor、不上 Central
│   └── src/main/resources/static/   # 前端产物快照（git 实测 10 个文件）
├── _frontend/             # Vite + React 19 + antd 6 独立 npm 工程（同样不在 <modules>）
└── _doc/                  # 文档，见文末「文档目录」
```

根 POM `<modules>` 实测只有三条：`z-meta-api`、`z-meta-core`、`z-meta-web`。
`z-meta-admin` 与 `_frontend` 都被注释掉留在仓里：admin 的 controllers + AutoConfiguration 已整体搬到
`z-meta-web`，留源码目录仅作 standalone 参考，免得它的 `application.yml` 污染宿主；`_frontend` 不参与
Maven 发布，否则构建会去 nodejs.org 下 node（它的 dist 由 `_frontend/pom.xml` 的 resources-plugin
拷进 `z-meta-admin/src/main/resources/static`）。

`z-meta-admin` 与 `z-meta-web` 是同一套代码的两份拷贝（包名 `...meta.admin.*` vs `...meta.web.*`），
`z-meta-admin` 的 POM 还引了 log4j2 而 `z-meta-web` 不引——按 standalone 用途各自成闭环。

---

## 🔧 技术栈

| 层级 | 技术（版本口径均来自 pom 实测） |
|------|--------------------------------|
| 语言 / 运行时 | Java 8（`compile.version=8`；`z-boot-parent` 下发 1.8 口径） |
| 框架 | Spring Boot 2.7.18（由 `z-boot-parent` 自带地板供，本仓不再 import Boot BOM） |
| 持久层 | MyBatis-Plus 3.5.7 + Druid 1.2.23（均走 `z-boot-datasource-starter`，版本由父链下发） |
| 数据库驱动 | `com.mysql:mysql-connector-j:8.0.33`（本仓按坐标显式钉，并整条抄回 Boot 的 `protobuf-java` exclusion，防被地板抬到 8.4.0） |
| 其它显式钉版 | `org.yaml:snakeyaml:2.0`（地板不供这一条，不钉会静默落回 Boot 的 1.30） |
| 组织内依赖 | `io.github.yuku123`: `z-boot-datasource-starter` / `z-boot-web-starter`（`${z-boot.version}`=1.0.21）、`z-util-core`（版本走 `z-boot-parent` 的兄弟仓权威表）、`z-meta-api` / `z-meta-core` |
| 返回体 / 文档 | `com.zifang.util.core.meta.Result`（z-util）· Knife4j `knife4j-openapi3-spring-boot-starter:4.1.0`（父链下发） |
| 独立启动壳 | `spring-boot-starter-web` + `spring-boot-starter-log4j2`（admin 排除默认 Logback） |
| 前端 | React 19 + antd 6 + react-router-dom 7 + axios，Vite 6 构建，pnpm workspace（`_frontend/`，非 Maven reactor） |
| 构建 / 发布 | Maven（`central` profile：central-publishing-maven-plugin 0.7.0 + source 3.3.0 + javadoc 3.11.2 + gpg 3.2.7）· Docker |

---

## 🚀 快速开始

### 编译

```bash
mvn clean install -DskipTests
```

本仓**自包含**：parent 是 repo1 上的 `io.github.yuku123:z-boot-parent:1.1.0`，`<relativePath/>` 留空，
不需要任何"父项目目录"，也不需要 monorepo 时代的 `../pom.xml`。只要本机能解析到那条 parent 就能构建。
第三方版本一律由 `z-boot-parent:1.1.0` 供给（第三方地板与兄弟仓权威表均已并入 parent 自身），
模块 POM 里不应再出现字面版本钉。

### standalone 跑 admin

```bash
# 1) 先把 reactor 三件装进本地仓库（admin 依赖 z-meta-core，而 admin 不在 reactor）
mvn clean install -DskipTests

# 2) 单独构建 admin
mvn -f z-meta-admin/pom.xml clean package -DskipTests

# 3) 起服务：repackage 带 <classifier>exec</classifier>，可执行的是 -exec 那一只
java -jar z-meta-admin/target/z-meta-admin-1.0.0-exec.jar \
  --server.port=18086 \
  --server.servlet.context-path=/meta \
  --z.meta.standalone=true
```

`--server.port=18086 --server.servlet.context-path=/meta` 是仓内注释（`z-meta-admin/application.yml`
首两行与根 POM）给出的既有走法；`z.meta.standalone=true` 是 `ZMetaDataSourceConfig` 上
`@ConditionalOnProperty` 的开关——不打这条就不额外建 `spring.datasource` 那份主数据源，
mapper 走 `MetaModuleDataSource` 的模块数据源。

### 数据源配置键与环境变量

`MetaModuleDataSource#buildDataSource(env, "meta")` 读 `z.base.db.meta.*`，缺省回退 `z.base.db.default.*`：

| 配置键 | 环境变量名（Spring 宽松绑定） |
|--------|------------------------------|
| `z.base.db.meta.host` / `.port` / `.database` | `Z_BASE_DB_META_HOST` / `Z_BASE_DB_META_PORT` / `Z_BASE_DB_META_DATABASE` |
| `z.base.db.meta.username` / `.password` | `Z_BASE_DB_META_USERNAME` / `Z_BASE_DB_META_PASSWORD` |
| `z.base.db.meta.initial-size` / `.min-idle` / `.max-active` / `.max-wait` | `Z_BASE_DB_META_INITIAL_SIZE` 等同形 |
| `z.base.db.meta.remove-abandoned` / `.remove-abandoned-timeout` | `Z_BASE_DB_META_REMOVE_ABANDONED` 等同形 |
| 以上任意键的回退档 | `Z_BASE_DB_DEFAULT_*` |

**凭据只走环境变量注入，禁止写进 yml / jar / 镜像层。** 2026-10-19 按 001_凭证管理规范 清理：
`z-meta-admin/src/main/resources/application.yml` 里原先写死的 RDS 连接串与用户名已换成占位符
（`${SPRING_DATASOURCE_URL:…}` / `${SPRING_DATASOURCE_USERNAME:}`），口令早已是
`${SPRING_DATASOURCE_PASSWORD:}`。**部署时必须注入 `SPRING_DATASOURCE_URL` 与
`SPRING_DATASOURCE_USERNAME`**，否则会落到占位符里的 localhost 默认值上。
另外该仓**没有定义任何 profile 专属 yml**，也没有 `docker-compose`/`k8s` 清单；根 `Dockerfile` 用
exec 形式 ENTRYPOINT，不经过 shell，所以 `JAVA_OPTS`、`SPRING_PROFILES_ACTIVE` 这类变量在那里不生效。

---

## 🔌 API 一览

下表是 `@RequestMapping` 实测值（`rg --no-ignore` 扫全部 `*.java`）。宿主若设
`server.servlet.context-path=/meta`，实际路径为 `/meta` + 表中前缀；`z-meta-admin` 那套副本路径逐字相同。

| 路径 | Controller | 主要子路径 |
|------|------------|-----------|
| `/api/meta-tenant` | `MetaTenantController` | `GET /list`、`GET /get`、`POST`、`PUT`、`DELETE` |
| `/api/meta-app` | `MetaApplicationController` | `GET /list`、`GET /get`、`POST`、`PUT`、`DELETE` |
| `/api/dict` | `DictController` | `GET /type/list`、`GET /type/get`、`POST`/`PUT`/`DELETE` `/type`、`GET /item/list`、`GET /items/get`、`POST`/`PUT`/`DELETE` `/item` |
| `/api/interface` | `ApiController` | `GET /list`、`GET /get`、`GET /app/list`、`POST`、`PUT`、`DELETE` |
| `/api/private-config` | `PrivateConfigController` | `GET /list`、`GET /get`、`GET /get-by-key`、`POST`、`PUT`、`DELETE` |
| `/api/meta` | `BizModelController` | `GET /models`、`GET /models/{code}`、`GET /models/module/{module}` |
| `/api/health` | `HealthController` | `GET` → `z-meta is running` |

接口文档由 knife4j 提供（`z-meta-web` / `z-meta-admin` 均引该 starter，版本 4.1.0 由父链下发）：
服务带 `/meta` 前缀启动后访问 `http://localhost:<port>/meta/doc.html`。
本仓不产出 `swagger-ui.html`，历史 README 里那条地址已删。

---

## 🧪 测试

**本仓当前没有任何 `src/test` 目录**（`find . -type d -name test` 实测为空），所以：

```bash
mvn test      # 通过，但一个用例也不跑
```

`z-meta-admin` 声明了 `spring-boot-starter-test` 却没有测试类；历史 README 承诺的
"单元测试 / 集成测试 / `*PerformanceTest.java` 性能测试"在仓里没有对应文件，已删。
手工验证按上面 standalone 步骤起服务后打 `GET /api/health`。

---

## 🐳 部署

本仓没有 `docker-compose` / `Makefile` / k8s 清单，只有两个 Dockerfile 与三个运维脚本：

```bash
# 后端镜像（根 Dockerfile：eclipse-temurin:8-jdk，EXPOSE 8080，健康检查探测 /meta/doc.html）
mvn clean install -DskipTests
mvn -f z-meta-admin/pom.xml clean package -DskipTests
docker build -f Dockerfile -t z-meta:latest .

# 前端镜像（_frontend/Dockerfile：node:18-alpine 构建 + nginx:alpine 运行，EXPOSE 80）
docker build -f _frontend/Dockerfile -t z-meta-frontend:latest _frontend/
```

nginx 侧把 `/meta` 反代到 `http://z-meta:8080`（见 [`_frontend/z-meta-suit/nginx.conf`](_frontend/z-meta-suit/nginx.conf)）。

两处**实测的脚本陈旧项**，用前要先改（本任务只改 README，不动脚本）：

- 根 [`Dockerfile`](Dockerfile) 的 `COPY` 仍指向 `z-meta-admin/target/z-meta-admin-1.0.0-SNAPSHOT.jar`，
  而坐标已换成 `1.0.0` 且可执行件带 `exec` classifier → 现状会 COPY 失败。
- [`_doc/003_script/package.sh`](_doc/003_script/package.sh) 仍在 `cd z-meta-admin-frontend`，
  该目录已不存在（前端现在是 `_frontend/`）。

[`_doc/003_script/build.sh`](_doc/003_script/build.sh) 做「打包 → 建镜像 → 换容器 → 健康检查 → 清理旧容器」，
宿主端口 `8094` → 容器 `8080`，探活 URL 写的是 `/meta/health`（与代码里的 `/api/health` 对不上，
同样属陈旧项）；[`_doc/003_script/start.sh`](_doc/003_script/start.sh) 只起容器，若存在
`./application-prod.yml` 会挂进 `/app/application-prod.yml`。

---

## 📦 近期变更（来自 `git log`，非模板）

| 日期 | 变更 |
|------|------|
| 2026-09-30 | `release:` groupId `com.zifang` → `io.github.yuku123`、版本 `1.0.0-SNAPSHOT` → `1.0.0`，补 `central` / `central-dryrun` 发布 profile；四件已上 repo1 |
| 2026-09-30 | 三处 `com.zifang:z-boot-*-starter` 依赖换到 `io.github.yuku123` 发布坐标（clean-room 可构建） |
| 2026-09-29 | 消费入口抬到 `z-boot-parent:1.0.21`（消费者轮） |
| 2026-09-29 | 消费入口从幻影 parent `com.zifang:z-opc:1.0.0-SNAPSHOT`（`relativePath ../pom.xml`）换成 repo1 上的 `z-boot-parent` |
| 2026-09-06 | 从 z-opc monorepo 拆出独立仓（拆分记录见 `_doc/001_arch/01-module-structure.md`） |

注意 [`_doc/001_arch/01-module-structure.md`](_doc/001_arch/01-module-structure.md) 仍写着
"父项目 `../pom.xml`（com.zifang:z-opc）"、[`08-design.md`](_doc/001_arch/08-design.md) 写着
"Spring Boot 3.x / Vite 8.x"：这些是**历史/规划文档**，与当前 pom 不符，以本 README 为准。

---

## 📄 License

MIT，见仓库根 [`LICENSE`](LICENSE)（`Copyright (c) 2026 z-opc-foundation`）；根 POM `<licenses>` 同为 MIT。
本仓版权归 **z-opc-foundation** 组织（历史 README 写的"版权属于 z-biz"有误，已改）。

_Maintained by the z-opc-foundation organization._

---

## 文档目录

- [`_doc/008_troubleshooting/`](_doc/008_troubleshooting/) — 踩坑与失败要点（控制台首页写死字面量实录）
本项目文档统一收口在 `_doc/` 下：

- [`_doc/001_arch/`](_doc/001_arch/) — 架构文档：
  - [`00-overview.md`](_doc/001_arch/00-overview.md) — 项目总览与愿景（含尚未实现的血缘/质量规划）
  - [`01-module-structure.md`](_doc/001_arch/01-module-structure.md) — 从 z-opc 拆分的历史记录（parent 描述已过时）
  - [`08-design.md`](_doc/001_arch/08-design.md) — 技术方案（技术选型为规划口径，与 pom 实测不同）
  - [`09-design.md`](_doc/001_arch/09-design.md) — 系统概述与能力设计

- [`_doc/002_deploy/`](_doc/002_deploy/) — 部署 SQL：
  - [`init.sql`](_doc/002_deploy/init.sql) — 建表脚本，实测含 `z_tenant`、`z_application`、`z_dict_type`、
    `z_dict_item`、`z_api`、`z_private_config`，以及代码里没有对应实体的 `z_users` / `z_roles`

- [`_doc/003_script/`](_doc/003_script/) — 运维脚本：
  - [`build.sh`](_doc/003_script/build.sh)
  - [`package.sh`](_doc/003_script/package.sh)
  - [`start.sh`](_doc/003_script/start.sh)

- `_doc/004_skill/` — 目前为空目录，暂无 skill
