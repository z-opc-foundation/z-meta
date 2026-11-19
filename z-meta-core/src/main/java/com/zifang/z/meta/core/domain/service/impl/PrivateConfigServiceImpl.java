package com.zifang.z.meta.core.domain.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zifang.z.meta.api.annotation.BizMethod;
import com.zifang.z.meta.core.domain.entity.ZPrivateConfigDO;
import com.zifang.z.meta.core.domain.mapper.ZPrivateConfigMapper;
import com.zifang.z.meta.core.domain.service.IPrivateConfigService;
import org.springframework.stereotype.Service;

/**
 * 私有配置 Service 实现
 */
@Service
public class PrivateConfigServiceImpl extends ServiceImpl<ZPrivateConfigMapper, ZPrivateConfigDO> implements IPrivateConfigService {

    @Override
    @BizMethod(name = "getByKey", module = "z-meta", description = "", operation = BizMethod.Operation.QUERY)
    public ZPrivateConfigDO getByKey(String configKey) {
        LambdaQueryWrapper<ZPrivateConfigDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ZPrivateConfigDO::getConfigKey, configKey);
        return getOne(wrapper);
    }
}



