package com.zifang.z.meta.core.collector;

import com.zifang.z.meta.api.annotation.BizField;
import com.zifang.z.meta.api.annotation.BizModel;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class BizModelCollector {

    private final Map<String, BizModelDTO> models = new ConcurrentHashMap<>();

    @PostConstruct
    public void collect() {
        ClassPathScanningCandidateComponentProvider scanner =
                new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new AnnotationTypeFilter(BizModel.class));

        for (BeanDefinition bd : scanner.findCandidateComponents("com.zifang")) {
            try {
                Class<?> clazz = Class.forName(bd.getBeanClassName());
                BizModel modelAnn = clazz.getAnnotation(BizModel.class);
                if (modelAnn == null) continue;

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
            } catch (ClassNotFoundException e) {
                // skip
            }
        }
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
