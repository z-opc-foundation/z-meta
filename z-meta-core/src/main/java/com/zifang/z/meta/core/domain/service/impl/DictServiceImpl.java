package com.zifang.z.meta.core.domain.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zifang.z.meta.core.domain.entity.ZDictItemDO;
import com.zifang.z.meta.core.domain.entity.ZDictTypeDO;
import com.zifang.z.meta.core.domain.mapper.ZDictItemMapper;
import com.zifang.z.meta.core.domain.mapper.ZDictTypeMapper;
import com.zifang.z.meta.core.domain.service.IDictService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 字典 Service 实现
 */
@Service
public class DictServiceImpl extends ServiceImpl<ZDictTypeMapper, ZDictTypeDO> implements IDictService {

    @Autowired
    private ZDictItemMapper dictItemMapper;

    @Override
    public IPage<ZDictTypeDO> pageDictType(Page<ZDictTypeDO> page, ZDictTypeDO dictType) {
        LambdaQueryWrapper<ZDictTypeDO> wrapper = new LambdaQueryWrapper<>();
        if (dictType != null) {
            if (dictType.getTenantId() != null) {
                wrapper.eq(ZDictTypeDO::getTenantId, dictType.getTenantId());
            }
            if (StringUtils.hasText(dictType.getDictCode())) {
                wrapper.like(ZDictTypeDO::getDictCode, dictType.getDictCode());
            }
            if (StringUtils.hasText(dictType.getDictName())) {
                wrapper.like(ZDictTypeDO::getDictName, dictType.getDictName());
            }
            if (dictType.getStatus() != null) {
                wrapper.eq(ZDictTypeDO::getStatus, dictType.getStatus());
            }
        }
        wrapper.orderByDesc(ZDictTypeDO::getGmtCreate);
        return page(page, wrapper);
    }

    @Override
    public ZDictTypeDO getByDictCode(Long tenantId, String dictCode) {
        return getOne(new LambdaQueryWrapper<ZDictTypeDO>()
                .eq(ZDictTypeDO::getTenantId, tenantId)
                .eq(ZDictTypeDO::getDictCode, dictCode));
    }

    @Override
    public List<ZDictItemDO> listByDictId(Long dictId) {
        return dictItemMapper.selectList(new LambdaQueryWrapper<ZDictItemDO>()
                .eq(ZDictItemDO::getDictId, dictId)
                .orderByAsc(ZDictItemDO::getSortOrder));
    }

    @Override
    public List<ZDictItemDO> listByDictCode(String dictCode) {
        // 先查询字典类型
        ZDictTypeDO dictType = getByDictCode(1L, dictCode); // TODO: 从上下文获取租户ID
        if (dictType == null) {
            return null;
        }
        return listByDictId(dictType.getId());
    }
}