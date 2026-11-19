package com.zifang.z.meta.admin.api;

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

import java.util.List;
import java.util.Map;

/**
 * 元数据文档出口（standalone 壳）：自研 OpenAPI 文档 + 方法元数据 + 因果链。
 *
 * <p>与 {@code z-meta-web} 的 {@code MetaDocumentController} 是同一份能力的两份拷贝
 * （web 版供宿主嵌入，admin 版供独立启动壳）。</p>
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
     * 全量因果链。
     *
     * @return 因果边列表
     */
    @GetMapping("/causes")
    @BizApi(summary = "全量因果链", description = "全量因果链。", tags = { "z-meta" })
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



