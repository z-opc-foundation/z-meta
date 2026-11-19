# z-meta-component

z-meta（元数据中心）跨仓唯一前端面：把「租户 / 应用 / 数据字典 / 接口台账 / 私有配置 / 业务模型」
这一整套元数据页面收敛成一个可复用的 React 组件包。

- **npm 包名**：`@yuku123/z-meta-component`
- **宿主接入**：`@yuku123/z-meta-component/pages`（manifest + API），主壳可只取 `ComponentDocPage`
- **后端**：z-opc 主进程 8888 的元数据接口，请求实例 `baseURL='/meta'`，前缀由 `configureMeta` 注入
- **技术栈**：React 19 + antd 6 + react-router-dom 7 + vite（library mode，双入口 `.` / `./pages`）

---

## 一、能力概览

路由前缀 `/z-meta`，菜单与路由在 `src/pages-manifest.jsx` 里声明。

| 页面 | 路由（相对 `/z-meta/*`） | 源文件 | 能力 |
|------|--------------------------|--------|------|
| 登录 | `login` | `pages/login/Login.jsx` | 用户名密码登录（suit 侧单独挂这条路由） |
| 首页 | `home` | `pages/HomePage.jsx` | 入口卡片页 |
| 概览 | `dashboard` | `pages/Dashboard.jsx` | 租户 / 应用 / 字典 / 接口四张统计卡（当前为静态占位值） |
| 租户管理 | `tenant` | `pages/tenant/TenantList.jsx` | 租户台账的增删改查 |
| 应用管理 | `app` | `pages/application/AppList.jsx` | 应用台账的增删改查 |
| 接口管理 | `api` | `pages/api/ApiList.jsx` | 接口台账的增删改查 |
| 应用元数据（只读台账） | `*/app` | `pages/AppList.jsx` | 只读应用列表（基座 `PagedTable`） |
| 租户管理（只读台账） | `*/tenant` | `pages/TenantList.jsx` | 只读租户列表（基座 `PagedTable`） |
| 接口台账 | `*/interface` | `pages/InterfaceList.jsx` | 只读业务接口清单（基座 `PagedTable`） |
| 私有配置 | `*/private-config` | `pages/PrivateConfigList.jsx` | 只读应用私有键值配置（基座 `PagedTable`） |
| 业务模型 | `*/models` | `pages/BizModels.jsx` | 只读结构化业务模型清单（含字段数） |
| 组件文档 | `docs` | `pages/ComponentDoc.jsx` | 本文件渲染页（左侧 240px TOC + 右侧正文） |

`src/pages/MetaApp.jsx` 是只读台账一组的子路由挂载点（`app` / `tenant` / `interface` /
`private-config` / `models`），被 `routes` 里的 `/z-meta/:rest*` 承接，同时从包根具名导出。

> ⚠ **两套同名页面并存**：manifest 里 `/z-meta/tenant`、`/z-meta/app`、`/z-meta/api`
> 挂的是 `pages/{tenant,application,api}/` 下的**可写 CRUD 版**；`MetaApp` 里的
> `app` / `tenant` 挂的是 `pages/` 根目录下的**只读台账版**。路由不同、行为不同，别混。
> 字典页已于 2026-11-18 随字典能力整体迁往 z-ctc（`@yuku123/z-ctc-component` 的 DictPage，
> 后端 `/api/ctc/ac/dict`），z-meta 不再承载字典。

## 二、导出面

```js
// 主入口（. 桶）
import {configureMeta, MetaApp, ComponentDocPage} from '@yuku123/z-meta-component'

// 宿主组装面（./pages 桶）
import {menuItems, routes, HomePage, LoginPage, Dashboard} from '@yuku123/z-meta-component/pages'
```

`./pages` 桶 = `export * from './pages-manifest.jsx'` 的整份转发 + `configureMeta` 再导出。
**不要再手挑名字** —— 之前手挑过一次，manifest 新增的 `HomePage` / `LoginPage` 没被转发，
rollup 直接报 `LoginPage is not exported by dist/pages.js`。

## 三、API 端点

`src/services/api.js` 的 `metaApi`（下表是代码里写的路径，请求实例 `baseURL='/meta'`）：

| 方法 | 路径 | 说明 |
|---|---|---|
| GET | `/meta-app/list` | 应用列表（query `pageNum` / `pageSize`） |
| GET | `/meta-app/get` | 应用详情（query `id`） |
| GET | `/meta-tenant/list` | 租户列表（query `pageNum` / `pageSize`） |
| GET | `/dict/type/list` | 字典类型列表（query `pageNum` / `pageSize` / `dictName`） |
| GET | `/dict/items/get` | 按 `dict_code` 取字典项（query `dictCode`） |
| GET | `/dict/item/list` | 按字典类型 id 取字典项（query `dictId`） |
| GET | `/interface/list` | 接口台账列表 |
| GET | `/interface/app/list` | 按应用查接口（query `appId`） |
| GET | `/private-config/list` | 私有配置列表 |
| GET | `/meta/models` | 业务模型清单 |

可写 CRUD 版页面（`pages/{tenant,application,dict,api}/`）不走 `metaApi`，而是直接用
`services/request.js` 的实例，路径如下：

| 方法 | 路径 | 说明 |
|---|---|---|
| GET | `/tenant/list` | 租户列表 |
| POST | `/tenant` | 新建租户 |
| PUT | `/tenant/{id}` | 修改租户 |
| DELETE | `/tenant/{id}` | 删除租户 |
| GET | `/app/list` | 应用列表 |
| POST | `/app` | 新建应用 |
| PUT | `/app/{id}` | 修改应用 |
| DELETE | `/app/{id}` | 删除应用 |
| GET | `/interface/list` | 接口列表 |
| POST | `/interface` | 新建接口 |
| PUT | `/interface/{id}` | 修改接口 |
| DELETE | `/interface/{id}` | 删除接口 |
| GET | `/dict/type/list` | 字典类型列表 |
| POST | `/dict/type` | 新建字典类型 |
| PUT | `/dict/type/{id}` | 修改字典类型 |
| DELETE | `/dict/type/{id}` | 删除字典类型 |
| GET | `/dict/item/list` | 字典项列表 |
| POST | `/dict/item` | 新建字典项 |
| PUT | `/dict/item/{id}` | 修改字典项 |
| DELETE | `/dict/item/{id}` | 删除字典项 |

## 四、页面说明

### 4.1 概览

四张统计卡（租户数量 / 应用数量 / 字典数量 / 接口数量）。⚠ 当前卡里的数值是**写死的占位值**
（租户 1、其余 0），不接后端 —— 展示真实数值的页面是 `MetaApp` 下的只读台账。

### 4.2 租户 / 应用 / 接口管理（可写）

标准台账页：列表 + 新建 / 编辑弹窗 + 删除（`Popconfirm` 二次确认）。列表统一用基座 `ListTable`。

### 4.3 字典管理（可写）

两级结构：字典类型为父行，字典项按类型展开。类型与字典项各自支持增删改查。

### 4.4 只读台账一组

`pages/AppList.jsx` / `TenantList.jsx` / `InterfaceList.jsx` / `PrivateConfigList.jsx` /
`BizModels.jsx` 都是基座 `PagedTable` 的薄用法：给 `title` / `description` / `load` / `columns` 即可。
`description` 里写明了各自的数据源端点，方便排查。

### 4.5 数据字典（只读台账）

字典类型列表，展开行按 `dict_code` 实时拉字典项（`GET /dict/type/list` + `GET /dict/items/get`）。

### 4.6 业务模型

结构化业务模型清单，`fields` 一次性返回，表格里用 `(r.fields || []).length` 显示字段数。

### 4.7 组件文档

本文件。内容以 `?raw` 在编译期内联，零额外请求；左侧 240px TOC 随滚动联动。

## 五、配置

| 配置项 | 默认值 | 说明 |
|---|---|---|
| `configureMeta({apiBase})` | `'/meta'` | 改写请求实例的 `baseURL`，宿主换挂载前缀时用 |
| token 存储键 | `token` | 基座默认键，token 注入与 401 处理统一走共享拦截器 |
| 鉴权头 | `Authorization: Bearer` | 基座拦截器自动附加 |

## 六、已知限制

- **同名两套页面**（可写 CRUD 版 vs 只读台账版）并存，改动前先确认改的是哪一套，路由前缀不同。
- 概览页的统计数字是写死的占位值，不是实时数据。
- `api.js` 里 `metaApi` 的路径带了 `meta-` 段（如 `/meta-app/list`），叠加请求实例的 `baseURL='/meta'`
  后实际请求路径是 `/meta/meta-app/list`；而 CRUD 页直连的路径是 `/meta/app/list`。
  两套路径口径不同，**与后端真实路由的对应关系建议实测核对一次**（`api.js` 顶注称这些路径
  已在 2026-09-24 用 `/actuator/mappings` 核对过）。
- `metaApi` 里没有删除 / 更新方法，写操作全在页面里直连 request —— 收口 API 层时要一并搬。
- `api/ApiList.jsx` 仍用 antd 静态 `message`（antd 6 会告警），未迁到 `App.useApp()`。
- 组件包必须 external 基座；`vite.config.js` 的 `external` 是**精确字符串匹配**，
  `@yuku123/z-frontend-common/ui` 与 `/ui/echarts` 两个子路径必须逐个列出，漏一个基座就会被复制进 dist。

## 七、变更记录

| 版本 | 变更 |
|---|---|
| 0.1.1 | 新增组件文档页 `ComponentDocPage`（lead 008 §7）：`src/DOC.md` + 基座 `ComponentDoc` 薄包装 |
| 0.1.0 | 首版：元数据中心页面集 + manifest + 自洽 request |
