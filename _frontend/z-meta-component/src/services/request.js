/**
 * z-meta 请求实例
 *
 * 从 @yuku123/z-frontend-common 工厂创建，baseURL 指向本模块的 /meta 前缀。
 * token 注入 + 401 处理逻辑统一走共享拦截器（localStorage 'token' key）。
 *
 * 配置：
 *   - baseURL '/meta'  →  路径走 /meta/tenant、/meta/app、/meta/dict、/meta/api
 */
import {createRequest} from '@yuku123/z-frontend-common'

const request = createRequest({baseURL: '/meta'})

export default request

// 前缀参数注入（lead 005 section 9.3）：默认 '/meta'（与原行为一致）。
// 宿主换挂载前缀时 configureMeta({ apiBase })，直接改写实例 defaults。
export function configureMeta(config) {
    if (config && config.apiBase !== undefined) {
        request.defaults.baseURL = config.apiBase
    }
}
