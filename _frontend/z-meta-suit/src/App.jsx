import {Navigate, Route, Routes} from 'react-router-dom'
import {AppLayout} from '../../../../_shared/z-frontend-common-local/dist/z-frontend-common.es.js'
import {Dashboard, Login, menuItems, routeTable} from '@yuku123/z-meta-component/pages'

// 壳只做组装（lead 005 §9.1）：AppLayout + manifest 路由（z-meta 无鉴权守卫）。
export default function App() {
    return (
        <Routes>
            <Route path="/login" element={<Login/>}/>
            <Route path="/" element={
                <AppLayout menuItems={menuItems} appTitle="z-meta 元数据平台" appShort="META" appIcon={{icon: <img src="/icon.png" alt="META" style={{width: "100%", height: "100%", objectFit: "cover", borderRadius: 8}}/>, color: '#4338ca', label: 'META'}}/>
            }>
                <Route index element={<Navigate to="/dashboard" replace/>}/>
                {routeTable.map((r) => (
                    <Route key={r.path} path={r.path} element={<r.Component/>}/>
                ))}
            </Route>
        </Routes>
    )
}
