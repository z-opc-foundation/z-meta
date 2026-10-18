import {Table, Tag} from 'antd'
import {metaApi} from '../services/api'
import {PagedTable} from '@yuku123/z-frontend-common/ui'

export default function BizModels() {
    return (
        <PagedTable
            title="业务模型"
            description="z-meta 的结构化业务模型清单（数据源 GET /api/meta/models，字段随模型一次性返回）"
            load={() => metaApi.bizModels()}
            rowKey="code"
            columns={[
                {title: '模型编码', dataIndex: 'code', key: 'code', width: 200},
                {title: '模型名称', dataIndex: 'name', key: 'name', width: 180},
                {
                    title: '所属模块', dataIndex: 'module', key: 'module', width: 160,
                    render: (v) => <Tag color="blue">{v}</Tag>
                },
                {title: '实体类', dataIndex: 'class_name', key: 'class_name', ellipsis: true},
                {
                    title: '字段数', key: 'fieldCount', width: 90,
                    render: (_, r) => (r.fields || []).length
                },
                {title: '描述', dataIndex: 'description', key: 'description', ellipsis: true},
            ]}
            expandable={{
                rowExpandable: (r) => (r.fields || []).length > 0,
                expandedRowRender: (r) => (
                    <Table
                        size="small"
                        rowKey={(f) => `${r.code}:${f.field_name}`}
                        dataSource={r.fields || []}
                        pagination={false}
                        columns={[
                            {title: '字段名', dataIndex: 'name', key: 'name', width: 160},
                            {title: '属性', dataIndex: 'field_name', key: 'field_name', width: 200},
                            {title: '类型', dataIndex: 'type', key: 'type', width: 120, render: (v) => <Tag>{v}</Tag>},
                            {title: '关联模型', dataIndex: 'ref_model', key: 'ref_model', render: (v) => v || '-'},
                            {title: '字典', dataIndex: 'dict_code', key: 'dict_code', render: (v) => v || '-'},
                        ]}
                    />
                ),
            }}
        />
    )
}
