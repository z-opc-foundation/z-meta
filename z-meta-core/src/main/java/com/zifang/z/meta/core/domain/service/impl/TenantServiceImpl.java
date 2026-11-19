package com.zifang.z.meta.core.domain.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zifang.z.meta.api.annotation.BizMethod;
import com.zifang.z.meta.core.domain.entity.ZTenantDO;
import com.zifang.z.meta.core.domain.mapper.ZTenantMapper;
import com.zifang.z.meta.core.domain.service.ITenantService;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * 租户 Service 实现
 */
@Service
@Primary
public class TenantServiceImpl extends ServiceImpl<ZTenantMapper, ZTenantDO> implements ITenantService {

    @Override
    @BizMethod(name = "pageTenant", module = "z-meta", description = "", operation = BizMethod.Operation.QUERY)
    public IPage<ZTenantDO> pageTenant(Page<ZTenantDO> page, ZTenantDO tenant) {
        LambdaQueryWrapper<ZTenantDO> wrapper = new LambdaQueryWrapper<>();
        if (tenant != null) {
            if (StringUtils.hasText(tenant.getTenantCode())) {
                wrapper.like(ZTenantDO::getTenantCode, tenant.getTenantCode());
            }
            if (StringUtils.hasText(tenant.getTenantName())) {
                wrapper.like(ZTenantDO::getTenantName, tenant.getTenantName());
            }
            if (StringUtils.hasText(tenant.getTenantType())) {
                wrapper.eq(ZTenantDO::getTenantType, tenant.getTenantType());
            }
            if (tenant.getStatus() != null) {
                wrapper.eq(ZTenantDO::getStatus, tenant.getStatus());
            }
        }
        wrapper.orderByDesc(ZTenantDO::getGmtCreate);
        return page(page, wrapper);
    }

    @Override
    @BizMethod(name = "getByTenantCode", module = "z-meta", description = "", operation = BizMethod.Operation.QUERY)
    public ZTenantDO getByTenantCode(String tenantCode) {
        return getOne(new LambdaQueryWrapper<ZTenantDO>().eq(ZTenantDO::getTenantCode, tenantCode));
    }
}

