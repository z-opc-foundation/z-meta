package com.zifang.z.meta.core.domain.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.zifang.z.meta.core.domain.entity.ZApplicationDO;

/**
 * 应用 Service
 */
public interface IApplicationService extends IService<ZApplicationDO> {

    /**
     * 分页查询应用
     */
    IPage<ZApplicationDO> pageApplication(Page<ZApplicationDO> page, ZApplicationDO application);

    /**
     * 根据租户和应用编码查询
     */
    ZApplicationDO getByAppCode(Long tenantId, String appCode);
}