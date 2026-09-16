package com.zifang.z.meta.core.domain.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zifang.z.meta.core.domain.entity.ZDictItemDO;
import com.zifang.z.meta.core.domain.mapper.ZDictItemMapper;
import com.zifang.z.meta.core.domain.service.IDictItemService;
import org.springframework.stereotype.Service;

/**
 * 字典项 Service 实现
 */
@Service
public class DictItemServiceImpl extends ServiceImpl<ZDictItemMapper, ZDictItemDO> implements IDictItemService {
}