# z-meta 控制台首页四个"数量"是源码字面量，不接任何数据源

日期 2026-10-01。发现方式：把 `_frontend` 整份 rsync 到仓外副本装依赖、起 vite、用 Playwright 真点击普查
（量具 `~/.cache/zopc_ui_sweep/sweep/sweep_console.js`，账本 `click_z-meta.jsonl` / `click_z-meta_p2.jsonl`）。
本轮只点不改：非 GET 请求在浏览器层一律 `abort` 并记账，页面上没有打过任何一个字。

## 1. 现象（浏览器实测，不是读代码推的）

`/dashboard` 渲染出的正文（逐字抄自 `body.innerText`）：

```
z-meta 元数据平台 首页 租户管理 应用管理 字典管理 接口管理 欢迎使用 z-meta 元数据平台 租户数量 1 应用数量 0 字典类型 0 接口数量 0
```

同一轮里点进四个列表页，全部拿不到数据，页面自己弹提示：

| 路由 | 控件数 | 表格行 | 页面提示 | 网络 |
|---|---|---|---|---|
| `/dashboard` | 5 | 0 | 无 | 0 条 |
| `/tenant` | — | 1 | `获取数据失败` | 500 |
| `/app` | — | 1 | `获取数据失败` | 500 |
| `/dict` | — | 1 | `获取字典类型失败` | 500 |
| `/api` | — | 1 | `获取数据失败` | 500 |

⇒ 全站在这一刻**一条后端数据都没拿到**，而首页却报出"租户数量 1"。

## 2. 根因：那四个数字是写死的常量

现读命令与读数（本仓 `_frontend/`）：

```
$ grep -n 'value={' src/pages/Dashboard.jsx
14:                            value={1}
23:                            value={0}
32:                            value={0}
41:                            value={0}
```

`Dashboard.jsx` 全文只做一件事：渲染四个 antd `<Statistic>`，文件里 **0 次** `import` 任何 api/request 模块、
**0 次** `useEffect`/`fetch`（同条 grep 里 `import|fetch|api|useEffect` 只命中 1/2/3 行的
React、antd、图标）。"租户数量 1"和它下面三个 0 是源码里的字面量，与库里到底有几个租户没有因果关系。

对照：另外四个列表页是**接了真接口的**（`src/utils/request.js:9` `createRequest({baseURL: '/meta'})`，
`vite.config.js` 把 `/meta` 代理到 `http://localhost:8080`）。所以本轮那些 500 是"后端没起"
（`lsof -nP -iTCP:8080 -sTCP:LISTEN` 空、`curl http://127.0.0.1:8080/...` 拿 000），
不是本模块的缺陷——这一条要说清楚，免得下一个读的人去修错地方。

## 3. 为什么这比"接口没通"更值得修

接口没通，用户看得见：列表页自己弹了 `获取数据失败`。
首页看不见：它给的是一个长得完全一样的统计卡片，标题写着"租户数量"，值恰好是个非零的 `1`——
`1` 比 `0` 更容易被读成"我确实建过一个租户"。同一个界面上一边说"租户数量 1"一边说"获取数据失败"，
用户没有第三来源可以判断哪句是真的。

同类形状在本项目群里已经是第三例（z-mq 管理台的 mock 把所有失败咽成成功提示，
记在 `z-mq/_doc/007_backlog/feature013_console_mock_swallows_every_failure/`），
所以这不只是"首页还没写完"，是一个会持续复发的模式。

## 4. 建议（未动代码，等拍板）

1. 最低成本：`Dashboard.jsx` 的四个 `<Statistic>` 改成接 `/meta/**` 的真实计数，取不到时显示 `-` 而不是数字。
   antd `<Statistic value={null}>` 会渲染空，需要显式给 `formatter` 或退化成文本。
2. 若首页本就不该有统计：把四张卡删掉，别留"看起来像数据"的形状。
3. 加一条门禁尺（可以很小）：`grep -n 'value={[0-9]' src/pages/*.jsx src/pages/**/*.jsx` 命中即红——
   统计卡片的值必须由变量提供。判据要在 CI 里，否则下一个模块还会写死。
4. 本轮另有一处量具教训与本仓无关，记在普查台账：静态抽取路由表时**只认前导斜杠会漏掉整层嵌套子路由**。
   `src/App.jsx:37-42` 的 index/dashboard/tenant/app/dict/api 全是相对 `path`，
   第一遍只抽到 `/login` 与 `/` 两条（点了 8 下）；改成"第二遍把应用自己导航到的 URL 补进分母"后
   变成 7 条路由 / 39 次点击。

## 5. 复跑

```
cd ~/.cache/zopc_ui_sweep/sweep && python3 -u console_click_probe.py z-meta
```

副本在 `~/.cache/zopc_ui_sweep/consoles/z-meta___frontend`（刻意装在仓库外：
`git check-ignore` 实测本仓 `.gitignore` 不覆盖 `node_modules`，原地装会在共享工作树留上万条未跟踪文件）。
