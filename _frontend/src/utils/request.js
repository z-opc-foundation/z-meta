/**
 * z-meta 请求实例
 *
 * 从 @yuku123/z-frontend-common 工厂创建，baseURL 指向本模块的 /meta 前缀。
 * token 注入 + 401 处理逻辑统一走共享拦截器（localStorage 'token' key）。
 */
import {createRequest} from '@yuku123/z-frontend-common'

const request = createRequest({baseURL: '/meta'})

export default request
