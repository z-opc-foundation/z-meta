package com.zifang.z.meta.starter.repo;

import com.zifang.z.meta.api.annotation.BizModel;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 采集器 —— 把本仓的端点与数据模型读成 {@link RepoSelfDescriptor}。
 *
 * <p>端点来源是 Spring 的 {@code RequestMappingHandlerMapping}：那是**真正注册上去**的路由，
 * 类级 {@code @RequestMapping} 前缀、path pattern、HTTP 方法都是拼好的，比 classpath 猜注解准。</p>
 *
 * <p>数据模型来源是 classpath 扫描 {@code @BizModel}：模型是数据类不是端点，没有 Spring 元数据可查，
 * 只能按注解找 —— 扫描范围限定在本仓包前缀（由 starter 所在包推），避免把别人的模型算成自己的。</p>
 *
 * @author zifang
 * @since 1.0.3
 */
public class RepoSelfCollector {

    /** 本仓包前缀（由 starter 所在包名推导，形如 {@code com.zifang.z.lc}）。 */
    private final String repoPackagePrefix;

    /** 本仓扫不到时的退扫前缀。 */
    private static final String FALLBACK_PREFIX = "com.zifang";

    /** 扫描结果。 */
    private final RepoSelfDescriptor descriptor = new RepoSelfDescriptor();

    /**
     * @param repoPackagePrefix 本仓包前缀
     */
    public RepoSelfCollector(String repoPackagePrefix) {
        this.repoPackagePrefix = repoPackagePrefix;
    }

    /**
     * 采集本仓端点与模型。
     *
     * @param context Spring 上下文
     * @return 自述结果
     */
    public RepoSelfDescriptor collect(ApplicationContext context) {
        collectEndpoints(context);
        collectModels();
        collectMethods();
        descriptor.getEndpoints().sort(Comparator
                .comparing(RepoSelfDescriptor.EndpointInfo::getPath)
                .thenComparing(RepoSelfDescriptor.EndpointInfo::getHttpMethod));
        return descriptor;
    }

    /** 从 Spring 注册的路由里读端点。 */
    private void collectEndpoints(ApplicationContext context) {
        for (Map.Entry<RequestMappingInfo, HandlerMethod> entry : handlerMethods(context).entrySet()) {
            RequestMappingInfo info = entry.getKey();
            HandlerMethod handler = entry.getValue();
            Method method = handler.getMethod();
            Class<?> declaringClass = handler.getBeanType();
            for (String path : paths(info)) {
                for (String httpMethod : methods(info)) {
                    descriptor.getEndpoints().add(descriptor.describeEndpoint(method, httpMethod, path, declaringClass));
                }
            }
        }
    }

    /** 取本进程所有 HandlerMethod（兼容普通 MVC 与 actuator 两种映射器）。 */
    @SuppressWarnings("unchecked")
    private Map<RequestMappingInfo, HandlerMethod> handlerMethods(ApplicationContext context) {
        Map<RequestMappingInfo, HandlerMethod> result = new LinkedHashMap<>();
        for (String beanName : context.getBeanNamesForType(RequestMappingHandlerMapping.class)) {
            Object bean = context.getBean(beanName);
            if (bean instanceof RequestMappingHandlerMapping mapping) {
                result.putAll(mapping.getHandlerMethods());
            }
        }
        return result;
    }

    /** 路由路径（Spring 5.3+ 走 PathPattern，旧的走 Patterns）。 */
    private List<String> paths(RequestMappingInfo info) {
        if (info.getPathPatternsCondition() != null) {
            return new ArrayList<>(info.getPathPatternsCondition().getPatternValues());
        }
        return info.getPatternsCondition() == null
                ? List.of()
                : new ArrayList<>(info.getPatternsCondition().getPatterns());
    }

    /** 路由的 HTTP 方法（未限定时记为 ALL）。 */
    private List<String> methods(RequestMappingInfo info) {
        List<String> methods = new ArrayList<>();
        info.getMethodsCondition().getMethods().forEach(m -> methods.add(m.name()));
        return methods.isEmpty() ? List.of("ALL") : methods;
    }

    /** 扫本仓 {@code @BizModel} 数据模型；扫不到时退扫全组织（all-in-one 宿主里业务包前缀推不准）。 */
    private void collectModels() {
        scanModels(repoPackagePrefix);
        if (descriptor.getModels().isEmpty()) scanModels(FALLBACK_PREFIX);
    }

    private void scanModels(String prefix) {
        ClassPathScanningCandidateComponentProvider scanner =
                new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new AnnotationTypeFilter(BizModel.class));
        for (BeanDefinition bd : scanner.findCandidateComponents(prefix)) {
            try {
                Class<?> clazz = Class.forName(bd.getBeanClassName());
                RepoSelfDescriptor.ModelInfo info = descriptor.describeModel(clazz);
                if (info != null) descriptor.getModels().add(info);
            } catch (ClassNotFoundException | LinkageError ignored) {
                // 扫到加载不了的类（缺依赖/版本冲突）不该拖垮整个自述
            }
        }
    }

    /** 扫本仓 {@code @BizMethod} 业务方法；扫不到时退扫全组织。 */
    private void collectMethods() {
        scanMethods(repoPackagePrefix);
        if (descriptor.getMethods().isEmpty()) scanMethods(FALLBACK_PREFIX);
    }

    private void scanMethods(String prefix) {
        ClassPathScanningCandidateComponentProvider scanner =
                new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new AnnotationTypeFilter(org.springframework.stereotype.Component.class));
        for (BeanDefinition bd : scanner.findCandidateComponents(prefix)) {
            try {
                Class<?> clazz = Class.forName(bd.getBeanClassName());
                for (Method method : clazz.getDeclaredMethods()) {
                    if (AnnotatedElementUtils.hasAnnotation(method, com.zifang.z.meta.api.annotation.BizMethod.class)) {
                        RepoSelfDescriptor.MethodInfo info = descriptor.describeMethod(method, clazz);
                        if (info != null) {
                            descriptor.getMethods().put(clazz.getSimpleName() + "#" + method.getName(), info);
                        }
                    }
                }
            } catch (ClassNotFoundException | LinkageError ignored) {
                // 同上：单个类加载不了不影响其余自述
            }
        }
    }
}
