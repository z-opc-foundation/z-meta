package com.zifang.z.meta.core.domain.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.zifang.z.meta.core.domain.entity.ZTenantDO;

/**
 * 租户 Service
 */
public interface ITenantService extends IService<ZTenantDO> {

    /**
     * 分页查询租户
     */
    IPage<ZTenantDO> pageTenant(Page<ZTenantDO> page, ZTenantDO tenant);

    /**
     * 根据租户编码查询
     */
    ZTenantDO getByTenantCode(String tenantCode);
}