package com.zifang.z.meta.admin.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * z-meta 模块自动配置入口
 */
@Configuration
@ComponentScan(basePackages = "com.zifang.z.meta")
public class ZMetaAutoConfiguration {
}
