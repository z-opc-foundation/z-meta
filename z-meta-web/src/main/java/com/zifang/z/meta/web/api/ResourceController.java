package com.zifang.z.meta.web.api;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zifang.util.core.meta.Result;
import com.zifang.z.meta.api.annotation.BizApi;
import com.zifang.z.meta.api.annotation.BizField;
import com.zifang.z.meta.api.annotation.BizParam;
import com.zifang.z.meta.api.resource.ResourceProvider;
import com.zifang.z.meta.core.domain.entity.ZResourceDO;
import com.zifang.z.meta.core.domain.mapper.ZResourceMapper;
import com.zifang.z.meta.core.resource.ResourceCollector;
import com.zifang.z.meta.core.resource.ResourceProviderRegistry;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 技术资源台账控制器：登记「这个系统在跑在什么上面」。
 *
 * <p>与 z-asset 的分工：本控制器管技术资源实例（数据库 / 缓存 / 队列 / 存储桶）；
 * 企业资产（实物设备、域名证书、软件许可）归 z-asset。</p>
 *
 * @author zifang
 * @since 1.1.0
 */
@RestController
@RequestMapping("/api/meta/resource")
public class ResourceController {

    @Resource
    private ResourceProviderRegistry registry;

    @Resource
    private ResourceCollector collector;

    @Resource
    private ZResourceMapper resourceMapper;

    /**
     * 触发一次全量采集。
     *
     * @return 采集结果
     */
    @PostMapping("/collect")
    @BizApi(summary = "触发一次全量采集", description = "触发一次全量采集。", tags = { "z-meta" })
    public Result<String> collect() {
        collector.collectAll();
        return Result.success("ok");
    }

    /**
     * 查询资源台账。
     *
     * @param module       模块名（可选）
     * @param resourceType 资源类型（可选）
     * @return 资源列表
     */
    @GetMapping("/list")
    @BizApi(summary = "查询资源台账", description = "查询资源台账。", tags = { "z-meta" })
    public Result<List<ZResourceDO>> list(@RequestParam(required = false) @BizParam(name = "module", type = BizField.FieldType.STRING, description = "模块名（可选）", position = BizParam.Position.QUERY) String module,
                                           @RequestParam(required = false)
                                           @BizParam(name = "resourceType", type = BizField.FieldType.STRING, description = "资源类型（可选）", position = BizParam.Position.QUERY) String resourceType) {
        LambdaQueryWrapper<ZResourceDO> qw = new LambdaQueryWrapper<>();
        if (module != null) qw.eq(ZResourceDO::getModule, module);
        if (resourceType != null) qw.eq(ZResourceDO::getResourceType, resourceType);
        qw.orderByDesc(ZResourceDO::getGmtModified);
        return Result.success(resourceMapper.selectList(qw));
    }

    /**
     * 已登记的上报方花名册。
     *
     * @return 上报方列表
     */
    @GetMapping("/providers")
    @BizApi(summary = "已登记的上报方花名册", description = "已登记的上报方花名册。", tags = { "z-meta" })
    public Result<List<ResourceProvider>> providers() {
        return Result.success(registry.getAllProviders());
    }
}



