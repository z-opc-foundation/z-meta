package com.zifang.z.meta.core.domain.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zifang.z.meta.core.domain.entity.ZApplicationDO;
import com.zifang.z.meta.core.domain.mapper.ZApplicationMapper;
import com.zifang.z.meta.core.domain.service.IApplicationService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * 应用 Service 实现
 */
@Service
public class ApplicationServiceImpl extends ServiceImpl<ZApplicationMapper, ZApplicationDO> implements IApplicationService {

    @Override
    public IPage<ZApplicationDO> pageApplication(Page<ZApplicationDO> page, ZApplicationDO application) {
        LambdaQueryWrapper<ZApplicationDO> wrapper = new LambdaQueryWrapper<>();
        if (application != null) {
            if (application.getTenantId() != null) {
                wrapper.eq(ZApplicationDO::getTenantId, application.getTenantId());
            }
            if (StringUtils.hasText(application.getAppCode())) {
                wrapper.like(ZApplicationDO::getAppCode, application.getAppCode());
            }
            if (StringUtils.hasText(application.getAppName())) {
                wrapper.like(ZApplicationDO::getAppName, application.getAppName());
            }
            if (StringUtils.hasText(application.getAppType())) {
                wrapper.eq(ZApplicationDO::getAppType, application.getAppType());
            }
            if (application.getStatus() != null) {
                wrapper.eq(ZApplicationDO::getStatus, application.getStatus());
            }
        }
        wrapper.orderByDesc(ZApplicationDO::getGmtCreate);
        return page(page, wrapper);
    }

    @Override
    public ZApplicationDO getByAppCode(Long tenantId, String appCode) {
        return getOne(new LambdaQueryWrapper<ZApplicationDO>()
                .eq(ZApplicationDO::getTenantId, tenantId)
                .eq(ZApplicationDO::getAppCode, appCode));
    }
}