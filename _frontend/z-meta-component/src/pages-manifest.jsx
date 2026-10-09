import {
    ApiOutlined, AppstoreOutlined, BookOutlined, DashboardOutlined, UserOutlined,
} from '@ant-design/icons'
import Login from './pages/login/Login'
import Dashboard from './pages/Dashboard'
import TenantList from './pages/tenant/TenantList'
import AppList from './pages/application/AppList'
import DictList from './pages/dict/DictList'
import ApiList from './pages/api/ApiList'

/** 菜单 + 路由清单（lead 005 §8.2 manifest）。App 壳在 suit/宿主侧组装。 */
export const menuItems = [
    {key: '/dashboard', icon: <DashboardOutlined/>, label: '首页'},
    {key: '/tenant', icon: <UserOutlined/>, label: '租户管理'},
    {key: '/app', icon: <AppstoreOutlined/>, label: '应用管理'},
    {key: '/dict', icon: <BookOutlined/>, label: '字典管理'},
    {key: '/api', icon: <ApiOutlined/>, label: '接口管理'},
]

const routeTable = [
    {path: 'dashboard', Component: Dashboard},
    {path: 'tenant', Component: TenantList},
    {path: 'app', Component: AppList},
    {path: 'dict', Component: DictList},
    {path: 'api', Component: ApiList},
]
export {routeTable}
export {default as Dashboard} from './pages/Dashboard'
export {default as Login} from './pages/login/Login'
