package com.zifang.z.meta.core;

import com.zifang.z.meta.api.resource.ResourceProvider;
import com.zifang.z.meta.core.domain.entity.ZResourceDO;
import com.zifang.z.meta.core.domain.mapper.ZResourceMapper;
import com.zifang.z.meta.core.resource.ResourceCollector;
import com.zifang.z.meta.core.resource.ResourceProviderRegistrationBean;
import com.zifang.z.meta.core.resource.ResourceProviderRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 技术资源上报链路的自守卫：登记 → 采集 → 落库。
 */
class ResourceReportingTest {

    private ResourceProviderRegistry registry;
    private ZResourceMapper mapper;
    private ResourceCollector collector;

    @BeforeEach
    void setUp() {
        registry = new ResourceProviderRegistry();
        mapper = mock(ZResourceMapper.class);
        collector = new ResourceCollector(registry, mapper);
    }

    private static ResourceProvider provider(String module, String type, boolean healthy,
                                             List<ResourceProvider.ResourceDTO> resources) {
        return new ResourceProvider() {
            @Override
            public String getModule() {
                return module;
            }

            @Override
            public String getResourceType() {
                return type;
            }

            @Override
            public List<ResourceProvider.ResourceDTO> listResources() {
                return resources;
            }

            @Override
            public ResourceProvider.ResourceDTO getResourceDetail(String resourceCode) {
                return null;
            }

            @Override
            public boolean isHealthy() {
                return healthy;
            }
        };
    }

    @Test
    @DisplayName("上报方 bean 初始化后自动进注册表")
    void providerBeanIsRegistered() {
        ResourceProviderRegistrationBean bean = new ResourceProviderRegistrationBean(registry);
        ResourceProvider p = provider("trade", "database", true, List.of());
        bean.postProcessAfterInitialization(p, "tradeDbProvider");
        assertEquals(1, registry.getAllProviders().size());
        assertNotNull(registry.getProvider("trade", "database"));
        assertEquals(1, registry.getProvidersByModule("trade").size());
    }

    @Test
    @DisplayName("不健康的模块被跳过，不拖垮整体采集")
    void unhealthyProviderIsSkipped() {
        registry.register(provider("broken", "cache", false, List.of()));
        collector.collectAll();
        verify(mapper, never()).insert(any(ZResourceDO.class));
    }

    @Test
    @DisplayName("attributes 落库是真 JSON，不是 Java 的 {k=v} 伪格式")
    void attributesAreStoredAsJson() {
        ResourceProvider.ResourceDTO dto = new ResourceProvider.ResourceDTO(
                "db-1", "主库", "database", "trade", "UP");
        dto.setEndpoint("jdbc:mysql://h:3306/db?a=1");
        dto.setAttributes(Map.of("pool", "8,16", "user", "root=pwd"));

        registry.register(provider("trade", "database", true, List.of(dto)));
        when(mapper.selectOne(any())).thenReturn(null);

        collector.collectAll();

        ArgumentCaptor<ZResourceDO> captor = ArgumentCaptor.forClass(ZResourceDO.class);
        verify(mapper).insert(captor.capture());
        String attributes = captor.getValue().getAttributes();
        assertNotNull(attributes);
        assertTrue(attributes.startsWith("{") && attributes.endsWith("}"), "应为 JSON 对象：" + attributes);
        assertTrue(attributes.contains("\"pool\""), "应是带引号的 JSON key：" + attributes);
        assertTrue(attributes.contains("8,16"), "含逗号的值必须原样保留：" + attributes);
    }
}
