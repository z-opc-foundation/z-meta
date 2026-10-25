import {useEffect, useState} from 'react'
import {Alert, Table, Tag} from 'antd'
import {metaApi} from '../services/api'
import {PagedTable} from '@yuku123/z-frontend-common/ui'

/** 字典项按需从 GET /api/dict/items/get 拉, 失败就把错误显示在展开行里, 不静默变空 */
function DictItems({dictCode}) {
    const [state, setState] = useState({loading: true, rows: [], error: null})

    useEffect(() => {
        let cancelled = false
        setState({loading: true, rows: [], error: null})
        metaApi.dictItemsByCode(dictCode)
            .then((rows) => {
                if (!cancelled) setState({loading: false, rows: rows || [], error: null})
            })
            .catch((e) => {
                if (!cancelled) setState({loading: false, rows: [], error: e})
            })
        return () => {
            cancelled = true
        }
    }, [dictCode])

    if (state.error) {
        return <Alert type="error" showIcon
                      message={`字典项加载失败：${state.error.message || state.error}`}/>
    }

    return (
        <Table
            size="small"
            rowKey="id"
            loading={state.loading}
            dataSource={state.rows}
            pagination={{ pageSize: 20, showSizeChanger: true, pageSizeOptions: ['10', '20', '50', '100'], showTotal: (t) => `共 ${t} 条` }}
            columns={[
                {title: '项编码', dataIndex: 'item_code', key: 'item_code', width: 180},
                {title: '项名称', dataIndex: 'item_name', key: 'item_name'},
                {title: '值', dataIndex: 'item_value', key: 'item_value', width: 120},
                {title: '排序', dataIndex: 'sort_order', key: 'sort_order', width: 80},
                {
                    title: '状态', dataIndex: 'status', key: 'status', width: 90,
                    render: (v) => (v === 1 ? <Tag color="success">启用</Tag> : <Tag>停用</Tag>)
                },
            ]}
            locale={{emptyText: `字典 ${dictCode} 下暂无字典项`}}
        />
    )
}

export default function DictList() {
    return (
        <PagedTable
            title="数据字典"
            description="字典类型台账；展开行按 dict_code 实时拉取字典项（GET /api/dict/type/list + /api/dict/items/get）"
            load={(pageNum, pageSize) => metaApi.dictTypePage(pageNum, pageSize)}
            columns={[
                {title: '字典编码', dataIndex: 'dict_code', key: 'dict_code', width: 200},
                {title: '字典名称', dataIndex: 'dict_name', key: 'dict_name'},
                {
                    title: '值类型', dataIndex: 'dict_type', key: 'dict_type', width: 110,
                    render: (v) => (v ? <Tag>{v}</Tag> : '-')
                },
                {
                    title: '状态', dataIndex: 'status', key: 'status', width: 90,
                    render: (v) => (v === 1 ? <Tag color="success">启用</Tag> : <Tag>停用</Tag>)
                },
                {title: '描述', dataIndex: 'description', key: 'description', ellipsis: true},
                {title: '更新时间', dataIndex: 'gmt_modified', key: 'gmt_modified', width: 170},
            ]}
            expandable={{
                expandedRowRender: (record) => <DictItems dictCode={record.dict_code}/>,
                rowExpandable: (record) => !!record.dict_code,
            }}
        />
    )
}
