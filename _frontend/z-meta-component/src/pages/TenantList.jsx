import {Tag} from 'antd'
import {metaApi} from '../services/api'
import {PagedTable} from '@yuku123/z-frontend-common/ui'

export default function TenantList() {
    return (
        <PagedTable
            title="租户管理"
            description="z-meta 租户台账（数据源 GET /api/meta-tenant/list）"
            load={(pageNum, pageSize) => metaApi.tenantPage(pageNum, pageSize)}
            columns={[
                {title: '租户编码', dataIndex: 'tenant_code', key: 'tenant_code', width: 160},
                {title: '租户名称', dataIndex: 'tenant_name', key: 'tenant_name'},
                {
                    title: '类型', dataIndex: 'tenant_type', key: 'tenant_type', width: 110,
                    render: (v) => (v ? <Tag>{v}</Tag> : '-')
                },
                {
                    title: '状态', dataIndex: 'status', key: 'status', width: 90,
                    render: (v) => (v === 1 ? <Tag color="success">正常</Tag> : <Tag>停用</Tag>)
                },
                {title: '到期时间', dataIndex: 'expire_time', key: 'expire_time', width: 170, render: (v) => v || '长期'},
                {title: '描述', dataIndex: 'description', key: 'description', ellipsis: true},
                {title: '创建时间', dataIndex: 'gmt_create', key: 'gmt_create', width: 170},
            ]}
        />
    )
}
