package com.zifang.z.meta.web.api;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zifang.util.core.meta.Result;
import com.zifang.z.meta.api.annotation.BizApi;
import com.zifang.z.meta.api.annotation.BizField;
import com.zifang.z.meta.api.annotation.BizParam;
import com.zifang.z.meta.core.domain.entity.ZApplicationDO;
import com.zifang.z.meta.core.domain.service.IApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

/**
 * 元数据 - 应用管理 Controller.
 * <p>
 * API 基础路径: /api/meta-app
 * 所属模块: z-meta-admin
 * 鉴权: 由 z-ctc 统一拦截
 *
 * <p>主要端点:
 * <ul>
 *   <li>GET    /api/meta-app/list  — 分页查询应用</li>
 *   <li>GET    /api/meta-app/get   — 按 id 获取应用详情</li>
 *   <li>POST   /api/meta-app       — 新建应用 (默认租户 id=1)</li>
 *   <li>PUT    /api/meta-app/{id}  — 更新应用</li>
 *   <li>DELETE /api/meta-app/{id}  — 删除应用</li>
 * </ul>
 */
@Tag(name = "应用管理")
@RestController
@RequestMapping("/api/meta-app")
public class MetaApplicationController {

    @Autowired
    private IApplicationService applicationService;

    /**
     * 分页查询应用, 支持 tenantId / appCode / appName / appType / status 多条件过滤.
     *
     * @param pageNum  页码
     * @param pageSize 每页数量
     * @param tenantId 租户 ID, 可空
     * @param appCode  应用编码, 可空
     * @param appName  应用名称, 可空
     * @param appType  应用类型, 可空
     * @param status   状态, 可空
     * @return 分页结果
     */
    @Operation(summary = "分页查询应用")
    @GetMapping("/list")
    @BizApi(summary = "分页查询应用, 支持 tenantId / appCode / appName / appType / status 多条件过滤", description = "分页查询应用, 支持 tenantId / appCode / appName / appType / status 多条件过滤.", tags = { "z-meta" })
    public Result<IPage<ZApplicationDO>> list(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1")
            @BizParam(name = "pageNum", type = BizField.FieldType.INTEGER, description = "页码", position = BizParam.Position.QUERY) long pageNum,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10")
            @BizParam(name = "pageSize", type = BizField.FieldType.INTEGER, description = "每页数量", position = BizParam.Position.QUERY) long pageSize,
            @Parameter(description = "租户ID") @RequestParam(required = false)
            @BizParam(name = "tenantId", type = BizField.FieldType.INTEGER, description = "租户 ID, 可空", position = BizParam.Position.QUERY) Long tenantId,
            @Parameter(description = "应用编码") @RequestParam(required = false)
            @BizParam(name = "appCode", type = BizField.FieldType.STRING, description = "应用编码, 可空", position = BizParam.Position.QUERY) String appCode,
            @Parameter(description = "应用名称") @RequestParam(required = false)
            @BizParam(name = "appName", type = BizField.FieldType.STRING, description = "应用名称, 可空", position = BizParam.Position.QUERY) String appName,
            @Parameter(description = "应用类型") @RequestParam(required = false)
            @BizParam(name = "appType", type = BizField.FieldType.STRING, description = "应用类型, 可空", position = BizParam.Position.QUERY) String appType,
            @Parameter(description = "状态") @RequestParam(required = false)
            @BizParam(name = "status", type = BizField.FieldType.INTEGER, description = "状态, 可空", position = BizParam.Position.QUERY) Integer status) {

        ZApplicationDO application = new ZApplicationDO();
        application.setTenantId(tenantId);
        if (StringUtils.hasText(appCode)) {
            application.setAppCode(appCode);
        }
        if (StringUtils.hasText(appName)) {
            application.setAppName(appName);
        }
        if (StringUtils.hasText(appType)) {
            application.setAppType(appType);
        }
        application.setStatus(status);

        Page<ZApplicationDO> page = new Page<>(pageNum, pageSize);
        IPage<ZApplicationDO> result = applicationService.pageApplication(page, application);
        return Result.success(result);
    }

    /**
     * 按 id 获取应用详情.
     *
     * @param id 应用主键
     * @return 应用实体, 不存在时为 null
     */
    @Operation(summary = "获取应用详情")
    @GetMapping("/get")
    @BizApi(summary = "按 id 获取应用详情", description = "按 id 获取应用详情.", tags = { "z-meta" })
    public Result<ZApplicationDO> getById(@Parameter(description = "应用ID") @RequestParam @BizParam(name = "id", type = BizField.FieldType.INTEGER, description = "应用主键", position = BizParam.Position.QUERY) Long id) {
        ZApplicationDO application = applicationService.getById(id);
        return Result.success(application);
    }

    /**
     * 创建一个新应用.
     * <p>
     * 默认使用系统租户 (id=1); 同一租户下 appCode 重复时返回失败.
     *
     * @param application 应用实体 (请求体, 内部填充 id 与默认 tenantId)
     * @return 保存后的应用实体
     */
    @Operation(summary = "创建应用")
    @PostMapping
    @BizApi(summary = "创建一个新应用", description = "创建一个新应用. 默认使用系统租户 (id=1); 同一租户下 appCode 重复时返回失败.", tags = { "z-meta" })
    public Result<ZApplicationDO> create(@RequestBody @BizParam(name = "application", type = BizField.FieldType.STRING, description = "应用实体 (请求体, 内部填充 id 与默认 tenantId)", position = BizParam.Position.BODY) ZApplicationDO application) {
        // 默认使用系统租户 (id=1)
        if (application.getTenantId() == null) {
            application.setTenantId(1L);
        }
        // 检查应用编码是否已存在
        ZApplicationDO exist = applicationService.getByAppCode(application.getTenantId(), application.getAppCode());
        if (exist != null) {
            return Result.fail("应用编码已存在");
        }
        applicationService.save(application);
        return Result.success(application);
    }

    /**
     * 按 id 更新应用.
     *
     * @param id          应用主键
     * @param application 待更新字段
     * @return 更新后的应用实体
     */
    @Operation(summary = "更新应用")
    @PutMapping
    @BizApi(summary = "按 id 更新应用", description = "按 id 更新应用.", tags = { "z-meta" })
    public Result<ZApplicationDO> update(@Parameter(description = "应用ID") @RequestParam @BizParam(name = "id", type = BizField.FieldType.INTEGER, description = "应用主键", position = BizParam.Position.QUERY) Long id, @RequestBody @BizParam(name = "application", type = BizField.FieldType.STRING, description = "待更新字段", position = BizParam.Position.BODY) ZApplicationDO application) {
        application.setId(id);
        applicationService.updateById(application);
        return Result.success(applicationService.getById(id));
    }

    /**
     * 按 id 删除应用.
     *
     * @param id 应用主键
     * @return 成功结果
     */
    @Operation(summary = "删除应用")
    @DeleteMapping
    @BizApi(summary = "按 id 删除应用", description = "按 id 删除应用.", tags = { "z-meta" })
    public Result<Object> delete(@Parameter(description = "应用ID") @RequestParam @BizParam(name = "id", type = BizField.FieldType.INTEGER, description = "应用主键", position = BizParam.Position.QUERY) Long id) {
        applicationService.removeById(id);
        return Result.success();
    }
}

