import {Tooltip, Typography} from 'antd'
import {metaApi} from '../services/api'
import {PagedTable} from '@/common/components/ui'

export default function PrivateConfigList() {
    return (
        <PagedTable
            title="私有配置"
            description="应用私有键值配置（数据源 GET /api/private-config/list）"
            load={(pageNum, pageSize) => metaApi.privateConfigPage(pageNum, pageSize)}
            columns={[
                {title: '配置键', dataIndex: 'config_key', key: 'config_key', width: 240},
                {
                    title: '配置值', dataIndex: 'config_value', key: 'config_value', ellipsis: true,
                    render: (v) => v
                        ? <Tooltip title={v}><Typography.Text code copyable={{text: v}}>{v}</Typography.Text></Tooltip>
                        : '-'
                },
                {title: '描述', dataIndex: 'description', key: 'description', ellipsis: true},
                {title: '更新时间', dataIndex: 'gmt_modified', key: 'gmt_modified', width: 170},
            ]}
        />
    )
}
