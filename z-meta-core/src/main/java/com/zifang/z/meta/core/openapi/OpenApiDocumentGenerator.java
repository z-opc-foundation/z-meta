package com.zifang.z.meta.core.openapi;

import com.zifang.z.meta.core.collector.ApiContractCollector;
import com.zifang.z.meta.core.collector.BizApiDTO;
import com.zifang.z.meta.core.collector.BizCauseDTO;
import com.zifang.z.meta.core.collector.BizMethodDTO;
import com.zifang.z.meta.core.collector.BizModelCollector;
import com.zifang.z.meta.core.collector.BizParamDTO;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 自研 OpenAPI 文档生成器 —— 比 Swagger 多一层业务语义。
 *
 * <p>Swagger 只知道「有个 GET /models/{code}，参数是 code」。
 * 本生成器额外给出：</p>
 * <ul>
 *   <li>{@code components.schemas}：{@code @BizModel} / {@code @BizField} 声明的业务模型与字段口径；</li>
 *   <li>参数的 {@code x-refModel} / {@code x-dictCode}：这个参数引用哪张表、查哪个字典；</li>
 *   <li>端点的 {@code x-operation} / {@code x-idempotent}：业务操作类型与幂等性；</li>
 *   <li>顶层 {@code x-causes}：方法级因果边（Swagger 没有这个概念）。</li>
 * </ul>
 *
 * <p>这一份文档既是给人的接口手册，也是给认知板块的原料。</p>
 *
 * @author zifang
 * @since 1.1.0
 */
@Component
public class OpenApiDocumentGenerator {

    private final BizModelCollector modelCollector;
    private final ApiContractCollector apiCollector;

    public OpenApiDocumentGenerator(BizModelCollector modelCollector, ApiContractCollector apiCollector) {
        this.modelCollector = modelCollector;
        this.apiCollector = apiCollector;
    }

    /**
     * 生成 OpenAPI 3.1 文档。
     *
     * @return 可直接序列化为 JSON 的文档结构
     */
    public Map<String, Object> generate() {
        Map<String, Object> doc = new LinkedHashMap<>();
        doc.put("openapi", "3.1.0");

        Map<String, Object> info = new LinkedHashMap<>();
        info.put("title", "z-meta 元数据 API");
        info.put("version", "1.1.0");
        info.put("description", "由 z-meta 注解体系采集生成：路径与 HTTP 方法取自 Spring 映射，"
                + "业务语义取自 @BizModel / @BizField / @BizMethod / @BizParam / @BizApi / @BizCause。");
        doc.put("info", info);

        doc.put("paths", new LinkedHashMap<String, Object>(buildPaths()));
        doc.put("components", buildComponents());
        doc.put("x-causes", buildCauses());
        doc.put("x-models", modelCollector.getAllModels());
        doc.put("x-methods", modelCollector.getAllMethods());
        return doc;
    }

    private Map<String, Map<String, Object>> buildPaths() {
        Map<String, Map<String, Object>> paths = new LinkedHashMap<>();
        for (BizApiDTO endpoint : apiCollector.getAllEndpoints()) {
            Map<String, Object> pathItem = paths.computeIfAbsent(endpoint.getPath(),
                    k -> new LinkedHashMap<String, Object>());
            Map<String, Object> operation = new LinkedHashMap<>();
            operation.put("operationId", endpoint.getDeclaringClass() + "#" + endpoint.getMethodName());
            operation.put("summary", endpoint.getSummary());
            if (endpoint.getDescription() != null && !endpoint.getDescription().isEmpty()) {
                operation.put("description", endpoint.getDescription());
            }
            if (!endpoint.getTags().isEmpty()) operation.put("tags", endpoint.getTags());
            if (!endpoint.getParams().isEmpty()) {
                operation.put("parameters", buildParameters(endpoint.getParams()));
            }
            Map<String, Object> responses = new LinkedHashMap<>();
            Map<String, Object> ok = new LinkedHashMap<>();
            ok.put("description", "成功");
            responses.put("200", ok);
            operation.put("responses", responses);
            if (endpoint.isDeprecated()) operation.put("deprecated", true);
            operation.put("x-contentType", endpoint.getProduces());
            pathItem.put(endpoint.getHttpMethod().toLowerCase(), operation);
        }
        return paths;
    }

    private List<Map<String, Object>> buildParameters(List<BizParamDTO> params) {
        List<Map<String, Object>> list = new ArrayList<>();
        for (BizParamDTO param : params) {
            Map<String, Object> p = new LinkedHashMap<>();
            p.put("name", param.getName());
            p.put("in", param.getPosition().toLowerCase());
            p.put("required", param.isRequired());
            if (param.getDescription() != null && !param.getDescription().isEmpty()) {
                p.put("description", param.getDescription());
            }
            Map<String, Object> schema = new LinkedHashMap<>();
            schema.put("type", openApiType(param.getType()));
            p.put("schema", schema);
            if (param.getRefModel() != null && !param.getRefModel().isEmpty()) {
                p.put("x-refModel", param.getRefModel());
            }
            if (param.getRefModule() != null && !param.getRefModule().isEmpty()) {
                p.put("x-refModule", param.getRefModule());
            }
            if (param.getDictCode() != null && !param.getDictCode().isEmpty()) {
                p.put("x-dictCode", param.getDictCode());
            }
            p.put("x-businessType", param.getType());
            list.add(p);
        }
        return list;
    }

    private Map<String, Object> buildComponents() {
        Map<String, Object> components = new LinkedHashMap<>();
        Map<String, Object> schemas = new LinkedHashMap<>();
        for (BizModelCollector.BizModelDTO model : modelCollector.getAllModels()) {
            Map<String, Object> schema = new LinkedHashMap<>();
            schema.put("title", model.getName());
            if (model.getDescription() != null && !model.getDescription().isEmpty()) {
                schema.put("description", model.getDescription());
            }
            schema.put("x-module", model.getModule());
            schema.put("x-className", model.getClassName());
            Map<String, Object> properties = new LinkedHashMap<>();
            if (model.getFields() != null) {
                for (BizModelCollector.BizFieldDTO field : model.getFields()) {
                    Map<String, Object> prop = new LinkedHashMap<>();
                    prop.put("type", openApiType(field.getType()));
                    prop.put("x-businessType", field.getType());
                    if (field.getRefModel() != null && !field.getRefModel().isEmpty()) {
                        prop.put("x-refModel", field.getRefModel());
                    }
                    if (field.getDictCode() != null && !field.getDictCode().isEmpty()) {
                        prop.put("x-dictCode", field.getDictCode());
                    }
                    properties.put(field.getFieldName(), prop);
                }
            }
            schema.put("properties", properties);
            schemas.put(model.getCode(), schema);
        }
        components.put("schemas", schemas);
        return components;
    }

    private List<Map<String, Object>> buildCauses() {
        List<Map<String, Object>> list = new ArrayList<>();
        for (BizCauseDTO cause : allCauses()) {
            Map<String, Object> edge = new LinkedHashMap<>();
            edge.put("subject", cause.getSubject());
            edge.put("predicate", "causes");
            edge.put("object", cause.getEffect());
            edge.put("because", cause.getBecause());
            edge.put("condition", cause.getCondition());
            edge.put("weight", cause.getWeight());
            edge.put("source", cause.getSource());
            list.add(edge);
        }
        return list;
    }

    /**
     * 合并两路采集器的因果边并去重。
     *
     * <p>同一个类可能既带 {@code @BizApi} 又带 {@code @BizCause}，两路采集器会各收一遍。</p>
     *
     * @return 去重后的因果边
     */
    public List<BizCauseDTO> allCauses() {
        List<BizCauseDTO> all = new ArrayList<>(modelCollector.getAllCauses());
        all.addAll(apiCollector.getAllCauses());
        Map<String, BizCauseDTO> unique = new LinkedHashMap<>();
        for (BizCauseDTO cause : all) {
            String key = cause.getSubject() + "|" + cause.getEffect() + "|" + cause.getSource();
            unique.putIfAbsent(key, cause);
        }
        return new ArrayList<>(unique.values());
    }

    /** 业务类型 → OpenAPI 基础类型。 */
    private String openApiType(String businessType) {
        if (businessType == null) return "string";
        switch (businessType) {
            case "INTEGER":
                return "integer";
            case "DECIMAL":
                return "number";
            case "BOOLEAN":
                return "boolean";
            case "JSON":
                return "object";
            case "ENUM":
                return "string";
            default:
                return "string";
        }
    }
}
