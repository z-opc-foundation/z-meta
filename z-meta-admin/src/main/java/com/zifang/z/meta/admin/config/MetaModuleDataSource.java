package com.zifang.z.meta.admin.config;

import com.baomidou.mybatisplus.extension.spring.MybatisSqlSessionFactoryBean;
import com.zifang.z.boot.datasource.starter.ModuleDataSourceTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
@MapperScan(basePackages = "com.zifang.z.meta.core.domain.mapper", sqlSessionFactoryRef = "sqlSessionFactoryMeta")
public class MetaModuleDataSource extends ModuleDataSourceTemplate {

    @Bean("dataSourceMeta")
    public DataSource dataSource(org.springframework.core.env.Environment env) {
        return buildDataSource(env, "meta");
    }

    @Bean("sqlSessionFactoryMeta")
    public MybatisSqlSessionFactoryBean sqlSessionFactory(DataSource dataSourceMeta) throws Exception {
        return buildSqlSessionFactory(dataSourceMeta);
    }
}
