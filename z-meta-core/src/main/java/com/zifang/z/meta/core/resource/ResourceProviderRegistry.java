package com.zifang.z.meta.core.resource;

import com.zifang.z.meta.api.resource.ResourceProvider;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 资源上报方注册表：容器里所有 {@link ResourceProvider} 的花名册。
 *
 * @author zifang
 * @since 1.1.0
 */
@Component
public class ResourceProviderRegistry {

    private final List<ResourceProvider> providers = new CopyOnWriteArrayList<>();

    public void register(ResourceProvider provider) {
        providers.add(provider);
    }

    public List<ResourceProvider> getAllProviders() {
        return providers;
    }

    public List<ResourceProvider> getProvidersByModule(String module) {
        List<ResourceProvider> result = new ArrayList<>();
        for (ResourceProvider p : providers) {
            if (p.getModule().equals(module)) result.add(p);
        }
        return result;
    }

    public ResourceProvider getProvider(String module, String resourceType) {
        for (ResourceProvider p : providers) {
            if (p.getModule().equals(module) && p.getResourceType().equals(resourceType)) return p;
        }
        return null;
    }
}
