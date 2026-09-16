package com.zifang.z.meta.web.config;

import com.alibaba.druid.pool.DruidDataSource;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * z-meta 独立启动时的默认 DataSource（仅在没有其他 dataSource bean 时生效）。
 * <p>
 * 集成到 main-starter 时由 DruidDataSourceAutoConfigure 提供 dataSource，本配置自动跳过，
 * 避免 BeanDefinitionOverrideException。
 */
@Configuration
@org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(name = "z.meta.standalone", havingValue = "true")
public class ZMetaDataSourceConfig {

    @Bean(name = "dataSource", initMethod = "init", destroyMethod = "close")
    @Primary
    @ConfigurationProperties(prefix = "spring.datasource")
    public DruidDataSource dataSource() {
        return DataSourceBuilder.create().type(DruidDataSource.class).build();
    }
}
