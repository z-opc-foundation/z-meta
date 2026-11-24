# z-meta

> 元数据管理平台 —— 租户 / 应用 / 接口清单 / 私有配置的集中登记处，外加 `@BizModel` 业务模型注解采集

一人公司基座里"谁有什么数据、谁调什么接口"的那本账：把租户、应用、API 清单、私有配置
落库成可查可改的元数据，并在启动时扫描 `com.zifang` 包下带 `@BizModel` / `@BizField` 的实体，
把字段级模型口径当场采集成可查询的元数据。`z-meta-web` 是可直接嵌进宿主应用的库，
`z-meta-admin` 只是同代码的独立启动壳。

---

## 📋 基本信息

| 字段 | 值 |
|------|-----|
| **仓库** | `z-meta` |
| **Maven 坐标** | `io.github.yuku123:z-meta`（根聚合 POM，`<packaging>pom</packaging>`） |
| **当前版本** | `1.0.2`（根 POM 写字面 `<version>1.0.2</version>`，**不是** `${revision}`；flatten-maven-plugin 1.5.0 + `oss` 模式常开） |
| **父项目** | `io.github.yuku123:z-boot-parent:1.2.6`（`<relativePath/>` 留空，parent 在 repo1 不在磁盘） |
| **Maven Central** | `1.0.0` / `1.0.1` 已发布（repo1 实测 200）；**`1.0.2` 待发**（本版新增 `@BizMethod` / `@BizParam` / `@BizApi` / `@BizCause` 与 `ResourceProvider` SPI）；`z-meta-admin`、`z-meta-frontend` 为 404（不在 reactor，不发布）；旧 `com.zifang` namespace 整树 404 |
| **默认端口** | `8080`（**仓内没有任何 yml 设置 `server.port`**，取 Spring Boot 默认值；`Dockerfile` 亦 `EXPOSE 8080`，运维脚本映射宿主机 `8094`） |
| **context-path** | 仓内未设置；前端、nginx、vite 代理都按 `/meta` 前缀对接，需运行时注入 `--server.servlet.context-path=/meta` |
| **运行口径** | Java 17（根 POM `<compile.version>17</compile.version>`）· Spring Boot 3.3.13 |
| **最近更新** | 2026-09-30 |

---

## 🎯 能力清单

每条都能对应到 `z-meta-web/src/main/java/com/zifang/z/meta/web/api/` 下的 Controller 或
`z-meta-core` 里的 Service/实体：

| 能力 | 入口 | 落库/实现 |
|------|------|-----------|
| 租户管理 | `MetaTenantController` | `ZTenantDO` → `z_tenant`（tenantCode / tenantName / tenantType / expireTime / status） |
| 应用管理 | `MetaApplicationController` | `ZApplicationDO` → `z_application`（tenantId / appCode / appName / appType / baseUrl / owner） |
| 接口清单 | `ApiController` | `ZApiDO` → `z_api`（appId / apiPath / apiMethod / apiVersion / authType / deprecated） |
| 私有配置 | `PrivateConfigController`（含 `get-by-key`、按 `configKey` 幂等 upsert） | `ZPrivateConfigDO` → `z_private_config` |
| 业务模型采集 | `BizModelController` + `BizModelCollector`（`@PostConstruct` 扫 `com.zifang`） | 注解定义在 `z-meta-api`：`@BizModel(name/module/description)`、`@BizField(name/type/refModel/refModule/dictCode)`，10 种 `FieldType`；结果驻留内存，**不落库** |
| 注解体系扩展 | `z-meta-api` 的 `@BizMethod` / `@BizParam` / `@BizApi` / `@BizCause` | 方法（业务名/操作类型/幂等）、参数（必填/位置/引用模型/字典）、端点（摘要/标签/废弃）、因果链（因为/条件/导致/权重）—— 零依赖，宿主只引 `z-meta-api` 即可标注 |
| 接口契约采集 | `ApiContractCollector`（扫 `@RestController`） | 路径与 HTTP 方法一律取自 Spring 映射注解，`@Biz*` 只补业务语义；产出 `BizApiDTO` 端点契约 |
| 自研 OpenAPI 文档 | `MetaDocumentController` + `OpenApiDocumentGenerator` | `GET /api/meta/openapi.json`：OpenAPI 3.1 文档 + `components.schemas` 业务模型 + 参数 `x-refModel`/`x-dictCode` + 端点 `x-operation` + 顶层 `x-causes` |
| 因果链导出 | `MetaDocumentController` | `GET /api/meta/causes`（因果边）、`GET /api/meta/methods`（方法元数据）—— 供认知板块摄取 |
| 技术资源台账 | `ResourceController` + `ResourceCollector` + `ResourceProviderRegistry` | 各模块实现 `ResourceProvider`（在 `z-meta-api`）主动上报数据库/缓存/队列/模型服务/存储桶，z-meta 自动登记并落库 `z_resource`；`POST /api/meta/resource/collect` 触发采集，`GET /list` 查台账，`GET /providers` 查上报方花名册 |
| 健康检查 | `HealthController`（bean 名 `metaHealthController`） | 固定返回 `z-meta is running` |
| 模块数据源装配 | `MetaModuleDataSource`（继承 `z-boot-datasource-starter` 的 `ModuleDataSourceTemplate`） | `dataSourceMeta` + `sqlSessionFactoryMeta`，`@MapperScan("com.zifang.z.meta.core.domain.mapper")` |
| 自动装配 | `ZMetaAutoConfiguration`（`@ComponentScan("com.zifang.z.meta")`） | 经 `z-meta-web/src/main/resources/META-INF/spring.factories` 生效 |

> `00-overview.md` 里的"数据标准 / 数据血缘 / 数据质量"是**愿景**，代码里没有对应实现，README 不把它当功能宣称。

---

## 🏗️ 项目结构

```
z-meta/
├── pom.xml                # 根聚合 POM：继承 z-boot-parent:1.2.6，version 1.0.2
├── Dockerfile             # 后端镜像（eclipse-temurin:8-jdk，EXPOSE 8080）
├── LICENSE                # MIT，Copyright (c) 2026 z-opc-foundation
├── README.md              # 本文档
├── z-meta-api/            # 注解 API：@BizModel / @BizField / @BizMethod / @BizParam / @BizApi / @BizCause + ResourceProvider SPI，零实现零依赖（进 Central）
├── z-meta-core/           # 实体 + MyBatis Mapper + Service + BizModelCollector + ApiContractCollector + OpenApiDocumentGenerator + 资源采集（进 Central）
├── z-meta-web/            # HTTP 层：8 个 Controller + 自动装配 + 模块数据源（进 Central）
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
| 语言 / 运行时 | Java 17（`compile.version=17`；`z-boot-parent` 1.2.2 下发 17 口径） |
| 框架 | Spring Boot 2.7.18（由 `z-boot-parent` 自带地板供，本仓不再 import Boot BOM） |
| 持久层 | MyBatis-Plus 3.5.7 + Druid 1.2.23（均走 `z-boot-datasource-starter`，版本由父链下发） |
| 数据库驱动 | `com.mysql:mysql-connector-j:8.0.33`（本仓按坐标显式钉，并整条抄回 Boot 的 `protobuf-java` exclusion，防被地板抬到 8.4.0） |
| 其它显式钉版 | `org.yaml:snakeyaml:2.0`（地板不供这一条，不钉会静默落回 Boot 的 1.30） |
| 组织内依赖 | `io.github.yuku123`: `z-boot-datasource-starter` / `z-boot-web-starter`（`${z-boot.version}`=1.1.0）、`z-util-core`（版本走 `z-boot-parent` 的兄弟仓权威表）、`z-meta-api` / `z-meta-core` |
| 返回体 / 文档 | `com.zifang.util.core.meta.Result`（z-util）· Knife4j `knife4j-openapi3-spring-boot-starter:4.1.0`（父链下发） |
| 独立启动壳 | `spring-boot-starter-web` + `spring-boot-starter-log4j2`（admin 排除默认 Logback） |
| 前端 | React 19 + antd 6 + react-router-dom 7 + axios，Vite 6 构建，npm 工程（`_frontend/`，非 Maven reactor） |
| 构建 / 发布 | Maven（`central` profile：central-publishing-maven-plugin 0.7.0 + source 3.3.0 + javadoc 3.11.2 + gpg 3.2.7）· Docker |

---

## 🚀 快速开始

### 编译

```bash
mvn clean install -DskipTests
```

本仓**自包含**：parent 是 repo1 上的 `io.github.yuku123:z-boot-parent:1.2.6`，`<relativePath/>` 留空，
不需要任何"父项目目录"，也不需要 monorepo 时代的 `../pom.xml`。只要本机能解析到那条 parent 就能构建。
第三方版本一律由 `z-boot-parent:1.2.6` 供给（第三方地板与兄弟仓权威表均已并入 parent 自身），
模块 POM 里不应再出现字面版本钉。

### standalone 跑 admin

```bash
# 1) 先把 reactor 三件装进本地仓库（admin 依赖 z-meta-core，而 admin 不在 reactor）
mvn clean install -DskipTests

# 2) 单独构建 admin
mvn -f z-meta-admin/pom.xml clean package -DskipTests

# 3) 起服务：repackage 带 <classifier>exec</classifier>，可执行的是 -exec 那一只
java -jar z-meta-admin/target/z-meta-admin-1.0.2-exec.jar \
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
| `/api/interface` | `ApiController` | `GET /list`、`GET /get`、`GET /app/list`、`POST`、`PUT`、`DELETE` |
| `/api/private-config` | `PrivateConfigController` | `GET /list`、`GET /get`、`GET /get-by-key`、`POST`、`PUT`、`DELETE` |
| `/api/meta` | `BizModelController` | `GET /models`、`GET /models/{code}`、`GET /models/module/{module}` |
| `/api/meta` | `MetaDocumentController` | `GET /openapi.json`、`GET /causes`、`GET /methods` |
| `/api/meta/resource` | `ResourceController` | `POST /collect`、`GET /list`、`GET /providers` |
| `/api/health` | `HealthController` | `GET` → `z-meta is running` |

接口文档有两份，用途不同：

- **knife4j**（`z-meta-web` / `z-meta-admin` 均引该 starter，版本 4.1.0 由父链下发）：
  服务带 `/meta` 前缀启动后访问 `http://localhost:<port>/meta/doc.html`。
  本仓不产出 `swagger-ui.html`，历史 README 里那条地址已删。
- **自研 OpenAPI**（`GET /api/meta/openapi.json`）：路径与 HTTP 方法取自 Spring 映射注解，
  业务语义取自 `@Biz*` 注解——比 Swagger 多出 `components.schemas`（业务模型）、
  参数的 `x-refModel` / `x-dictCode`、端点的 `x-operation`、以及 Swagger 没有的顶层 `x-causes`（因果链）。

---

## 🧪 测试

测试代码在 [`z-meta-core/src/test/java/com/zifang/z/meta/core/ReadmeClaimsTest.java`](z-meta-core/src/test/java/com/zifang/z/meta/core/ReadmeClaimsTest.java)
—— README 主张自守卫：README 里每条定量主张（版本 / `<modules>` 条数与名单 / Controller 数与名单 /
`FieldType` 枚举值个数 / 建表清单 / 端口 / 前端依赖版本）都跟源码重算值逐条对账，README 改数字即红。

```bash
mvn test      # 会跑上面那个守卫
```

`z-meta-admin` 声明了 `spring-boot-starter-test` 却没有测试类（它也不在 `<modules>` 里，不参与 reactor）；
历史 README 承诺的"单元测试 / 集成测试 / `*PerformanceTest.java` 性能测试"在仓里没有对应文件，已删。
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
  而坐标已换成 `1.0.1` 且可执行件带 `exec` classifier → 现状会 COPY 失败。
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
| 2026-11-19 | **注解体系扩到六件 + 自研 OpenAPI + 资源台账并入**：新增 `@BizMethod`（方法业务语义/操作类型/幂等）、`@BizParam`（入参业务语义/位置/引用模型/字典）、`@BizApi`（端点契约：摘要/标签/废弃）、`@BizCause`（因果边：因为/前提/导致/权重）；`BizModelCollector` 扩到方法/参数/因果三级（新增 ASM 级 `MethodAnnotationTypeFilter`，Spring 自带过滤器只看类级注解会一个都扫不到）；新增 `ApiContractCollector` + `OpenApiDocumentGenerator`，产出比 Swagger 多业务语义的 OpenAPI 3.1 文档（`GET /api/meta/openapi.json`，含 `components.schemas` / `x-causes`）；z-asset 的主动上报能力（`ResourceProvider` SPI + 采集器 + `z_resource` 表）并入本仓。版本 `1.0.1` → `1.0.2`。 |
| 2026-09-30 | `release:` groupId `com.zifang` → `io.github.yuku123`、版本 `1.0.1-SNAPSHOT` → `1.0.1`，补 `central` / `central-dryrun` 发布 profile；四件已上 repo1 |
| 2026-09-30 | 三处 `com.zifang:z-boot-*-starter` 依赖换到 `io.github.yuku123` 发布坐标（clean-room 可构建） |
| 2026-09-29 | 消费入口抬到 `z-boot-parent:1.2.4`（消费者轮） |
| 2026-09-29 | 消费入口从幻影 parent `com.zifang:z-opc:1.0.1-SNAPSHOT`（`relativePath ../pom.xml`）换成 repo1 上的 `z-boot-parent` |
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
  - [`init.sql`](_doc/002_deploy/init.sql) — 建表脚本，实测含 `z_tenant`、`z_application`、`z_api`、
    `z_private_config`、`z_resource`，以及代码里没有对应实体的 `z_users` / `z_roles`
    （字典两张表已随字典能力迁出至 z-ctc，2026-11-18 从本脚本移除）

- [`_doc/003_script/`](_doc/003_script/) — 运维脚本：
  - [`build.sh`](_doc/003_script/build.sh)
  - [`package.sh`](_doc/003_script/package.sh)
  - [`start.sh`](_doc/003_script/start.sh)

- `_doc/004_skill/` — 目前为空目录，暂无 skill

---

<!-- z-opc-foundation-sibling-registry:v1 -->
## 🔗 兄弟仓全景（z-opc-foundation 组织）

本仓隶属于 [z-opc-foundation](https://github.com/orgs/z-opc-foundation/repositories) 组织——一人公司基座的全部自研件（现存 61 仓，含私有仓；口径 2026-11-24 GitHub org API 实读）。

| 仓 | 一句话说明 |
|---|---|
| [z-agent](https://github.com/z-opc-foundation/z-agent) | 云端 Agent 平台的库与 starter——ReAct/Plan 实现 + 注册中心 + REST 网关 + 只读控制面 |
| [z-agent-kernel](https://github.com/z-opc-foundation/z-agent-kernel) | Agent 框架基座——消息/工具/记忆/provider/会话 SPI + 轻量默认实现（纯 POJO 无 Spring） |
| [z-asset](https://github.com/z-opc-foundation/z-asset) | 企业资产台账——实物设备全生命周期 + 域名/证书/许可到期提醒 |
| [z-audit](https://github.com/z-opc-foundation/z-audit) | 审计与合规留痕——事件模型 + 哈希链 + 事件出口 SPI + 留存期策略 |
| [z-biz](https://github.com/z-opc-foundation/z-biz) | 商务外壳域——客户/合同/票据/对账四聚合 |
| [z-biz-erp](https://github.com/z-opc-foundation/z-biz-erp) | 进销存 ERP——商品/客商/采购/销售/库存流水/应收应付核销（z-biz 业务线） |
| [z-biz-security](https://github.com/z-opc-foundation/z-biz-security) | 业务数据安全防护入口——场景化策略注册 + 安全线四段流水线对外编排 |
| [z-boot](https://github.com/z-opc-foundation/z-boot) | Spring Boot Starter 聚合仓 + 版本权威（火车制）——唯一消费入口 z-boot-parent |
| [z-bot](https://github.com/z-opc-foundation/z-bot) | 本地 agent 应用——终端 CLI + HTTP/SSE web 控制台 + 多通道 gateway（飞书/钉钉/webhook） |
| [z-cache](https://github.com/z-opc-foundation/z-cache) | RESP2 协议兼容的分布式内存缓存——Java 8 + Netty，Redis 客户端零修改直连 |
| [z-camuda](https://github.com/z-opc-foundation/z-camuda) | 【已退役·归档】基于 Camunda 的工作流封装层（原 z-wf 改名，与现行 z-wf 无共同历史） |
| [z-cognition](https://github.com/z-opc-foundation/z-cognition) | 业务认知域/认知舱——术语/概念/证据链 + memory/kb/ontology 渐进式模块 |
| [z-config](https://github.com/z-opc-foundation/z-config) | 分布式配置中心 + 服务注册中心（类 Nacos）——Netty 长连接推送 + React 控制台 |
| [z-ctc](https://github.com/z-opc-foundation/z-ctc) | 4A 中心——认证/授权/账户/审计，多租户 JWT + SSO 跨子域 |
| [z-dms](https://github.com/z-opc-foundation/z-dms) | 数据源管控底座——统一登记、统一建池、统一探活、变更统一推送 |
| [z-env](https://github.com/z-opc-foundation/z-env) | 本地中间件与基础设施环境——31 个 docker-compose 场景 + make 一键运维 |
| [z-etl](https://github.com/z-opc-foundation/z-etl) | ETL 引擎——DataX 式线性作业 + Flink 式 DAG 流管道（键控状态/水位/窗口） |
| [z-ext](https://github.com/z-opc-foundation/z-ext) | Java 扩展点框架——@ExtPoint/@ExtImpl 运行期选实现，五档侵入方式 |
| [z-frontend-common](https://github.com/z-opc-foundation/z-frontend-common) | 全仓共享前端基座——AppLayout/LoginPage 等前端规范的唯一承载 |
| [z-frontend-markdown](https://github.com/z-opc-foundation/z-frontend-markdown) | Markdown 通用共享组件包（npm）——MarkdownLiveEditor/NoteMarkdown/MermaidBlock 单一真源 |
| [z-frontend-render](https://github.com/z-opc-foundation/z-frontend-render) | 平台共享前端渲染引擎——后端 schema 是唯一事实源，报表与低代码共用 |
| [z-graph](https://github.com/z-opc-foundation/z-graph) | 独立图数据库——MVCC 版本链存储 + OpenCypher 子集 + Bolt 风格协议 + React 控制台 |
| [z-gw](https://github.com/z-opc-foundation/z-gw) | 自研高性能 API 网关——Netty 手写运行时，Route/Predicate/Filter 模型可嵌 Spring Boot |
| [z-indexer](https://github.com/z-opc-foundation/z-indexer) | 代码索引服务——仓库注册表 + 定时重建索引 + 符号/调用链查询 |
| [z-introduce](https://github.com/z-opc-foundation/z-introduce) | 组织仓库展示站——全仓实测元数据汇总成可搜索静态站点 |
| [z-lc](https://github.com/z-opc-foundation/z-lc) | 低代码平台——DB 驱动通用 CRUD 引擎 + 报表/模型设计框架 |
| [z-llm-agent](https://github.com/z-opc-foundation/z-llm-agent) | CLI Agent 服务化——把本机 CLI agent（CC/Hermes/Qoder 等）暴露成 OpenAI 兼容对话 API |
| [z-llm-gateway](https://github.com/z-opc-foundation/z-llm-gateway) | LLM 网关——OpenAI/Anthropic 双协议面 + 多 vendor 凭据池、限流与 token 记账 |
| [z-llm-msghub](https://github.com/z-opc-foundation/z-llm-msghub) | msgHub 中心消息路由——WebSocket 中心枢纽 + Agent 侧客户端 SDK |
| [z-mask](https://github.com/z-opc-foundation/z-mask) | 脱敏与遮蔽引擎——按敏感命中位置遮蔽，策略即数据 |
| [z-mcp](https://github.com/z-opc-foundation/z-mcp) | MCP 服务端 + 上游聚合网关——Java 8 手写协议层，Spring Boot 自动装配 |
| [z-meta](https://github.com/z-opc-foundation/z-meta) | 元数据管理平台——租户/应用/接口清单登记 + @BizModel 业务模型采集 **（本仓）** |
| [z-mill](https://github.com/z-opc-foundation/z-mill) | 资料磨坊——全格式文档转 Markdown/JSON/纯文本/HTML（对标 MinerU） |
| [z-mist](https://github.com/z-opc-foundation/z-mist) | KMS 分布式密钥/秘密管理平台——集中加密存储 + Netty 协议 + starter 嵌入 |
| [z-mq](https://github.com/z-opc-foundation/z-mq) | 自研分布式消息队列——NameServer+Broker+Store，形状对标 RocketMQ、自有 Netty 协议 |
| [z-msg](https://github.com/z-opc-foundation/z-msg) | 多通道消息 + WebSocket 实时下发基础设施——短信/邮件/站内信/Webhook/IM 平台 |
| [z-observer](https://github.com/z-opc-foundation/z-observer) | 可观测性域——指标/日志/链路 + 告警引擎 |
| [z-opc](https://github.com/z-opc-foundation/z-opc) | 一人公司统一后端+统一前端——单 JVM all-in-one 承载协作/Agent/RPA/交易/资产等业务域与自研中间件 |
| [z-opc-foundation-lead](https://github.com/z-opc-foundation/z-opc-foundation-lead) | 组织治理大脑——事实表、强制规范、跨仓工具集、新机器恢复钥匙（本清单权威持有处） |
| [z-opcs](https://github.com/z-opc-foundation/z-opcs) | 任务撮合/服务市场——用户付费发布诉求，持有者接单完成，金额自动瓜分 |
| [z-ops](https://github.com/z-opc-foundation/z-ops) | 个人公司运维平台——部署单元/镜像构建/WebShell/K8s 巡检 + Ops Agent 执行器 |
| [z-oss](https://github.com/z-opc-foundation/z-oss) | 对象存储服务——自持 local 后端 + aliyun/s3 对接层，starter 注入 OssTemplate |
| [z-product](https://github.com/z-opc-foundation/z-product) | 产品设计信息汇总仓（开发域 PM 工件） |
| [z-qa](https://github.com/z-opc-foundation/z-qa) | 步骤式 API 链路测试平台——用例/计划/执行/报告/质量看板（对标 TestRail·MeterSphere） |
| [z-rpa](https://github.com/z-opc-foundation/z-rpa) | RPA 产品皮——应用市场/需求中心（执行归 z-script，触发归 z-schedule） |
| [z-rpc](https://github.com/z-opc-foundation/z-rpc) | Java 8 + Netty RPC 框架——微内核 + SPI 插件化，形状参照 Dubbo/gRPC |
| [z-schedule](https://github.com/z-opc-foundation/z-schedule) | 分布式任务调度中枢（对标 XXL-Job）——选主 + 时间环排期，starter 嵌入宿主 |
| [z-script](https://github.com/z-opc-foundation/z-script) | 多 DSL 脚本平台 + Mock 平台——沙箱执行/版本灰度/可注册 MCP Tool，starter 内嵌 |
| [z-security](https://github.com/z-opc-foundation/z-security) | 数据安全域编排层——场景→策略→识别→脱敏→水印→审计 四段流水线编排 |
| [z-sense](https://github.com/z-opc-foundation/z-sense) | 敏感识别与分类分级引擎——数据安全线底座（规则目录 + 可插拔识别器 + 分类分级） |
| [z-skill](https://github.com/z-opc-foundation/z-skill) | Skill 平台聚合器——归一化各家 Skill 形状，再按各家 API 形状发回 |
| [z-surl](https://github.com/z-opc-foundation/z-surl) | 短网址服务——短码生成/解析/命中统计，starter 一行集成 |
| [z-task](https://github.com/z-opc-foundation/z-task) | 任务协作平台——项目空间/看板/任务卡片（看板范式，对标 Trello·Teambition） |
| [z-team](https://github.com/z-opc-foundation/z-team) | 数字员工协同域——全栈独立仓，自 z-opc 毕业 |
| [z-trade](https://github.com/z-opc-foundation/z-trade) | 交易域独立仓——自 z-opc reactor 迁出 |
| [z-util](https://github.com/z-opc-foundation/z-util) | 底层 Java 工具库——89 个模块 POM，Java 8/17 口径，按需引用按需升级 |
| [z-vector](https://github.com/z-opc-foundation/z-vector) | 自研 Java 向量数据库——嵌入式+可独立部署，ANN+Payload 过滤，兼容 Qdrant REST |
| [z-voice-demo](https://github.com/z-opc-foundation/z-voice-demo) | 语音链路 demo——流式识别→kernel LLM→流式 TTS→可打断 的契约验证实现 |
| [z-watermark](https://github.com/z-opc-foundation/z-watermark) | 水印引擎——显式文字/图片隐写/盲水印，三种技术成对「打+验」 |
| [z-webide](https://github.com/z-opc-foundation/z-webide) | 企业级 WebIDE 后端——Git 仓/共享目录秒变 code-server 实例，全生命周期管理 |
| [z-wf](https://github.com/z-opc-foundation/z-wf) | 自研流程引擎——不依赖 Camunda/Flowable，定义层与运行时全自研 |

**已退役仓**（迁至 [z-trash](https://github.com/z-trash) 组织，archived 只读）：

| 仓 | 说明 |
|---|---|
| [z-agent-proxy](https://github.com/z-trash/z-agent-proxy) | 三方 coding agent 适配代理（Aider/Cursor → kernel Agent SPI），2026-10-26 退役 |
| [z-kb](https://github.com/z-trash/z-kb) | 自研知识库系统（对标 LightRAG/RAGFlow），2026-11 并入 z-cognition 认知舱后退役 |
| [z-ontology](https://github.com/z-trash/z-ontology) | 本体层（TBox/ABox/RDFS 推理），并入 z-cognition 后退役 |

另有 [z-report](https://github.com/z-opc-foundation/z-report)（报表域，2026-10-11 撤销并入 z-lc（远端已删除））。

总清单权威持有处在 [z-opc-foundation-lead](https://github.com/z-opc-foundation/z-opc-foundation-lead) `008_组织规范/005_兄弟仓清单.md`。
