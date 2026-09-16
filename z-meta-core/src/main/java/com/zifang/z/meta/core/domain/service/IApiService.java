package com.zifang.z.meta.core.domain.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.zifang.z.meta.core.domain.entity.ZApiDO;

import java.util.List;

/**
 * 接口 Service
 */
public interface IApiService extends IService<ZApiDO> {

    /**
     * 分页查询接口
     */
    IPage<ZApiDO> pageApi(Page<ZApiDO> page, ZApiDO api);

    /**
     * 根据应用ID查询接口列表
     */
    List<ZApiDO> listByAppId(Long appId);
}