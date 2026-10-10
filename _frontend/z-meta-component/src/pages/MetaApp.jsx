import {Navigate, Route, Routes} from 'react-router-dom'
import AppList from './AppList'
import TenantList from './TenantList'
import DictList from './DictList'
import InterfaceList from './InterfaceList'
import PrivateConfigList from './PrivateConfigList'
import BizModels from './BizModels'

/** z-meta 元数据中心 — OpsWorkbench 以 /meta/* 通配挂进来，子路由在这里收口 */
export default function MetaApp() {
    return (
        <Routes>
            <Route index element={<Navigate to="app" replace/>}/>
            <Route path="app" element={<AppList/>}/>
            <Route path="tenant" element={<TenantList/>}/>
            <Route path="dict" element={<DictList/>}/>
            <Route path="interface" element={<InterfaceList/>}/>
            <Route path="private-config" element={<PrivateConfigList/>}/>
            <Route path="models" element={<BizModels/>}/>
        </Routes>
    )
}
