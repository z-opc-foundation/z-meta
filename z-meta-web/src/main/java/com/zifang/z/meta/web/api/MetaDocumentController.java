package com.zifang.z.meta.web.api;

import com.zifang.util.core.meta.Result;
import com.zifang.z.meta.api.annotation.BizApi;
import com.zifang.z.meta.core.collector.ApiContractCollector;
import com.zifang.z.meta.core.collector.BizCauseDTO;
import com.zifang.z.meta.core.collector.BizMethodDTO;
import com.zifang.z.meta.core.collector.BizModelCollector;
import com.zifang.z.meta.core.openapi.OpenApiDocumentGenerator;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 元数据文档出口：自研 OpenAPI 文档 + 方法元数据 + 因果链。
 *
 * <p>{@code /openapi.json} 直出文档本体（不带 {@code Result} 信封），便于工具与认知板块直接抓取；
 * 其余端点沿用组织统一的 {@code Result} 信封。</p>
 *
 * @author zifang
 * @since 1.1.0
 */
@RestController
@RequestMapping("/api/meta")
public class MetaDocumentController {

    @Resource
    private OpenApiDocumentGenerator openApiGenerator;

    @Resource
    private BizModelCollector modelCollector;

    @Resource
    private ApiContractCollector apiCollector;

    /**
     * 自研 OpenAPI 3.1 文档。
     *
     * @return 文档本体
     */
    @GetMapping("/openapi.json")
    @BizApi(summary = "自研 OpenAPI 3.1 文档", description = "自研 OpenAPI 3.1 文档。", tags = { "z-meta" })
    public Map<String, Object> openapi() {
        return openApiGenerator.generate();
    }

    /**
     * 全量因果链：方法级「因为 / 条件 / 导致」的边。
     *
     * @return 因果边列表
     */
    @GetMapping("/causes")
    @BizApi(summary = "全量因果链：方法级「因为 / 条件 / 导致」的边", description = "全量因果链：方法级「因为 / 条件 / 导致」的边。", tags = { "z-meta" })
    public Result<List<BizCauseDTO>> causes() {
        return Result.success(openApiGenerator.allCauses());
    }

    /**
     * 全量方法元数据。
     *
     * @return 方法列表
     */
    @GetMapping("/methods")
    @BizApi(summary = "全量方法元数据", description = "全量方法元数据。", tags = { "z-meta" })
    public Result<List<BizMethodDTO>> methods() {
        return Result.success(modelCollector.getAllMethods());
    }
}



