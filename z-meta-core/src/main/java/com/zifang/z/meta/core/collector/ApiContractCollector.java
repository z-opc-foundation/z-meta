package com.zifang.z.meta.core.collector;

import com.zifang.z.meta.api.annotation.BizApi;
import com.zifang.z.meta.api.annotation.BizCause;
import com.zifang.z.meta.api.annotation.BizMethod;
import com.zifang.z.meta.api.annotation.BizParam;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.PostConstruct;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 接口契约采集器：读 Spring 的真实路由，叠上业务注解，产出端点契约。
 *
 * <p>与 {@link BizModelCollector} 的分工：那边管「数据是什么」，这边管「对外怎么调」。
 * 路径与 HTTP 方法一律以 Spring 映射注解为准（{@link BizApi} 可显式覆盖），
 * 业务语义（摘要、操作类型、参数含义、废弃标记）一律以 {@code @Biz*} 注解为准。</p>
 *
 * @author zifang
 * @since 1.1.0
 */
@Component
public class ApiContractCollector {

    private final Map<String, BizApiDTO> endpoints = new ConcurrentHashMap<>();

    /** 端点级声明的因果边。 */
    private final List<BizCauseDTO> causes = new ArrayList<>();

    @PostConstruct
    public void collect() {
        ClassPathScanningCandidateComponentProvider scanner =
                new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new AnnotationTypeFilter(RestController.class));
        scanner.addIncludeFilter(new AnnotationTypeFilter(Controller.class));

        for (BeanDefinition bd : scanner.findCandidateComponents("com.zifang")) {
            Class<?> clazz;
            try {
                clazz = Class.forName(bd.getBeanClassName());
            } catch (ClassNotFoundException e) {
                continue;
            }
            collectClass(clazz);
        }
    }

    private void collectClass(Class<?> clazz) {
        String basePath = "";
        RequestMapping classMapping = clazz.getAnnotation(RequestMapping.class);
        if (classMapping != null && classMapping.value().length > 0) {
            basePath = classMapping.value()[0];
        }

        for (Method method : clazz.getDeclaredMethods()) {
            String[] paths = null;
            String httpMethod = null;

            GetMapping get = method.getAnnotation(GetMapping.class);
            PostMapping post = method.getAnnotation(PostMapping.class);
            PutMapping put = method.getAnnotation(PutMapping.class);
            DeleteMapping delete = method.getAnnotation(DeleteMapping.class);
            PatchMapping patch = method.getAnnotation(PatchMapping.class);

            if (get != null) {
                paths = get.value();
                httpMethod = "GET";
            } else if (post != null) {
                paths = post.value();
                httpMethod = "POST";
            } else if (put != null) {
                paths = put.value();
                httpMethod = "PUT";
            } else if (delete != null) {
                paths = delete.value();
                httpMethod = "DELETE";
            } else if (patch != null) {
                paths = patch.value();
                httpMethod = "PATCH";
            } else {
                RequestMapping generic = method.getAnnotation(RequestMapping.class);
                if (generic == null) continue;
                paths = generic.value();
                httpMethod = generic.method().length > 0 ? generic.method()[0].name() : "";
            }
            if (paths == null || paths.length == 0) paths = new String[] {""};

            BizApi apiAnn = method.getAnnotation(BizApi.class);
            BizMethod methodAnn = method.getAnnotation(BizMethod.class);
            BizCause causeAnn = method.getAnnotation(BizCause.class);

            for (String path : paths) {
                String fullPath = normalize(basePath + path);
                BizApiDTO dto = new BizApiDTO();
                dto.setPath(fullPath);
                dto.setHttpMethod(apiAnn != null && !apiAnn.httpMethod().isEmpty()
                        ? apiAnn.httpMethod() : httpMethod);
                dto.setDeclaringClass(clazz.getName());
                dto.setMethodName(method.getName());
                dto.setProduces(apiAnn != null ? apiAnn.produces() : "application/json");
                dto.setDeprecated(apiAnn != null && apiAnn.deprecated());
                dto.setSummary(apiAnn != null ? apiAnn.summary()
                        : (methodAnn != null ? methodAnn.name() : method.getName()));
                dto.setDescription(apiAnn != null ? apiAnn.description()
                        : (methodAnn != null ? methodAnn.description() : ""));
                dto.setTags(apiAnn != null ? new ArrayList<>(Arrays.asList(apiAnn.tags()))
                        : new ArrayList<>());

                List<BizParamDTO> params = collectParams(method, fullPath);
                dto.setParams(params);

                endpoints.put(dto.getHttpMethod() + " " + fullPath, dto);
            }

            if (causeAnn != null) {
                BizCauseDTO cd = new BizCauseDTO();
                cd.setSubject(causeAnn.subject().isEmpty() ? clazz.getSimpleName() : causeAnn.subject());
                cd.setEffect(causeAnn.effect());
                cd.setBecause(causeAnn.because());
                cd.setCondition(causeAnn.condition());
                cd.setWeight(causeAnn.weight());
                cd.setSource(clazz.getSimpleName() + "#" + method.getName());
                causes.add(cd);
            }
        }
    }

    /** 采集方法入参的业务语义。 */
    private List<BizParamDTO> collectParams(Method method, String fullPath) {
        List<BizParamDTO> params = new ArrayList<>();
        for (Parameter parameter : method.getParameters()) {
            BizParam paramAnn = parameter.getAnnotation(BizParam.class);
            if (paramAnn == null) continue;
            BizParamDTO pd = new BizParamDTO();
            pd.setName(paramAnn.name());
            pd.setFieldName(parameter.getName());
            pd.setType(paramAnn.type().name());
            pd.setRequired(paramAnn.required());
            pd.setDescription(paramAnn.description());
            pd.setRefModel(paramAnn.refModel());
            pd.setRefModule(paramAnn.refModule());
            pd.setDictCode(paramAnn.dictCode());
            pd.setPosition(inferPosition(paramAnn, parameter, fullPath));
            params.add(pd);
        }
        return params;
    }

    /** 注解没写位置时，按路径模板与参数名推断。 */
    private String inferPosition(BizParam paramAnn, Parameter parameter, String fullPath) {
        if (!paramAnn.position().name().equals("QUERY")) return paramAnn.position().name();
        String name = parameter.getName();
        if (name != null && fullPath.contains("{" + name + "}")) return BizParam.Position.PATH.name();
        return BizParam.Position.QUERY.name();
    }

    private String normalize(String path) {
        if (path == null || path.isEmpty()) return "/";
        String p = path.startsWith("/") ? path : "/" + path;
        while (p.length() > 1 && p.endsWith("/")) p = p.substring(0, p.length() - 1);
        return p;
    }

    public List<BizApiDTO> getAllEndpoints() {
        return new ArrayList<>(endpoints.values());
    }

    public BizApiDTO getEndpoint(String httpMethod, String path) {
        return endpoints.get(httpMethod + " " + path);
    }

    public List<BizCauseDTO> getAllCauses() {
        return new ArrayList<>(causes);
    }
}
