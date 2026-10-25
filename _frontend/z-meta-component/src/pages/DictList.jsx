import {useCallback, useEffect, useState} from 'react'
import {Alert, Input, Table, Tag} from 'antd'
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
    // lead 008 §2 规则 3：客户端查询过滤。
    //
    // 为什么过滤写在 load 里、过滤框挂在本页（基座 PagedTable 之外）：
    //   - 基座 PagedTable 自己渲染 Card，数据只存在它的 state 里，页面拿不到 rows，
    //     没法像 z-gw RouteList 那样在渲染期直接算出 filteredRows；
    //   - load 是本页唯一能插手数据的地方：后端 /dict/type/list 不接关键字参数，
    //     这里在客户端把**当前页已加载的记录**按关键字筛一遍（与 z-task 字典页同一口径）；
    //   - PagedTable 只在挂载/翻页时取数，关键字变化靠 key 重挂载触发重新取数，
    //     所以过滤框必须放在重挂载范围之外，否则每敲一个字输入框就被重建、焦点丢失。
    const [keyword, setKeyword] = useState('')
    const [applied, setApplied] = useState('')

    // 输入防抖 300ms（与基座 SearchInput 同口径），避免每敲一个字就重挂载重取数
    useEffect(() => {
        const timer = setTimeout(() => setApplied(keyword), 300)
        return () => clearTimeout(timer)
    }, [keyword])

    const load = useCallback(async (pageNum, pageSize) => {
        const res = await metaApi.dictTypePage(pageNum, pageSize)
        const kw = applied.trim().toLowerCase()
        if (!kw) return res
        // 参与匹配的字段取自本页 columns 真实存在的 dataIndex：dict_code / dict_name / dict_type
        const records = (res?.records || []).filter((r) =>
            `${r.dict_code || ''} ${r.dict_name || ''} ${r.dict_type || ''}`.toLowerCase().includes(kw))
        // total 跟着过滤后的条数走：过滤只作用在当前页，分页条数按过滤后的行数给
        return {...res, records, total: records.length}
    }, [applied])

    return (
        <div>
            <div style={{display: 'flex', justifyContent: 'flex-end', marginBottom: 8}}>
                <Input.Search allowClear placeholder="按字典编码 / 名称 / 类型过滤" style={{width: 240}}
                              value={keyword} onChange={(e) => setKeyword(e.target.value)}/>
            </div>
            <PagedTable
                key={applied}
                title="数据字典"
                description="字典类型台账；展开行按 dict_code 实时拉取字典项（GET /api/dict/type/list + /api/dict/items/get）"
                load={load}
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
        </div>
    )
}
