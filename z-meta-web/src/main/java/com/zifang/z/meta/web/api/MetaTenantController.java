package com.zifang.z.meta.web.api;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zifang.util.core.meta.Result;
import com.zifang.z.meta.api.annotation.BizApi;
import com.zifang.z.meta.api.annotation.BizField;
import com.zifang.z.meta.api.annotation.BizParam;
import com.zifang.z.meta.core.domain.entity.ZTenantDO;
import com.zifang.z.meta.core.domain.service.ITenantService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

/**
 * 元数据 - 租户管理 Controller.
 * <p>
 * API 基础路径: /api/meta-tenant
 * 所属模块: z-meta-admin
 * 鉴权: 由 z-ctc 统一拦截
 *
 * <p>主要端点:
 * <ul>
 *   <li>GET    /api/meta-tenant/list  — 分页查询租户</li>
 *   <li>GET    /api/meta-tenant/get   — 按 id 获取租户详情</li>
 *   <li>POST   /api/meta-tenant       — 新建租户 (编码重复返回失败)</li>
 *   <li>PUT    /api/meta-tenant/{id}  — 更新租户</li>
 *   <li>DELETE /api/meta-tenant/{id}  — 删除租户</li>
 * </ul>
 */
@Tag(name = "租户管理")
@RestController
@RequestMapping("/api/meta-tenant")
public class MetaTenantController {

    @Autowired
    @Qualifier("tenantServiceImpl")
    private ITenantService tenantService;

    /**
     * 分页查询租户, 支持 tenantCode / tenantName / tenantType / status 多条件过滤.
     *
     * @param pageNum    页码
     * @param pageSize   每页数量
     * @param tenantCode 租户编码, 可空
     * @param tenantName 租户名称, 可空
     * @param tenantType 租户类型, 可空
     * @param status     状态, 可空
     * @return 分页结果
     */
    @Operation(summary = "分页查询租户")
    @GetMapping("/list")
    @BizApi(summary = "分页查询租户, 支持 tenantCode / tenantName / tenantType / status 多条件过滤", description = "分页查询租户, 支持 tenantCode / tenantName / tenantType / status 多条件过滤.", tags = { "z-meta" })
    public Result<IPage<ZTenantDO>> list(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1")
            @BizParam(name = "pageNum", type = BizField.FieldType.INTEGER, description = "页码", position = BizParam.Position.QUERY) long pageNum,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10")
            @BizParam(name = "pageSize", type = BizField.FieldType.INTEGER, description = "每页数量", position = BizParam.Position.QUERY) long pageSize,
            @Parameter(description = "租户编码") @RequestParam(required = false)
            @BizParam(name = "tenantCode", type = BizField.FieldType.STRING, description = "租户编码, 可空", position = BizParam.Position.QUERY) String tenantCode,
            @Parameter(description = "租户名称") @RequestParam(required = false)
            @BizParam(name = "tenantName", type = BizField.FieldType.STRING, description = "租户名称, 可空", position = BizParam.Position.QUERY) String tenantName,
            @Parameter(description = "租户类型") @RequestParam(required = false)
            @BizParam(name = "tenantType", type = BizField.FieldType.STRING, description = "租户类型, 可空", position = BizParam.Position.QUERY) String tenantType,
            @Parameter(description = "状态") @RequestParam(required = false)
            @BizParam(name = "status", type = BizField.FieldType.INTEGER, description = "状态, 可空", position = BizParam.Position.QUERY) Integer status) {

        ZTenantDO tenant = new ZTenantDO();
        if (StringUtils.hasText(tenantCode)) {
            tenant.setTenantCode(tenantCode);
        }
        if (StringUtils.hasText(tenantName)) {
            tenant.setTenantName(tenantName);
        }
        if (StringUtils.hasText(tenantType)) {
            tenant.setTenantType(tenantType);
        }
        tenant.setStatus(status);

        Page<ZTenantDO> page = new Page<>(pageNum, pageSize);
        IPage<ZTenantDO> result = tenantService.pageTenant(page, tenant);
        return Result.success(result);
    }

    /**
     * 按 id 获取租户详情.
     *
     * @param id 租户主键
     * @return 租户实体, 不存在时为 null
     */
    @Operation(summary = "获取租户详情")
    @GetMapping("/get")
    @BizApi(summary = "按 id 获取租户详情", description = "按 id 获取租户详情.", tags = { "z-meta" })
    public Result<ZTenantDO> getById(@Parameter(description = "租户ID") @RequestParam @BizParam(name = "id", type = BizField.FieldType.INTEGER, description = "租户主键", position = BizParam.Position.QUERY) Long id) {
        ZTenantDO tenant = tenantService.getById(id);
        return Result.success(tenant);
    }

    /**
     * 创建一个新租户, 编码重复时返回失败.
     *
     * @param tenant 租户实体 (请求体, 内部填充 id)
     * @return 保存后的租户实体
     */
    @Operation(summary = "创建租户")
    @PostMapping
    @BizApi(summary = "创建一个新租户, 编码重复时返回失败", description = "创建一个新租户, 编码重复时返回失败.", tags = { "z-meta" })
    public Result<ZTenantDO> create(@RequestBody @BizParam(name = "tenant", type = BizField.FieldType.STRING, description = "租户实体 (请求体, 内部填充 id)", position = BizParam.Position.BODY) ZTenantDO tenant) {
        // 检查租户编码是否已存在
        ZTenantDO exist = tenantService.getByTenantCode(tenant.getTenantCode());
        if (exist != null) {
            return Result.fail("租户编码已存在");
        }
        tenantService.save(tenant);
        return Result.success(tenant);
    }

    /**
     * 按 id 更新租户.
     *
     * @param id     租户主键
     * @param tenant 待更新字段
     * @return 更新后的租户实体
     */
    @Operation(summary = "更新租户")
    @PutMapping
    @BizApi(summary = "按 id 更新租户", description = "按 id 更新租户.", tags = { "z-meta" })
    public Result<ZTenantDO> update(@Parameter(description = "租户ID") @RequestParam @BizParam(name = "id", type = BizField.FieldType.INTEGER, description = "租户主键", position = BizParam.Position.QUERY) Long id, @RequestBody @BizParam(name = "tenant", type = BizField.FieldType.STRING, description = "待更新字段", position = BizParam.Position.BODY) ZTenantDO tenant) {
        tenant.setId(id);
        tenantService.updateById(tenant);
        return Result.success(tenantService.getById(id));
    }

    /**
     * 按 id 删除租户.
     *
     * @param id 租户主键
     * @return 成功结果
     */
    @Operation(summary = "删除租户")
    @DeleteMapping
    @BizApi(summary = "按 id 删除租户", description = "按 id 删除租户.", tags = { "z-meta" })
    public Result<Object> delete(@Parameter(description = "租户ID") @RequestParam @BizParam(name = "id", type = BizField.FieldType.INTEGER, description = "租户主键", position = BizParam.Position.QUERY) Long id) {
        tenantService.removeById(id);
        return Result.success();
    }
}

