import { ApiOutlined, AppstoreOutlined, DashboardOutlined, HomeOutlined, ReadOutlined, UserOutlined } from '@ant-design/icons'
import Login from './pages/login/Login'
import Dashboard from './pages/Dashboard'
import TenantList from './pages/tenant/TenantList'
import AppList from './pages/application/AppList'
import ApiList from './pages/api/ApiList'

/** 菜单 + 路由清单（lead 005 §8.2 manifest）。App 壳在 suit/宿主侧组装。 */

export {default as Dashboard} from './pages/Dashboard'
export {default as Login} from './pages/login/Login'
import HomePage from './pages/HomePage'
import MetaApp from './pages/MetaApp.jsx'
import ComponentDocPage from './pages/ComponentDoc.jsx'

/** 菜单 + 路由清单（lead 008 §10/§14/§16 批量落地）。App 壳在 suit 侧组装。 */
export const appMeta = { title: 'z-meta 元数据平台', short: 'z-meta' }

export const menuItems = [
    { key: '/z-meta/home', label: '首页', icon: <HomeOutlined /> },
    { key: '/z-meta/dashboard', label: '概览', icon: <DashboardOutlined /> },
    { key: '/z-meta/tenant', label: '租户管理', icon: <UserOutlined /> },
    { key: '/z-meta/app', label: '应用管理', icon: <AppstoreOutlined /> },
    { key: '/z-meta/api', label: '接口管理', icon: <ApiOutlined /> },
    { key: '/z-meta/docs', label: '组件文档', icon: <ReadOutlined /> },
]

export const routes = [
    { path: '/z-meta/home', Component: HomePage },
    { path: '/z-meta/dashboard', Component: Dashboard },
    { path: '/z-meta/tenant', Component: TenantList },
    { path: '/z-meta/app', Component: AppList },
    { path: '/z-meta/api', Component: ApiList },
    { path: '/z-meta/docs', Component: ComponentDocPage },
    { path: '/z-meta/:rest*', Component: MetaApp },
]

export { default as HomePage } from './pages/HomePage'
export { default as LoginPage } from './pages/LoginPage'
// 主壳接线用：文档页统一以 ComponentDocPage 这个名字从主入口导出。
export { default as ComponentDocPage } from './pages/ComponentDoc.jsx'
