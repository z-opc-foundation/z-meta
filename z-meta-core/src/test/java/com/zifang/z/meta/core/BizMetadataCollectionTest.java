package com.zifang.z.meta.core;

import com.zifang.z.meta.api.annotation.BizApi;
import com.zifang.z.meta.api.annotation.BizCause;
import com.zifang.z.meta.api.annotation.BizField;
import com.zifang.z.meta.api.annotation.BizMethod;
import com.zifang.z.meta.api.annotation.BizModel;
import com.zifang.z.meta.api.annotation.BizParam;
import com.zifang.z.meta.core.collector.ApiContractCollector;
import com.zifang.z.meta.core.collector.BizApiDTO;
import com.zifang.z.meta.core.collector.BizCauseDTO;
import com.zifang.z.meta.core.collector.BizMethodDTO;
import com.zifang.z.meta.core.collector.BizModelCollector;
import com.zifang.z.meta.core.openapi.OpenApiDocumentGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 注解 → 采集 → 文档 全链路自守卫。
 *
 * <p>每条主张都用真实反射跑出来，不靠手抄预期值。</p>
 */
class BizMetadataCollectionTest {

    @BizModel(name = "订单", module = "trade", description = "订单主模型")
    public static class OrderModel {

        @BizField(name = "订单号", type = BizField.FieldType.STRING)
        private String orderNo;

        @BizField(name = "状态", type = BizField.FieldType.ENUM, dictCode = "order_status")
        private String status;
    }

    @BizCause(effect = "订单已创建", because = "用户提交订单", condition = "库存充足", weight = 0.9)
    public static class OrderService {

        @BizMethod(name = "创建订单", module = "trade", description = "下单主流程", operation = BizMethod.Operation.CREATE)
        @BizApi(summary = "创建订单", tags = {"订单"})
        @BizCause(effect = "库存已扣减", because = "订单已创建", condition = "库存充足")
        public String create(@BizParam(name = "订单号", type = BizField.FieldType.STRING, description = "业务单号") String orderNo) {
            return orderNo;
        }

        @BizMethod(name = "查询订单", module = "trade", operation = BizMethod.Operation.QUERY, idempotent = true)
        public String query(@BizParam(name = "订单号", type = BizField.FieldType.STRING) String orderNo) {
            return orderNo;
        }
    }

    @RestController
    @RequestMapping("/api/meta")
    public static class FixtureController {

        @GetMapping("/fixture/ping")
        @BizApi(summary = "探活", tags = {"夹具"})
        public String ping() {
            return "pong";
        }

        @PostMapping("/fixture/echo")
        @BizApi(summary = "回显", deprecated = true)
        public String echo(@BizParam(name = "内容", type = BizField.FieldType.TEXT) String body) {
            return body;
        }
    }

    private BizModelCollector modelCollector;
    private ApiContractCollector apiCollector;
    private OpenApiDocumentGenerator generator;

    @BeforeEach
    void setUp() {
        modelCollector = new BizModelCollector();
        modelCollector.collect();
        apiCollector = new ApiContractCollector();
        apiCollector.collect();
        generator = new OpenApiDocumentGenerator(modelCollector, apiCollector);
    }

    @Test
    @DisplayName("模型与字段照旧采到，且新增业务模型进 components.schemas")
    void modelsAreCollected() {
        List<BizModelCollector.BizModelDTO> models = modelCollector.getAllModels();
        BizModelCollector.BizModelDTO order = models.stream()
                .filter(m -> "OrderModel".equals(m.getCode()))
                .findFirst()
                .orElse(null);
        assertNotNull(order, "带 @BizModel 的类应被采到");
        assertEquals("订单", order.getName());
        assertEquals("trade", order.getModule());
        assertEquals(2, order.getFields().size());

        Map<String, Object> doc = generator.generate();
        Map<String, Object> components = (Map<String, Object>) doc.get("components");
        Map<String, Object> schemas = (Map<String, Object>) components.get("schemas");
        assertTrue(schemas.containsKey("OrderModel"), "业务模型应进 components.schemas");
        Map<String, Object> schema = (Map<String, Object>) schemas.get("OrderModel");
        assertEquals("trade", schema.get("x-module"));
        Map<String, Object> props = (Map<String, Object>) schema.get("properties");
        assertTrue(props.containsKey("orderNo"), "字段应进 properties");
    }

    @Test
    @DisplayName("方法、入参、业务操作类型被采到")
    void methodsAndParamsAreCollected() {
        List<BizMethodDTO> methods = modelCollector.getAllMethods();
        BizMethodDTO create = methods.stream()
                .filter(m -> "OrderService#create".equals(m.getCode()))
                .findFirst()
                .orElse(null);
        assertNotNull(create, "带 @BizMethod 的方法应被采到");
        assertEquals("创建订单", create.getName());
        assertEquals("CREATE", create.getOperation());
        assertEquals(1, create.getParams().size());
        assertEquals("订单号", create.getParams().get(0).getName());
        assertEquals("STRING", create.getParams().get(0).getType());

        BizMethodDTO query = methods.stream()
                .filter(m -> "OrderService#query".equals(m.getCode()))
                .findFirst()
                .orElse(null);
        assertNotNull(query);
        assertEquals("QUERY", query.getOperation());
        assertTrue(query.isIdempotent(), "idempotent=true 应被采到");
    }

    @Test
    @DisplayName("类级与方法级因果边都采到，字段完整")
    void causesAreCollected() {
        List<BizCauseDTO> causes = modelCollector.getAllCauses();
        assertEquals(2, causes.size(), "类级 1 条 + 方法级 1 条");

        BizCauseDTO classLevel = causes.stream()
                .filter(c -> "OrderService".equals(c.getSubject()))
                .findFirst()
                .orElse(null);
        assertNotNull(classLevel);
        assertEquals("订单已创建", classLevel.getEffect());
        assertEquals("用户提交订单", classLevel.getBecause());
        assertEquals("库存充足", classLevel.getCondition());
        assertEquals(0.9, classLevel.getWeight(), 1e-9);

        BizCauseDTO methodLevel = causes.stream()
                .filter(c -> "库存已扣减".equals(c.getEffect()))
                .findFirst()
                .orElse(null);
        assertNotNull(methodLevel);
        assertEquals("OrderService#create", methodLevel.getSource());
    }

    @Test
    @DisplayName("端点契约按 Spring 映射采集，业务注解叠加上去")
    void endpointsAreCollected() {
        BizApiDTO ping = apiCollector.getEndpoint("GET", "/api/meta/fixture/ping");
        assertNotNull(ping, "应采到端点");
        assertEquals("探活", ping.getSummary());
        assertEquals(1, ping.getTags().size());
        assertEquals("夹具", ping.getTags().get(0));
        assertFalse(ping.isDeprecated());

        BizApiDTO echo = apiCollector.getEndpoint("POST", "/api/meta/fixture/echo");
        assertNotNull(echo);
        assertTrue(echo.isDeprecated(), "@BizApi(deprecated=true) 应被采到");
        assertEquals(1, echo.getParams().size());
        assertEquals("内容", echo.getParams().get(0).getName());
    }

    @Test
    @DisplayName("OpenAPI 文档：paths 有端点、x-causes 有因果边")
    void openApiDocumentIsGenerated() {
        Map<String, Object> doc = generator.generate();
        assertEquals("3.1.0", doc.get("openapi"));

        Map<String, Object> paths = (Map<String, Object>) doc.get("paths");
        assertTrue(paths.containsKey("/api/meta/fixture/ping"), "端点应进 paths");
        Map<String, Object> pathItem = (Map<String, Object>) paths.get("/api/meta/fixture/ping");
        Map<String, Object> get = (Map<String, Object>) pathItem.get("get");
        assertEquals("探活", get.get("summary"));
        assertTrue(((List<String>) get.get("tags")).contains("夹具"));

        List<Map<String, Object>> causes = (List<Map<String, Object>>) doc.get("x-causes");
        assertFalse(causes.isEmpty(), "因果边应出现在 x-causes");
        assertTrue(causes.stream().anyMatch(c -> "causes".equals(c.get("predicate"))));

        List<BizMethodDTO> methods = (List<BizMethodDTO>) doc.get("x-methods");
        assertTrue(methods.stream().anyMatch(m -> "OrderService#create".equals(m.getCode())));
    }
}
