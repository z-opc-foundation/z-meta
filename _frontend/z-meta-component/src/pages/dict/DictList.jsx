import React, {useEffect, useState} from 'react';
import {Button, Form, Input, message, Modal, Popconfirm, Select, Space, Tabs} from 'antd';
import {DeleteOutlined, EditOutlined, PlusOutlined} from '@ant-design/icons';
import request from '../../services/request';
import {ErrorState, ListTable} from '@yuku123/z-frontend-common/ui';

const {Option} = Select;
const {TabPane} = Tabs;

function DictList() {
    // 字典类型状态
    const [dictTypeData, setDictTypeData] = useState([]);
    const [dictTypeLoading, setDictTypeLoading] = useState(false);
    const [dictTypePagination, setDictTypePagination] = useState({current: 1, pageSize: 10, total: 0});
    const [dictTypeModalVisible, setDictTypeModalVisible] = useState(false);
    const [editingDictType, setEditingDictType] = useState(null);
    const [dictTypeForm] = Form.useForm();
    // lead 008 §17：两栏各自常驻报错 + 重试，避免空表被误读成“没有字典”。
    const [typeError, setTypeError] = useState(null);

    // 字典项状态
    const [dictItemData, setDictItemData] = useState([]);
    const [dictItemLoading, setDictItemLoading] = useState(false);
    const [selectedDictId, setSelectedDictId] = useState(null);
    const [dictItemModalVisible, setDictItemModalVisible] = useState(false);
    const [editingDictItem, setEditingDictItem] = useState(null);
    const [dictItemForm] = Form.useForm();
    const [itemError, setItemError] = useState(null);

    // 获取字典类型列表
    const fetchDictType = async (page = 1, pageSize = 10) => {
        setDictTypeLoading(true);
        try {
            const res = await request.get('/dict/type/list', {
                params: {pageNum: page, pageSize},
            });
            setDictTypeData(res.data.records || []);
            setDictTypePagination({
                current: page,
                pageSize,
                total: res.data.total || 0,
            });
            setTypeError(null);
        } catch (error) {
            setTypeError(error);
        } finally {
            setDictTypeLoading(false);
        }
    };

    // 获取字典项列表
    const fetchDictItem = async (dictId) => {
        if (!dictId) return;
        setDictItemLoading(true);
        setSelectedDictId(dictId);
        try {
            const res = await request.get(`/dict/item/${dictId}`);
            setDictItemData(res.data || []);
            setItemError(null);
        } catch (error) {
            setItemError(error);
        } finally {
            setDictItemLoading(false);
        }
    };

    useEffect(() => {
        fetchDictType();
    }, []);

    // 字典类型操作
    const handleDictTypeAdd = () => {
        setEditingDictType(null);
        dictTypeForm.resetFields();
        dictTypeForm.setFieldsValue({status: 1, dictType: 'custom'});
        setDictTypeModalVisible(true);
    };

    const handleDictTypeEdit = (record) => {
        setEditingDictType(record);
        dictTypeForm.setFieldsValue(record);
        setDictTypeModalVisible(true);
    };

    const handleDictTypeDelete = async (id) => {
        try {
            await request.delete(`/dict/type/${id}`);
            message.success('删除成功');
            fetchDictType(dictTypePagination.current, dictTypePagination.pageSize);
        } catch (error) {
            message.error('删除失败');
        }
    };

    const handleDictTypeSubmit = async () => {
        try {
            const values = await dictTypeForm.validateFields();
            if (editingDictType) {
                await request.put(`/dict/type/${editingDictType.id}`, values);
                message.success('更新成功');
            } else {
                await request.post('/dict/type', values);
                message.success('创建成功');
            }
            setDictTypeModalVisible(false);
            fetchDictType(dictTypePagination.current, dictTypePagination.pageSize);
        } catch (error) {
            message.error('操作失败');
        }
    };

    // 字典项操作
    const handleDictItemAdd = () => {
        if (!selectedDictId) {
            message.warning('请先选择字典类型');
            return;
        }
        setEditingDictItem(null);
        dictItemForm.resetFields();
        dictItemForm.setFieldsValue({status: 1, dictId: selectedDictId, sortOrder: 0});
        setDictItemModalVisible(true);
    };

    const handleDictItemEdit = (record) => {
        setEditingDictItem(record);
        dictItemForm.setFieldsValue(record);
        setDictItemModalVisible(true);
    };

    const handleDictItemDelete = async (id) => {
        try {
            await request.delete(`/dict/item/${id}`);
            message.success('删除成功');
            fetchDictItem(selectedDictId);
        } catch (error) {
            message.error('删除失败');
        }
    };

    const handleDictItemSubmit = async () => {
        try {
            const values = await dictItemForm.validateFields();
            if (editingDictItem) {
                await request.put(`/dict/item/${editingDictItem.id}`, values);
                message.success('更新成功');
            } else {
                await request.post('/dict/item', values);
                message.success('创建成功');
            }
            setDictItemModalVisible(false);
            fetchDictItem(selectedDictId);
        } catch (error) {
            message.error('操作失败');
        }
    };

    const dictTypeColumns = [
        {title: 'ID', dataIndex: 'id', key: 'id', width: 60},
        {title: '字典编码', dataIndex: 'dictCode', key: 'dictCode'},
        {title: '字典名称', dataIndex: 'dictName', key: 'dictName'},
        {title: '字典类型', dataIndex: 'dictType', key: 'dictType', render: (t) => t === 'system' ? '系统' : '自定义'},
        {title: '状态', dataIndex: 'status', key: 'status', render: (s) => s === 1 ? '启用' : '禁用'},
        {title: '描述', dataIndex: 'description', key: 'description'},
        {
            title: '操作',
            key: 'action',
            render: (_, record) => (
                <Space>
                    <Button type="link" onClick={() => fetchDictItem(record.id)}>查看项</Button>
                    <Button type="link" icon={<EditOutlined/>} onClick={() => handleDictTypeEdit(record)}>编辑</Button>
                    <Popconfirm title="确定删除?" onConfirm={() => handleDictTypeDelete(record.id)}>
                        <Button type="link" danger icon={<DeleteOutlined/>}>删除</Button>
                    </Popconfirm>
                </Space>
            ),
        },
    ];

    const dictItemColumns = [
        {title: 'ID', dataIndex: 'id', key: 'id', width: 60},
        {title: '字典项编码', dataIndex: 'itemCode', key: 'itemCode'},
        {title: '字典项名称', dataIndex: 'itemName', key: 'itemName'},
        {title: '字典项值', dataIndex: 'itemValue', key: 'itemValue'},
        {title: '排序', dataIndex: 'sortOrder', key: 'sortOrder'},
        {title: '状态', dataIndex: 'status', key: 'status', render: (s) => s === 1 ? '启用' : '禁用'},
        {
            title: '操作',
            key: 'action',
            render: (_, record) => (
                <Space>
                    <Button type="link" icon={<EditOutlined/>} onClick={() => handleDictItemEdit(record)}>编辑</Button>
                    <Popconfirm title="确定删除?" onConfirm={() => handleDictItemDelete(record.id)}>
                        <Button type="link" danger icon={<DeleteOutlined/>}>删除</Button>
                    </Popconfirm>
                </Space>
            ),
        },
    ];

    return (
        <div>
            <Tabs defaultActiveKey="1">
                <TabPane tab="字典类型" key="1">
                    {/* 服务端分页，不套客户端分页：
                        - 分页对象保留原样（current/pageSize/total 由后端驱动，翻页重查接口），
                          只补 §2 规则 2 要求的 pageSizeOptions；
                        - 查询用基座 ListTable 自带的端侧过滤（后端 list 接口没有 keyword 参数，
                          过滤只作用在当前页已加载的数据上）；
                        - 空态由基座 <EmptyState/> 承接。 */}
                    {typeError && (
                        <ErrorState
                            error={typeError}
                            title="字典类型没有读到"
                            description="接口没有返回数据，下方空表不代表真的没有字典类型。"
                            onRetry={() => fetchDictType(dictTypePagination.current, dictTypePagination.pageSize)}
                            style={{marginBottom: 16}}
                        />
                    )}
                    <ListTable
                        rows={dictTypeData}
                        columns={dictTypeColumns}
                        loading={dictTypeLoading}
                        rowKey="id"
                        searchKeys="dictCode,dictName"
                        searchPlaceholder="按字典编码 / 名称过滤"
                        cardExtra={
                            <Button type="primary" icon={<PlusOutlined/>} onClick={handleDictTypeAdd}>
                                新增字典类型
                            </Button>
                        }
                        pagination={{
                            ...dictTypePagination,
                            showSizeChanger: true,
                            pageSizeOptions: ['10', '20', '50', '100'],
                            showTotal: (total) => `共 ${total} 条`,
                            onChange: (page, pageSize) => fetchDictType(page, pageSize),
                        }}
                        emptyTitle={typeError ? '数据没有读到' : '暂无字典类型'}
                        emptyDescription={typeError ? '上方错误条已说明原因，点「重新加载」重试。' : '当前没有字典类型，先点右上角「新增字典类型」建一个。'}
                    />
                </TabPane>
                <TabPane tab="字典项" key="2">
                    {/* 字典项是一次性拉全量（接口不带分页参数）⇒ 走基座默认的客户端分页，
                        不再自己写 pagination。 */}
                    {itemError && (
                        <ErrorState
                            error={itemError}
                            title="字典项没有读到"
                            description="接口没有返回数据，下方空表不代表真的没有字典项。"
                            onRetry={() => selectedDictId && fetchDictItem(selectedDictId)}
                            style={{marginBottom: 16}}
                        />
                    )}
                    <ListTable
                        rows={dictItemData}
                        columns={dictItemColumns}
                        loading={dictItemLoading}
                        rowKey="id"
                        searchKeys="itemCode,itemName,itemValue"
                        searchPlaceholder="按字典项编码 / 名称 / 值过滤"
                        cardExtra={
                            <Space>
                                <Button type="primary" icon={<PlusOutlined/>} onClick={handleDictItemAdd}>
                                    新增字典项
                                </Button>
                                {selectedDictId && <span>当前字典ID: {selectedDictId}</span>}
                            </Space>
                        }
                        emptyTitle={itemError ? '数据没有读到' : '暂无字典项'}
                        emptyDescription={itemError ? '上方错误条已说明原因，点「重新加载」重试。' : '先在「字典类型」里选一个类型，再新增字典项。'}
                    />
                </TabPane>
            </Tabs>

            {/* 字典类型弹窗 */}
            <Modal
                title={editingDictType ? '编辑字典类型' : '新增字典类型'}
                open={dictTypeModalVisible}
                onOk={handleDictTypeSubmit}
                onCancel={() => setDictTypeModalVisible(false)}
                width={600}
            >
                <Form form={dictTypeForm} layout="vertical">
                    <Form.Item name="tenantId" label="租户ID" initialValue={1}>
                        <Input type="number" placeholder="请输入租户ID"/>
                    </Form.Item>
                    <Form.Item name="dictCode" label="字典编码" rules={[{required: true}]}>
                        <Input placeholder="请输入字典编码"/>
                    </Form.Item>
                    <Form.Item name="dictName" label="字典名称" rules={[{required: true}]}>
                        <Input placeholder="请输入字典名称"/>
                    </Form.Item>
                    <Form.Item name="dictType" label="字典类型" initialValue="custom">
                        <Select>
                            <Option value="system">系统</Option>
                            <Option value="custom">自定义</Option>
                        </Select>
                    </Form.Item>
                    <Form.Item name="description" label="描述">
                        <Input.TextArea rows={2} placeholder="请输入描述"/>
                    </Form.Item>
                    <Form.Item name="status" label="状态" initialValue={1}>
                        <Select>
                            <Option value={1}>启用</Option>
                            <Option value={0}>禁用</Option>
                        </Select>
                    </Form.Item>
                </Form>
            </Modal>

            {/* 字典项弹窗 */}
            <Modal
                title={editingDictItem ? '编辑字典项' : '新增字典项'}
                open={dictItemModalVisible}
                onOk={handleDictItemSubmit}
                onCancel={() => setDictItemModalVisible(false)}
                width={600}
            >
                <Form form={dictItemForm} layout="vertical">
                    <Form.Item name="dictId" label="字典ID" hidden>
                        <Input/>
                    </Form.Item>
                    <Form.Item name="itemCode" label="字典项编码" rules={[{required: true}]}>
                        <Input placeholder="请输入字典项编码"/>
                    </Form.Item>
                    <Form.Item name="itemName" label="字典项名称" rules={[{required: true}]}>
                        <Input placeholder="请输入字典项名称"/>
                    </Form.Item>
                    <Form.Item name="itemValue" label="字典项值" rules={[{required: true}]}>
                        <Input placeholder="请输入字典项值"/>
                    </Form.Item>
                    <Form.Item name="sortOrder" label="排序">
                        <Input type="number" placeholder="请输入排序"/>
                    </Form.Item>
                    <Form.Item name="status" label="状态" initialValue={1}>
                        <Select>
                            <Option value={1}>启用</Option>
                            <Option value={0}>禁用</Option>
                        </Select>
                    </Form.Item>
                </Form>
            </Modal>
        </div>
    );
}

export default DictList;