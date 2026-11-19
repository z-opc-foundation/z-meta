package com.zifang.z.meta.starter.repo;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 装配冒烟：starter 被自动装配后, /api/meta/repo/** 必须真的能应答,
 * 且 /apis 里能看到本测试应用自己注册的路由 —— 这是对"启动之后有一批端点可拿到 repo 信息"的直接断言。
 */
@SpringBootTest(classes = RepoInfoControllerTest.TestApp.class,
        properties = "spring.application.name=z-meta-starter-test")
@AutoConfigureMockMvc
class RepoInfoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void infoReturnsIdentity() throws Exception {
        mockMvc.perform(get("/api/meta/repo/info"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.application").value("z-meta-starter-test"))
                .andExpect(jsonPath("$.data.endpointCount").value(org.hamcrest.Matchers.greaterThan(0)))
                .andExpect(jsonPath("$.data.javaVersion").isNotEmpty());
    }

    @Test
    void apisListsRegisteredRoutes() throws Exception {
        // /api/meta/repo/info 这条路由本身必须出现在自述里
        mockMvc.perform(get("/api/meta/repo/apis"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[?(@.path=='/api/meta/repo/info')]").isNotEmpty());
    }

    @Test
    void modelsAndMethodsAreEmptyButReachable() throws Exception {
        mockMvc.perform(get("/api/meta/repo/models")).andExpect(status().isOk());
        mockMvc.perform(get("/api/meta/repo/methods")).andExpect(status().isOk());
    }

    @Test
    void unknownModelCodeReturns404() throws Exception {
        mockMvc.perform(get("/api/meta/repo/models/__nope__"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(404));
    }

    @SpringBootApplication
    static class TestApp {
    }
}
