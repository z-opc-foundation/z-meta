package com.zifang.z.meta.core.resource;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zifang.util.json.JsonUtil;
import com.zifang.z.meta.api.resource.ResourceProvider;
import com.zifang.z.meta.core.domain.entity.ZResourceDO;
import com.zifang.z.meta.core.domain.mapper.ZResourceMapper;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 资源采集器：把各模块上报的资源实例落进 {@code z_resource}。
 *
 * <p>单个模块上报失败不得拖垮整体采集 —— 上报通道不健康时直接跳过该模块。</p>
 *
 * @author zifang
 * @since 1.1.0
 */
@Component
public class ResourceCollector {

    private final ResourceProviderRegistry registry;
    private final ZResourceMapper resourceMapper;

    public ResourceCollector(ResourceProviderRegistry registry, ZResourceMapper resourceMapper) {
        this.registry = registry;
        this.resourceMapper = resourceMapper;
    }

    /**
     * 采集所有模块上报的资源。
     */
    @Transactional
    public void collectAll() {
        for (ResourceProvider provider : registry.getAllProviders()) {
            collectFromProvider(provider);
        }
    }

    /**
     * 采集单个模块上报的资源。
     *
     * @param provider 上报方
     */
    @Transactional
    public void collectFromProvider(ResourceProvider provider) {
        if (!provider.isHealthy()) return;

        List<ResourceProvider.ResourceDTO> resources = provider.listResources();
        if (resources == null) return;

        for (ResourceProvider.ResourceDTO dto : resources) {
            ZResourceDO entity = resourceMapper.selectOne(
                    new LambdaQueryWrapper<ZResourceDO>()
                            .eq(ZResourceDO::getResourceCode, dto.getResourceCode()));
            LocalDateTime now = LocalDateTime.now();
            Date gmt = new Date();
            if (entity == null) {
                entity = new ZResourceDO();
                entity.setResourceCode(dto.getResourceCode());
                entity.setResourceName(dto.getResourceName());
                entity.setResourceType(dto.getResourceType());
                entity.setModule(dto.getModule());
                entity.setStatus(dto.getStatus());
                entity.setEndpoint(dto.getEndpoint());
                entity.setSpec(dto.getSpec());
                entity.setAttributes(toAttributesJson(dto.getAttributes()));
                entity.setTenantCode(dto.getTenantCode());
                entity.setDomainCode(dto.getDomainCode());
                entity.setCollectedAt(now);
                entity.setGmtCreate(gmt);
                entity.setGmtModified(gmt);
                resourceMapper.insert(entity);
            } else {
                entity.setResourceName(dto.getResourceName());
                entity.setStatus(dto.getStatus());
                entity.setEndpoint(dto.getEndpoint());
                entity.setSpec(dto.getSpec());
                entity.setAttributes(toAttributesJson(dto.getAttributes()));
                entity.setCollectedAt(now);
                entity.setGmtModified(gmt);
                resourceMapper.updateById(entity);
            }
        }
    }

    /**
     * 把上报方给的 attributes Map 序列化成 JSON。
     *
     * <p>{@code z_resource.attributes} 存的是 JSON，不是 Java 的 {@code {k=v}} 伪格式 ——
     * 值里只要含 {@code ,} 或 {@code =}（连接串、query、表达式都是常见值），
     * 伪格式就没有任何无歧义的解析方式，下游按 JSON 读必然失败。</p>
     */
    private static String toAttributesJson(Map<String, String> attributes) {
        return attributes == null ? null : JsonUtil.toJson(attributes);
    }
}
