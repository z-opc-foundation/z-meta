package com.zifang.z.meta.web.api;

import com.zifang.util.core.meta.Result;
import com.zifang.z.meta.api.annotation.BizApi;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * z-meta 健康检查 Controller.
 * <p>
 * API 基础路径: /api/health
 * 所属模块: z-meta-admin
 * 鉴权: 无 (运维探活使用)
 *
 * <p>主要端点:
 * <ul>
 *   <li>GET /api/health — 返回 {@code z-meta is running}, 探活用</li>
 * </ul>
 */
@Tag(name = "健康检查")
@RestController("metaHealthController")
@RequestMapping("/api/health")
public class HealthController {

    /**
     * 健康检查端点, 返回固定字符串 {@code "z-meta is running"}.
     *
     * @return 包含运行提示的 {@link Result}
     */
    @Operation(summary = "健康检查")
    @GetMapping
    @BizApi(summary = "健康检查端点, 返回固定字符串 \"z-meta is running\"", description = "健康检查端点, 返回固定字符串 \"z-meta is running\".", tags = { "z-meta" })
    public Result<String> health() {
        return Result.success("z-meta is running");
    }
}

