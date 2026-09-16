package com.zifang.z.meta.core.domain.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.zifang.z.meta.core.domain.entity.ZDictItemDO;
import com.zifang.z.meta.core.domain.entity.ZDictTypeDO;

import java.util.List;

/**
 * 字典 Service
 */
public interface IDictService extends IService<ZDictTypeDO> {

    /**
     * 分页查询字典类型
     */
    IPage<ZDictTypeDO> pageDictType(Page<ZDictTypeDO> page, ZDictTypeDO dictType);

    /**
     * 根据租户和字典编码查询
     */
    ZDictTypeDO getByDictCode(Long tenantId, String dictCode);

    /**
     * 根据字典ID查询字典项
     */
    List<ZDictItemDO> listByDictId(Long dictId);

    /**
     * 根据字典编码查询字典项
     */
    List<ZDictItemDO> listByDictCode(String dictCode);
}