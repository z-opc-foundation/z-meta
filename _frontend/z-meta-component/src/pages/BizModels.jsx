import {useCallback, useEffect, useState} from 'react'
import {Input, Table, Tag} from 'antd'
import {metaApi} from '../services/api'
import {PagedTable} from '@yuku123/z-frontend-common/ui'

export default function BizModels() {
    // lead 008 §2 规则 3：客户端查询过滤。
    //
    // 为什么过滤写在 load 里、过滤框挂在本页（基座 PagedTable 之外）：
    //   - 基座 PagedTable 自己渲染 Card，数据只存在它的 state 里，页面拿不到 rows，
    //     没法像 z-gw RouteList 那样在渲染期直接算出 filteredRows；
    //   - load 是本页唯一能插手数据的地方：/meta/models 一次返回全部模型，
    //     这里在客户端按关键字把整份清单筛一遍（过滤覆盖全集，不是只当前页）；
    //   - PagedTable 只在挂载/翻页时取数，关键字变化靠 key 重挂载触发重新取数，
    //     所以过滤框必须放在重挂载范围之外，否则每敲一个字输入框就被重建、焦点丢失。
    const [keyword, setKeyword] = useState('')
    const [applied, setApplied] = useState('')

    // 输入防抖 300ms（与基座 SearchInput 同口径），避免每敲一个字就重挂载重取数
    useEffect(() => {
        const timer = setTimeout(() => setApplied(keyword), 300)
        return () => clearTimeout(timer)
    }, [keyword])

    const load = useCallback(async () => {
        const res = await metaApi.bizModels()
        const rows = Array.isArray(res) ? res : (res?.records || [])
        const kw = applied.trim().toLowerCase()
        if (!kw) return rows
        // 参与匹配的字段取自本页 columns 真实存在的 dataIndex：code / name / module
        return rows.filter((m) => `${m.code || ''} ${m.name || ''} ${m.module || ''}`.toLowerCase().includes(kw))
    }, [applied])

    return (
        <div>
            <div style={{display: 'flex', justifyContent: 'flex-end', marginBottom: 8}}>
                <Input.Search allowClear placeholder="按模型编码 / 名称 / 所属模块过滤" style={{width: 240}}
                              value={keyword} onChange={(e) => setKeyword(e.target.value)}/>
            </div>
            <PagedTable
                key={applied}
                title="业务模型"
                description="z-meta 的结构化业务模型清单（数据源 GET /api/meta/models，字段随模型一次性返回）"
                load={load}
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
                            pagination={false /* §2 规则 2 豁免：展开行子表：单个业务模型的字段明细，随父行联动，条数由该模型字段数决定；分页会把「父行 + 展开行」拆到两页，看不出字段属于哪个模型 */}
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
        </div>
    )
}
