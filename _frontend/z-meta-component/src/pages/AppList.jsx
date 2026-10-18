import {Tag} from 'antd'
import {metaApi} from '../services/api'
import {PagedTable} from '@yuku123/z-frontend-common/ui'

const statusTag = (v) => (v === 1 ? <Tag color="success">启用</Tag> : <Tag color="default">停用</Tag>)

export default function AppList() {
    return (
        <PagedTable
            title="应用元数据"
            description="z-meta 登记在册的应用（数据源 GET /api/meta-app/list）"
            load={(pageNum, pageSize) => metaApi.appPage(pageNum, pageSize)}
            columns={[
                {title: '应用编码', dataIndex: 'app_code', key: 'app_code', width: 180},
                {title: '应用名称', dataIndex: 'app_name', key: 'app_name'},
                {title: '类型', dataIndex: 'app_type', key: 'app_type', width: 100, render: (v) => <Tag>{v}</Tag>},
                {
                    title: '入口地址', dataIndex: 'base_url', key: 'base_url', ellipsis: true,
                    render: (v) => v || '-'
                },
                {title: '负责人', dataIndex: 'owner', key: 'owner', width: 110},
                {title: '状态', dataIndex: 'status', key: 'status', width: 90, render: statusTag},
                {title: '描述', dataIndex: 'description', key: 'description', ellipsis: true},
                {title: '创建时间', dataIndex: 'gmt_create', key: 'gmt_create', width: 170},
            ]}
        />
    )
}
