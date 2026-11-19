package com.zifang.z.meta.starter.config;

import com.zifang.z.meta.starter.repo.RepoInfoController;
import com.zifang.z.meta.starter.repo.RepoInfoService;
import com.zifang.z.meta.starter.repo.RepoSelfCollector;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * 仓库自述 starter 自动装配 —— 引入本件即自带 {@code /api/meta/repo/**} 一组端点。
 *
 * <p>「本仓包前缀」怎么定：starter 所在包是 {@code com.zifang.z.meta.starter.*}，它**不属于**任何业务仓；
 * 真正要扫的是宿主的业务包。三条线索按优先级取：
 * <ol>
 *   <li>{@code z.repo.package} 显式配置（最准，特殊仓用它兜底）</li>
 *   <li>{@code spring.application.name} 派生的包（形如 {@code z-lc} ⇒ {@code com.zifang.z.lc}）</li>
 *   <li>退到 {@code com.zifang}（扫全组织，代价是慢一点，但不会漏）</li>
 * </ol>
 *
 * <p>为什么不用 classpath 扫 {@code com.zifang} 找端点：端点要准 ⇒ 走 Spring 注册的路由；
 * 模型没有 Spring 元数据 ⇒ 才退到注解扫描，且限定在本仓包前缀内。</p>
 *
 * @author zifang
 * @since 1.0.3
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@Import(RepoInfoController.class)
public class ZMetaRepoAutoConfiguration {

    @Value("${z.repo.package:}")
    private String configuredPackage;

    @Value("${spring.application.name:}")
    private String applicationName;

    /**
     * 采集器。
     *
     * @param context Spring 上下文
     * @return 采集器
     */
    @Bean
    @ConditionalOnMissingBean
    public RepoSelfCollector repoSelfCollector(ApplicationContext context) {
        return new RepoSelfCollector(resolvePackagePrefix(context));
    }

    /**
     * 身份服务。
     *
     * @param context   Spring 上下文
     * @param collector 采集器
     * @return 身份服务
     */
    @Bean
    @ConditionalOnMissingBean
    public RepoInfoService repoInfoService(ApplicationContext context, RepoSelfCollector collector) {
        return new RepoInfoService(context, collector);
    }

    /**
     * 推本仓包前缀。
     *
     * @param context Spring 上下文
     * @return 包前缀
     */
    String resolvePackagePrefix(ApplicationContext context) {
        if (configuredPackage != null && !configuredPackage.isEmpty()) return configuredPackage;
        String fromApp = packageFromApplicationName(applicationName);
        if (fromApp != null) return fromApp;
        return "com.zifang";
    }

    /**
     * 由应用名推包前缀：{@code z-lc} ⇒ {@code com.zifang.z.lc}；{@code zteam} ⇒ {@code com.zifang.zteam}。
     *
     * @param applicationName 应用名
     * @return 包前缀，推不出返回 null
     */
    String packageFromApplicationName(String applicationName) {
        if (applicationName == null || applicationName.isEmpty()) return null;
        String name = applicationName;
        if (name.startsWith("z-")) name = name.substring(2);
        else if (name.startsWith("z") && name.length() > 1 && !Character.isDigit(name.charAt(1))) {
            name = name.substring(1);
        }
        if (name.isEmpty()) return null;
        return "com.zifang.z." + name;
    }
}
