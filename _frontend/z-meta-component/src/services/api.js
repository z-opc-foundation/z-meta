import request from './request'

/**
 * z-meta (元数据中心) 管理接口 — 与 z-opc 主进程 8888 上的 /api/meta* 一一对应。
 *
 * 口径: 这里的每条路径都经 /actuator/mappings 运行时核对过 (2026-09-24), 不是按命名猜的。
 * request 已解 {code,data,success} 包络, 业务层拿到的就是 data 本身。
 */
export const metaApi = {
    appPage: (pageNum, pageSize) => request.get('/meta-app/list', {params: {pageNum, pageSize}}),
    appGet: (id) => request.get('/meta-app/get', {params: {id}}),

    tenantPage: (pageNum, pageSize) => request.get('/meta-tenant/list', {params: {pageNum, pageSize}}),

    interfacePage: (pageNum, pageSize) => request.get('/interface/list', {params: {pageNum, pageSize}}),
    interfaceByApp: (appId) => request.get('/interface/app/list', {params: {appId}}),

    privateConfigPage: (pageNum, pageSize) => request.get('/private-config/list', {params: {pageNum, pageSize}}),

    bizModels: () => request.get('/meta/models'),
}
