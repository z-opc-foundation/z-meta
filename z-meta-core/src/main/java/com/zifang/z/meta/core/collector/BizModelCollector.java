package com.zifang.z.meta.core.collector;

import com.zifang.z.meta.api.annotation.BizApi;
import com.zifang.z.meta.api.annotation.BizCause;
import com.zifang.z.meta.api.annotation.BizField;
import com.zifang.z.meta.api.annotation.BizMethod;
import com.zifang.z.meta.api.annotation.BizModel;
import com.zifang.z.meta.api.annotation.BizParam;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
public class BizModelCollector {

    private final Map<String, BizModelDTO> models = new ConcurrentHashMap<>();

    /** 方法元数据：编码 = 类名#方法名。 */
    private final Map<String, BizMethodDTO> methods = new ConcurrentHashMap<>();

    /** 因果边：主体 --causes--> 效果。 */
    private final List<BizCauseDTO> causes = new CopyOnWriteArrayList<>();

    @PostConstruct
    public void collect() {
        scanModels();
        scanAnnotatedMethods();
    }

    /** 已处理过的类，避免两轮扫描重复采集。 */
    private final java.util.Set<String> processedClasses = java.util.concurrent.ConcurrentHashMap.newKeySet();

    /** 第一轮：带 {@link BizModel} 的数据模型类。 */
    private void scanModels() {
        ClassPathScanningCandidateComponentProvider scanner =
                new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new AnnotationTypeFilter(BizModel.class));

        for (BeanDefinition bd : scanner.findCandidateComponents("com.zifang")) {
            try {
                Class<?> clazz = Class.forName(bd.getBeanClassName());
                BizModel modelAnn = clazz.getAnnotation(BizModel.class);
                if (modelAnn == null) continue;
                processedClasses.add(clazz.getName());

                BizModelDTO dto = new BizModelDTO();
                dto.setCode(clazz.getSimpleName());
                dto.setName(modelAnn.name());
                dto.setModule(modelAnn.module());
                dto.setDescription(modelAnn.description());
                dto.setClassName(clazz.getName());

                List<BizFieldDTO> fields = new ArrayList<>();
                for (Field field : clazz.getDeclaredFields()) {
                    BizField fieldAnn = field.getAnnotation(BizField.class);
                    if (fieldAnn == null) continue;
                    BizFieldDTO fd = new BizFieldDTO();
                    fd.setName(fieldAnn.name());
                    fd.setFieldName(field.getName());
                    fd.setType(fieldAnn.type().name());
                    fd.setRefModel(fieldAnn.refModel());
                    fd.setRefModule(fieldAnn.refModule());
                    fd.setDictCode(fieldAnn.dictCode());
                    fields.add(fd);
                }
                dto.setFields(fields);
                models.put(dto.getCode(), dto);

                collectClassCause(clazz);
                for (Method method : clazz.getDeclaredMethods()) {
                    BizMethodDTO methodDto = collectMethod(clazz, method, modelAnn.module());
                    if (methodDto != null) methods.put(methodDto.getCode(), methodDto);
                }
            } catch (ClassNotFoundException e) {
                // skip
            }
        }
    }

    /**
     * 第二轮：挂着方法级注解（{@link BizMethod} / {@link BizApi}）或类级
     * {@link BizCause} 的类 —— 它们不一定是数据模型，业务服务类同样要采。
     */
    private void scanAnnotatedMethods() {
        ClassPathScanningCandidateComponentProvider scanner =
                new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new MethodAnnotationTypeFilter(BizMethod.class));
        scanner.addIncludeFilter(new MethodAnnotationTypeFilter(BizApi.class));
        scanner.addIncludeFilter(new AnnotationTypeFilter(BizCause.class));

        for (BeanDefinition bd : scanner.findCandidateComponents("com.zifang")) {
            Class<?> clazz;
            try {
                clazz = Class.forName(bd.getBeanClassName());
            } catch (ClassNotFoundException e) {
                continue;
            }
            if (!processedClasses.add(clazz.getName())) continue;

            collectClassCause(clazz);

            String module = "";
            BizModel modelAnn = clazz.getAnnotation(BizModel.class);
            if (modelAnn != null) module = modelAnn.module();

            for (Method method : clazz.getDeclaredMethods()) {
                BizMethodDTO methodDto = collectMethod(clazz, method, module);
                if (methodDto != null) methods.put(methodDto.getCode(), methodDto);
            }
        }
    }

    /** 类级 {@link BizCause}：领域对象本身的效果。 */
    private void collectClassCause(Class<?> clazz) {
        BizCause cause = clazz.getAnnotation(BizCause.class);
        if (cause == null) return;
        causes.add(toCause(cause, clazz.getSimpleName(), clazz.getName()));
    }

    /** 方法级采集：{@link BizMethod} + {@link BizParam} + {@link BizCause}。 */
    private BizMethodDTO collectMethod(Class<?> clazz, Method method, String defaultModule) {
        BizMethod methodAnn = method.getAnnotation(BizMethod.class);
        BizCause causeAnn = method.getAnnotation(BizCause.class);
        BizApi apiAnn = method.getAnnotation(BizApi.class);
        if (methodAnn == null && causeAnn == null && apiAnn == null) return null;

        BizMethodDTO dto = new BizMethodDTO();
        dto.setCode(clazz.getSimpleName() + "#" + method.getName());
        dto.setDeclaringClass(clazz.getName());
        dto.setMethodName(method.getName());

        if (methodAnn != null) {
            dto.setName(methodAnn.name());
            dto.setModule(methodAnn.module().isEmpty() ? defaultModule : methodAnn.module());
            dto.setDescription(methodAnn.description());
            dto.setOperation(methodAnn.operation().name());
            dto.setIdempotent(methodAnn.idempotent());
        } else {
            dto.setName(method.getName());
            dto.setModule(defaultModule);
            dto.setOperation(BizMethod.Operation.OTHER.name());
        }

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
            pd.setPosition(paramAnn.position().name());
            params.add(pd);
        }
        dto.setParams(params);

        if (causeAnn != null) {
            BizCauseDTO cd = toCause(causeAnn, clazz.getSimpleName(), dto.getCode());
            causes.add(cd);
            dto.getCauses().add(cd);
        }

        if (apiAnn != null) {
            BizApiDTO ad = new BizApiDTO();
            ad.setPath(apiAnn.path());
            ad.setHttpMethod(apiAnn.httpMethod());
            ad.setSummary(apiAnn.summary());
            ad.setDescription(apiAnn.description());
            ad.setTags(new ArrayList<>(List.of(apiAnn.tags())));
            ad.setProduces(apiAnn.produces());
            ad.setDeprecated(apiAnn.deprecated());
            ad.setDeclaringClass(clazz.getName());
            ad.setMethodName(method.getName());
            ad.setParams(params);
            dto.setApi(ad);
        }

        return dto;
    }

    private BizCauseDTO toCause(BizCause cause, String defaultSubject, String source) {
        BizCauseDTO dto = new BizCauseDTO();
        dto.setSubject(cause.subject().isEmpty() ? defaultSubject : cause.subject());
        dto.setEffect(cause.effect());
        dto.setBecause(cause.because());
        dto.setCondition(cause.condition());
        dto.setWeight(cause.weight());
        dto.setSource(source);
        return dto;
    }

    public List<BizMethodDTO> getAllMethods() {
        return new ArrayList<>(methods.values());
    }

    public BizMethodDTO getMethod(String code) {
        return methods.get(code);
    }

    public List<BizMethodDTO> getMethodsByModule(String module) {
        List<BizMethodDTO> result = new ArrayList<>();
        for (BizMethodDTO dto : methods.values()) {
            if (module.equals(dto.getModule())) result.add(dto);
        }
        return result;
    }

    public List<BizCauseDTO> getAllCauses() {
        return new ArrayList<>(causes);
    }

    public List<BizCauseDTO> getCausesBySubject(String subject) {
        List<BizCauseDTO> result = new ArrayList<>();
        for (BizCauseDTO dto : causes) {
            if (subject.equals(dto.getSubject())) result.add(dto);
        }
        return result;
    }

    public List<BizModelDTO> getAllModels() {
        return new ArrayList<>(models.values());
    }

    public BizModelDTO getModel(String code) {
        return models.get(code);
    }

    public List<BizModelDTO> getModelsByModule(String module) {
        List<BizModelDTO> result = new ArrayList<>();
        for (BizModelDTO dto : models.values()) {
            if (dto.getModule().equals(module)) result.add(dto);
        }
        return result;
    }

    @com.fasterxml.jackson.databind.annotation.JsonNaming(com.fasterxml.jackson.databind.PropertyNamingStrategies.LowerCamelCaseStrategy.class)
    public static class BizModelDTO {
        private String code;
        private String name;
        private String module;
        private String description;
        private String className;
        private List<BizFieldDTO> fields;

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getModule() {
            return module;
        }

        public void setModule(String module) {
            this.module = module;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getClassName() {
            return className;
        }

        public void setClassName(String className) {
            this.className = className;
        }

        public List<BizFieldDTO> getFields() {
            return fields;
        }

        public void setFields(List<BizFieldDTO> fields) {
            this.fields = fields;
        }
    }

    @com.fasterxml.jackson.databind.annotation.JsonNaming(com.fasterxml.jackson.databind.PropertyNamingStrategies.LowerCamelCaseStrategy.class)
    public static class BizFieldDTO {
        private String name;
        private String fieldName;
        private String type;
        private String refModel;
        private String refModule;
        private String dictCode;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getFieldName() {
            return fieldName;
        }

        public void setFieldName(String fieldName) {
            this.fieldName = fieldName;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getRefModel() {
            return refModel;
        }

        public void setRefModel(String refModel) {
            this.refModel = refModel;
        }

        public String getRefModule() {
            return refModule;
        }

        public void setRefModule(String refModule) {
            this.refModule = refModule;
        }

        public String getDictCode() {
            return dictCode;
        }

        public void setDictCode(String dictCode) {
            this.dictCode = dictCode;
        }
    }
}
