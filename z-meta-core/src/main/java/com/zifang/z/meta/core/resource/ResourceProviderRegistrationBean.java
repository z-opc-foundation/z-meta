package com.zifang.z.meta.core.resource;

import com.zifang.z.meta.api.resource.ResourceProvider;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

/**
 * 上报方自动登记：任何 {@link ResourceProvider} bean 初始化完就进注册表，业务侧零配置接入。
 *
 * @author zifang
 * @since 1.1.0
 */
@Component
public class ResourceProviderRegistrationBean implements BeanPostProcessor {

    private final ResourceProviderRegistry registry;

    public ResourceProviderRegistrationBean(ResourceProviderRegistry registry) {
        this.registry = registry;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof ResourceProvider) {
            registry.register((ResourceProvider) bean);
        }
        return bean;
    }
}
