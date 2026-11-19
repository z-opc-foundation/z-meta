package com.zifang.z.meta.starter.repo;

import com.zifang.util.core.meta.Result;
import com.zifang.z.meta.api.annotation.BizApi;
import com.zifang.z.meta.api.annotation.BizField;
import com.zifang.z.meta.api.annotation.BizParam;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 仓库自述端点 —— 本仓启动后自报家门。
 *
 * <p>四个视角：
 * <ul>
 *   <li>{@code /api/meta/repo/info} —— 我是谁（坐标/版本/git commit/JVM/端口/启动时间）</li>
 *   <li>{@code /api/meta/repo/apis} —— 我对外提供什么（端点契约，含 {@code @BizApi} 语义）</li>
 *   <li>{@code /api/meta/repo/models} —— 我的数据长什么样（{@code @BizModel} 字段）</li>
 *   <li>{@code /api/meta/repo/methods} —— 我能做哪些业务动作（{@code @BizMethod}）</li>
 * </ul>
 *
 * @author zifang
 * @since 1.0.3
 */
@RestController
@RequestMapping("/api/meta/repo")
public class RepoInfoController {

    private final RepoInfoService repoInfoService;

    public RepoInfoController(RepoInfoService repoInfoService) {
        this.repoInfoService = repoInfoService;
    }

    /**
     * 本仓身份与运行时信息。
     *
     * @return 身份信息
     */
    @GetMapping("/info")
    @BizApi(summary = "本仓身份与运行时信息", description = "返回本仓的坐标、版本、git commit、JVM、端口与启动时间。", tags = { "z-meta" })
    public Result<Map<String, Object>> info() {
        return Result.success(repoInfoService.identity());
    }

    /**
     * 本仓对外端点契约。
     *
     * @param tag 可选，按业务标签过滤
     * @return 端点契约列表
     */
    @GetMapping("/apis")
    @BizApi(summary = "本仓对外端点契约", description = "返回本进程真正注册的路由及其 @BizApi 业务语义。", tags = { "z-meta" })
    public Result<List<RepoSelfDescriptor.EndpointInfo>> apis(
            @RequestParam(required = false) @BizParam(name = "tag", type = BizField.FieldType.STRING, description = "按业务标签过滤", position = BizParam.Position.QUERY) String tag) {
        List<RepoSelfDescriptor.EndpointInfo> all = repoInfoService.descriptor().getEndpoints();
        if (tag == null || tag.isEmpty()) return Result.success(all);
        List<RepoSelfDescriptor.EndpointInfo> filtered = new ArrayList<>();
        for (RepoSelfDescriptor.EndpointInfo info : all) {
            if (info.getTags() != null && info.getTags().contains(tag)) filtered.add(info);
        }
        return Result.success(filtered);
    }

    /**
     * 本仓数据模型。
     *
     * @param module 可选，按业务模块过滤
     * @return 数据模型列表
     */
    @GetMapping("/models")
    @BizApi(summary = "本仓数据模型", description = "返回本仓带 @BizModel 的数据模型及其字段。", tags = { "z-meta" })
    public Result<List<RepoSelfDescriptor.ModelInfo>> models(
            @RequestParam(required = false) @BizParam(name = "module", type = BizField.FieldType.STRING, description = "按业务模块过滤", position = BizParam.Position.QUERY) String module) {
        List<RepoSelfDescriptor.ModelInfo> all = repoInfoService.descriptor().getModels();
        if (module == null || module.isEmpty()) return Result.success(all);
        List<RepoSelfDescriptor.ModelInfo> filtered = new ArrayList<>();
        for (RepoSelfDescriptor.ModelInfo info : all) {
            if (module.equals(info.getModule())) filtered.add(info);
        }
        return Result.success(filtered);
    }

    /**
     * 按模型编码取单个数据模型。
     *
     * @param code 模型编码
     * @return 数据模型
     */
    @GetMapping("/models/{code}")
    @BizApi(summary = "按编码取数据模型", description = "按模型编码返回单个数据模型。", tags = { "z-meta" })
    public Result<RepoSelfDescriptor.ModelInfo> model(
            @PathVariable("code") @BizParam(name = "code", type = BizField.FieldType.STRING, description = "模型编码", position = BizParam.Position.PATH) String code) {
        for (RepoSelfDescriptor.ModelInfo info : repoInfoService.descriptor().getModels()) {
            if (code.equals(info.getCode())) return Result.success(info);
        }
        return Result.<RepoSelfDescriptor.ModelInfo>fail("模型不存在：" + code).code(404);
    }

    /**
     * 本仓业务方法。
     *
     * @param module 可选，按业务模块过滤
     * @return 业务方法列表
     */
    @GetMapping("/methods")
    @BizApi(summary = "本仓业务方法", description = "返回本仓带 @BizMethod 的业务动作清单。", tags = { "z-meta" })
    public Result<Map<String, RepoSelfDescriptor.MethodInfo>> methods(
            @RequestParam(required = false) @BizParam(name = "module", type = BizField.FieldType.STRING, description = "按业务模块过滤", position = BizParam.Position.QUERY) String module) {
        Map<String, RepoSelfDescriptor.MethodInfo> all = repoInfoService.descriptor().getMethods();
        if (module == null || module.isEmpty()) return Result.success(all);
        Map<String, RepoSelfDescriptor.MethodInfo> filtered = new LinkedHashMap<>();
        all.forEach((key, value) -> {
            if (module.equals(value.getModule())) filtered.put(key, value);
        });
        return Result.success(filtered);
    }
}
