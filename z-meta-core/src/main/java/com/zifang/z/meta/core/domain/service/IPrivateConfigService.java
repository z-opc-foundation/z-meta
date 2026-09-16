package com.zifang.z.meta.core.domain.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zifang.z.meta.core.domain.entity.ZPrivateConfigDO;

/**
 * 私有配置 Service
 */
public interface IPrivateConfigService extends IService<ZPrivateConfigDO> {

    /**
     * 根据配置键查询
     */
    ZPrivateConfigDO getByKey(String configKey);
}
