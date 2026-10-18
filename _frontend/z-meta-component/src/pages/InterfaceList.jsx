import {Tag} from 'antd'
import {metaApi} from '../services/api'
import {PagedTable} from '@yuku123/z-frontend-common/ui'

export default function InterfaceList() {
    return (
        <PagedTable
            title="接口台账"
            description="z-meta 登记的业务接口清单（数据源 GET /api/interface/list）"
            load={(pageNum, pageSize) => metaApi.interfacePage(pageNum, pageSize)}
            columns={[
                {title: '接口名称', dataIndex: 'api_name', key: 'api_name'},
                {title: '方法', dataIndex: 'api_method', key: 'api_method', width: 90, render: (v) => <Tag>{v}</Tag>},
                {title: '路径', dataIndex: 'api_path', key: 'api_path', ellipsis: true},
                {title: '版本', dataIndex: 'api_version', key: 'api_version', width: 90},
                {title: '鉴权', dataIndex: 'auth_type', key: 'auth_type', width: 120},
                {
                    title: '状态', key: 'status', width: 120,
                    render: (_, r) => (r.deprecated === 1
                        ? <Tag color="warning">已废弃</Tag>
                        : (r.status === 1 ? <Tag color="success">启用</Tag> : <Tag>停用</Tag>))
                },
                {title: '描述', dataIndex: 'description', key: 'description', ellipsis: true},
            ]}
        />
    )
}
