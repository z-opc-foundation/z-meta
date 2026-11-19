package com.zifang.z.meta.core.domain.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zifang.z.meta.api.annotation.BizMethod;
import com.zifang.z.meta.core.domain.entity.ZApiDO;
import com.zifang.z.meta.core.domain.mapper.ZApiMapper;
import com.zifang.z.meta.core.domain.service.IApiService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 接口 Service 实现
 */
@Service
public class ApiServiceImpl extends ServiceImpl<ZApiMapper, ZApiDO> implements IApiService {

    @Override
    @BizMethod(name = "pageApi", module = "z-meta", description = "", operation = BizMethod.Operation.QUERY)
    public IPage<ZApiDO> pageApi(Page<ZApiDO> page, ZApiDO api) {
        LambdaQueryWrapper<ZApiDO> wrapper = new LambdaQueryWrapper<>();
        if (api != null) {
            if (api.getAppId() != null) {
                wrapper.eq(ZApiDO::getAppId, api.getAppId());
            }
            if (StringUtils.hasText(api.getApiPath())) {
                wrapper.like(ZApiDO::getApiPath, api.getApiPath());
            }
            if (StringUtils.hasText(api.getApiName())) {
                wrapper.like(ZApiDO::getApiName, api.getApiName());
            }
            if (StringUtils.hasText(api.getApiMethod())) {
                wrapper.eq(ZApiDO::getApiMethod, api.getApiMethod());
            }
            if (api.getStatus() != null) {
                wrapper.eq(ZApiDO::getStatus, api.getStatus());
            }
            if (api.getDeprecated() != null) {
                wrapper.eq(ZApiDO::getDeprecated, api.getDeprecated());
            }
        }
        wrapper.orderByDesc(ZApiDO::getGmtCreate);
        return page(page, wrapper);
    }

    @Override
    @BizMethod(name = "listByAppId", module = "z-meta", description = "", operation = BizMethod.Operation.QUERY)
    public List<ZApiDO> listByAppId(Long appId) {
        return list(new LambdaQueryWrapper<ZApiDO>()
                .eq(ZApiDO::getAppId, appId)
                .orderByDesc(ZApiDO::getGmtCreate));
    }
}

