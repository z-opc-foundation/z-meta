import React from 'react';
import {BrowserRouter, Navigate, Route, Routes} from 'react-router-dom';
import {ConfigProvider} from 'antd';
import zhCN from 'antd/locale/zh_CN';
import {AppLayout} from '@yuku123/z-frontend-common';
import {ApiOutlined, AppstoreOutlined, BookOutlined, DashboardOutlined, UserOutlined,} from '@ant-design/icons';

import Login from './pages/login';
import Dashboard from './pages/Dashboard';
import TenantList from './pages/tenant/TenantList';
import AppList from './pages/application/AppList';
import DictList from './pages/dict/DictList';
import ApiList from './pages/api/ApiList';
import './App.css';

const menuItems = [
    {key: '/dashboard', icon: <DashboardOutlined/>, label: '首页'},
    {key: '/tenant', icon: <UserOutlined/>, label: '租户管理'},
    {key: '/app', icon: <AppstoreOutlined/>, label: '应用管理'},
    {key: '/dict', icon: <BookOutlined/>, label: '字典管理'},
    {key: '/api', icon: <ApiOutlined/>, label: '接口管理'},
];

function App() {
    return (
        <ConfigProvider locale={zhCN}>
            <BrowserRouter>
                <Routes>
                    <Route path="/login" element={<Login/>}/>
                    <Route path="/" element={
                        <AppLayout
                            menuItems={menuItems}
                            appTitle="z-meta 元数据平台"
                            appShort="META"
                        />
                    }>
                        <Route index element={<Navigate to="/dashboard" replace/>}/>
                        <Route path="dashboard" element={<Dashboard/>}/>
                        <Route path="tenant" element={<TenantList/>}/>
                        <Route path="app" element={<AppList/>}/>
                        <Route path="dict" element={<DictList/>}/>
                        <Route path="api" element={<ApiList/>}/>
                    </Route>
                </Routes>
            </BrowserRouter>
        </ConfigProvider>
    );
}

export default App;
