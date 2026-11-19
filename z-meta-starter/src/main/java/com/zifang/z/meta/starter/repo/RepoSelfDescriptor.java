package com.zifang.z.meta.starter.repo;

import com.zifang.z.meta.api.annotation.BizApi;
import com.zifang.z.meta.api.annotation.BizField;
import com.zifang.z.meta.api.annotation.BizMethod;
import com.zifang.z.meta.api.annotation.BizModel;
import com.zifang.z.meta.api.annotation.BizParam;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 仓库自述扫描器 —— 从**运行中的 Spring 上下文**里把本仓的端点与数据模型读出来。
 *
 * <p>与 {@code z-meta-core} 那套采集器的分工：那边管「元数据平台视角」（全 classpath 扫描
 * {@code com.zifang}，把契约汇总落库给平台消费）；这边管「本仓视角」（只看本进程真正注册上的
 * 路由 + 本仓类路径上的 {@code @BizModel}），供每个仓自报家门。</p>
 *
 * <p>为什么端点用 {@code RequestMappingHandlerMapping} 而不是 classpath 扫描：前者拿的是
 * Spring **实际注册**的路由（含类级前缀、path pattern、HTTP 方法），后者只能靠猜注解组合，
 * 遇到 {@code @RequestMapping} 拼路径或自定义条件就失真。本仓自述要准，不能猜。</p>
 *
 * <p>零 DB 依赖：本类只读注解与 Spring 元数据，不碰数据库、不写库。</p>
 *
 * @author zifang
 * @since 1.0.3
 */
public class RepoSelfDescriptor {

    /** 端点自述条目。 */
    public static class EndpointInfo {
        private String httpMethod;
        private String path;
        private String controller;
        private String method;
        private String summary;
        private String description;
        private List<String> tags = new ArrayList<>();
        private boolean deprecated;
        private String bizName;
        private String bizModule;
        private String bizOperation;
        private List<ParamInfo> params = new ArrayList<>();

        public String getHttpMethod() {
            return httpMethod;
        }

        public void setHttpMethod(String httpMethod) {
            this.httpMethod = httpMethod;
        }

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }

        public String getController() {
            return controller;
        }

        public void setController(String controller) {
            this.controller = controller;
        }

        public String getMethod() {
            return method;
        }

        public void setMethod(String method) {
            this.method = method;
        }

        public String getSummary() {
            return summary;
        }

        public void setSummary(String summary) {
            this.summary = summary;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public List<String> getTags() {
            return tags;
        }

        public void setTags(List<String> tags) {
            this.tags = tags;
        }

        public boolean isDeprecated() {
            return deprecated;
        }

        public void setDeprecated(boolean deprecated) {
            this.deprecated = deprecated;
        }

        public String getBizName() {
            return bizName;
        }

        public void setBizName(String bizName) {
            this.bizName = bizName;
        }

        public String getBizModule() {
            return bizModule;
        }

        public void setBizModule(String bizModule) {
            this.bizModule = bizModule;
        }

        public String getBizOperation() {
            return bizOperation;
        }

        public void setBizOperation(String bizOperation) {
            this.bizOperation = bizOperation;
        }

        public List<ParamInfo> getParams() {
            return params;
        }

        public void setParams(List<ParamInfo> params) {
            this.params = params;
        }
    }

    /** 参数自述条目。 */
    public static class ParamInfo {
        private String name;
        private String type;
        private boolean required;
        private String description;
        private String position;
        private String refModel;
        private String refModule;
        private String dictCode;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public boolean isRequired() {
            return required;
        }

        public void setRequired(boolean required) {
            this.required = required;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getPosition() {
            return position;
        }

        public void setPosition(String position) {
            this.position = position;
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

    /** 数据模型自述条目。 */
    public static class ModelInfo {
        private String code;
        private String name;
        private String module;
        private String description;
        private String className;
        private List<FieldInfo> fields = new ArrayList<>();

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

        public List<FieldInfo> getFields() {
            return fields;
        }

        public void setFields(List<FieldInfo> fields) {
            this.fields = fields;
        }
    }

    /** 数据模型字段自述条目。 */
    public static class FieldInfo {
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

    /** 本仓端点自述。 */
    private List<EndpointInfo> endpoints = new ArrayList<>();

    /** 本仓数据模型自述。 */
    private List<ModelInfo> models = new ArrayList<>();

    /** 本仓注解方法自述（按 类名#方法 索引）。 */
    private Map<String, MethodInfo> methods = new LinkedHashMap<>();

    public List<EndpointInfo> getEndpoints() {
        return endpoints;
    }

    public void setEndpoints(List<EndpointInfo> endpoints) {
        this.endpoints = endpoints;
    }

    public List<ModelInfo> getModels() {
        return models;
    }

    public void setModels(List<ModelInfo> models) {
        this.models = models;
    }

    public Map<String, MethodInfo> getMethods() {
        return methods;
    }

    public void setMethods(Map<String, MethodInfo> methods) {
        this.methods = methods;
    }

    /** 业务方法自述条目。 */
    public static class MethodInfo {
        private String name;
        private String module;
        private String description;
        private String operation;
        private boolean idempotent;

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

        public String getOperation() {
            return operation;
        }

        public void setOperation(String operation) {
            this.operation = operation;
        }

        public boolean isIdempotent() {
            return idempotent;
        }

        public void setIdempotent(boolean idempotent) {
            this.idempotent = idempotent;
        }
    }

    /**
     * 从方法上读 {@code @BizApi} / {@code @BizMethod}，填进端点条目。
     *
     * @param method       控制器方法
     * @param httpMethod   HTTP 方法
     * @param path         注册路径
     * @param declaringClass 控制器类
     * @return 端点自述条目
     */
    public EndpointInfo describeEndpoint(Method method, String httpMethod, String path, Class<?> declaringClass) {
        EndpointInfo info = new EndpointInfo();
        info.setHttpMethod(httpMethod);
        info.setPath(path);
        info.setController(declaringClass.getName());
        info.setMethod(method.getName());

        BizApi api = method.getAnnotation(BizApi.class);
        if (api != null) {
            info.setSummary(api.summary());
            info.setDescription(api.description());
            info.setTags(new ArrayList<>(List.of(api.tags())));
            info.setDeprecated(api.deprecated());
        }
        BizMethod biz = method.getAnnotation(BizMethod.class);
        if (biz != null) {
            info.setBizName(biz.name());
            info.setBizModule(biz.module());
            info.setBizOperation(biz.operation().name());
        }
        for (Parameter parameter : method.getParameters()) {
            BizParam bp = parameter.getAnnotation(BizParam.class);
            if (bp == null) continue;
            ParamInfo pi = new ParamInfo();
            pi.setName(bp.name());
            pi.setType(bp.type().name());
            pi.setRequired(bp.required());
            pi.setDescription(bp.description());
            pi.setPosition(bp.position().name());
            pi.setRefModel(bp.refModel());
            pi.setRefModule(bp.refModule());
            pi.setDictCode(bp.dictCode());
            info.getParams().add(pi);
        }
        return info;
    }

    /**
     * 从类上读 {@code @BizModel}，填进模型条目。
     *
     * @param clazz 带 {@code @BizModel} 的类
     * @return 模型自述条目
     */
    public ModelInfo describeModel(Class<?> clazz) {
        BizModel model = clazz.getAnnotation(BizModel.class);
        if (model == null) return null;
        ModelInfo info = new ModelInfo();
        info.setCode(clazz.getSimpleName());
        info.setName(model.name());
        info.setModule(model.module());
        info.setDescription(model.description());
        info.setClassName(clazz.getName());
        for (java.lang.reflect.Field field : clazz.getDeclaredFields()) {
            BizField bf = field.getAnnotation(BizField.class);
            if (bf == null) continue;
            FieldInfo fi = new FieldInfo();
            fi.setName(bf.name());
            fi.setFieldName(field.getName());
            fi.setType(bf.type().name());
            fi.setRefModel(bf.refModel());
            fi.setRefModule(bf.refModule());
            fi.setDictCode(bf.dictCode());
            info.getFields().add(fi);
        }
        return info;
    }

    /**
     * 从方法上读 {@code @BizMethod}。
     *
     * @param method 带 {@code @BizMethod} 的方法
     * @param declaringClass 所属类
     * @return 方法自述条目
     */
    public MethodInfo describeMethod(Method method, Class<?> declaringClass) {
        BizMethod biz = method.getAnnotation(BizMethod.class);
        if (biz == null) return null;
        MethodInfo info = new MethodInfo();
        info.setName(biz.name());
        info.setModule(biz.module());
        info.setDescription(biz.description());
        info.setOperation(biz.operation().name());
        info.setIdempotent(biz.idempotent());
        return info;
    }
}
