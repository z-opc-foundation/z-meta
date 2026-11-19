package com.zifang.z.meta.web.api;

import com.zifang.z.meta.core.collector.ApiContractCollector;
import com.zifang.z.meta.core.collector.BizModelCollector;
import com.zifang.z.meta.core.openapi.OpenApiDocumentGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 元数据文档出口的 HTTP 级自守卫：路由通不通、出的 JSON 对不对。
 *
 * <p>用 standaloneSetup 而不是整套 Spring 上下文 —— 这三个端点不碰数据库，
 * 起整个上下文反而会引出数据源装配问题，测的就不是被测物了。</p>
 */
class MetaDocumentControllerTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        BizModelCollector modelCollector = new BizModelCollector();
        modelCollector.collect();
        ApiContractCollector apiCollector = new ApiContractCollector();
        apiCollector.collect();
        OpenApiDocumentGenerator generator = new OpenApiDocumentGenerator(modelCollector, apiCollector);

        MetaDocumentController controller = new MetaDocumentController();
        ReflectionTestUtils.setField(controller, "openApiGenerator", generator);
        ReflectionTestUtils.setField(controller, "modelCollector", modelCollector);
        ReflectionTestUtils.setField(controller, "apiCollector", apiCollector);

        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    @DisplayName("GET /api/meta/openapi.json 直出文档本体，不带 Result 信封")
    void openApiJsonIsServedRaw() throws Exception {
        mockMvc.perform(get("/api/meta/openapi.json"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.openapi").value("3.1.0"))
                .andExpect(jsonPath("$.info.title").value("z-meta 元数据 API"))
                .andExpect(jsonPath("$.paths").isMap())
                .andExpect(jsonPath("$.components.schemas").isMap())
                .andExpect(jsonPath("$.x-causes").isArray());
    }

    @Test
    @DisplayName("GET /api/meta/causes 走 Result 信封")
    void causesUseResultEnvelope() throws Exception {
        mockMvc.perform(get("/api/meta/causes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @DisplayName("GET /api/meta/methods 走 Result 信封")
    void methodsUseResultEnvelope() throws Exception {
        mockMvc.perform(get("/api/meta/methods"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray());
    }
}
