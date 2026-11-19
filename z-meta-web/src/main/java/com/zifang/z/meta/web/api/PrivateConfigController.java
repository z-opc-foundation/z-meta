package com.zifang.z.meta.web.api;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zifang.util.core.meta.Result;
import com.zifang.z.meta.api.annotation.BizApi;
import com.zifang.z.meta.api.annotation.BizField;
import com.zifang.z.meta.api.annotation.BizParam;
import com.zifang.z.meta.core.domain.entity.ZPrivateConfigDO;
import com.zifang.z.meta.core.domain.service.IPrivateConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

/**
 * 私有配置 Controller — 特殊 KV 配置管理.
 * <p>
 * API 基础路径: /api/private-config
 * 所属模块: z-meta-admin
 * 鉴权: 由 z-ctc 统一拦截
 *
 * <p>主要端点:
 * <ul>
 *   <li>GET    /api/private-config/list        — 分页查询 (configKey/description 模糊)</li>
 *   <li>GET    /api/private-config/get         — 按 id 获取配置详情</li>
 *   <li>GET    /api/private-config/get-by-key  — 按 configKey 获取配置值</li>
 *   <li>POST   /api/private-config             — 按 configKey 幂等保存或更新</li>
 *   <li>PUT    /api/private-config/{id}        — 按 id 更新配置</li>
 *   <li>DELETE /api/private-config/{id}        — 按 id 删除配置</li>
 * </ul>
 */
@Tag(name = "私有配置")
@RestController
@RequestMapping("/api/private-config")
public class PrivateConfigController {

    @Autowired
    private IPrivateConfigService privateConfigService;

    /**
     * 分页查询私有配置, 支持 configKey / description 模糊匹配, 按修改时间倒序.
     *
     * @param pageNum     页码
     * @param pageSize    每页数量
     * @param configKey   配置键 (模糊), 可空
     * @param description 说明 (模糊), 可空
     * @return 分页结果
     */
    @Operation(summary = "分页查询私有配置")
    @GetMapping("/list")
    @BizApi(summary = "分页查询私有配置, 支持 configKey / description 模糊匹配, 按修改时间倒序", description = "分页查询私有配置, 支持 configKey / description 模糊匹配, 按修改时间倒序.", tags = { "z-meta" })
    public Result<IPage<ZPrivateConfigDO>> list(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1")
            @BizParam(name = "pageNum", type = BizField.FieldType.INTEGER, description = "页码", position = BizParam.Position.QUERY) long pageNum,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "20")
            @BizParam(name = "pageSize", type = BizField.FieldType.INTEGER, description = "每页数量", position = BizParam.Position.QUERY) long pageSize,
            @Parameter(description = "配置键（模糊）") @RequestParam(required = false)
            @BizParam(name = "configKey", type = BizField.FieldType.STRING, description = "配置键 (模糊), 可空", position = BizParam.Position.QUERY) String configKey,
            @Parameter(description = "说明（模糊）") @RequestParam(required = false)
            @BizParam(name = "description", type = BizField.FieldType.STRING, description = "说明 (模糊), 可空", position = BizParam.Position.QUERY) String description) {

        LambdaQueryWrapper<ZPrivateConfigDO> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(configKey)) {
            wrapper.like(ZPrivateConfigDO::getConfigKey, configKey);
        }
        if (StringUtils.hasText(description)) {
            wrapper.like(ZPrivateConfigDO::getDescription, description);
        }
        wrapper.orderByDesc(ZPrivateConfigDO::getGmtModified);

        Page<ZPrivateConfigDO> page = new Page<>(pageNum, pageSize);
        IPage<ZPrivateConfigDO> result = privateConfigService.page(page, wrapper);
        return Result.success(result);
    }

    /**
     * 按主键获取配置详情.
     *
     * @param id 配置主键
     * @return 配置实体, 不存在时为 null
     */
    @Operation(summary = "根据 ID 获取配置详情")
    @GetMapping("/get")
    @BizApi(summary = "按主键获取配置详情", description = "按主键获取配置详情.", tags = { "z-meta" })
    public Result<ZPrivateConfigDO> getById(@Parameter(description = "配置ID") @RequestParam @BizParam(name = "id", type = BizField.FieldType.INTEGER, description = "配置主键", position = BizParam.Position.QUERY) Long id) {
        ZPrivateConfigDO config = privateConfigService.getById(id);
        return Result.success(config);
    }

    /**
     * 按 configKey 业务键获取配置值, 供业务运行时按名称读取配置.
     *
     * @param configKey 配置键
     * @return 配置实体, 不存在时为 null
     */
    @Operation(summary = "根据配置键获取配置值")
    @GetMapping("/get-by-key")
    @BizApi(summary = "按 configKey 业务键获取配置值, 供业务运行时按名称读取配置", description = "按 configKey 业务键获取配置值, 供业务运行时按名称读取配置.", tags = { "z-meta" })
    public Result<ZPrivateConfigDO> getByKey(@Parameter(description = "配置键") @RequestParam @BizParam(name = "configKey", type = BizField.FieldType.STRING, description = "配置键", position = BizParam.Position.QUERY) String configKey) {
        ZPrivateConfigDO config = privateConfigService.getByKey(configKey);
        return Result.success(config);
    }

    /**
     * 按 configKey 幂等保存或更新配置: 键已存在则更新 value/description, 否则新建.
     *
     * @param config 配置实体 (请求体, 内部填充 id), configKey 必填
     * @return 保存/更新后的配置实体
     */
    @Operation(summary = "创建或更新配置（按 configKey 幂等）")
    @PostMapping
    @BizApi(summary = "按 configKey 幂等保存或更新配置: 键已存在则更新 value/description, 否则新建", description = "按 configKey 幂等保存或更新配置: 键已存在则更新 value/description, 否则新建.", tags = { "z-meta" })
    public Result<ZPrivateConfigDO> saveOrUpdate(@RequestBody @BizParam(name = "config", type = BizField.FieldType.STRING, description = "配置实体 (请求体, 内部填充 id), configKey 必填", position = BizParam.Position.BODY) ZPrivateConfigDO config) {
        if (!StringUtils.hasText(config.getConfigKey())) {
            return Result.fail("configKey 不能为空");
        }
        // 幂等：若 configKey 已存在则更新，否则新建
        ZPrivateConfigDO exist = privateConfigService.getByKey(config.getConfigKey());
        if (exist != null) {
            exist.setConfigValue(config.getConfigValue());
            if (StringUtils.hasText(config.getDescription())) {
                exist.setDescription(config.getDescription());
            }
            privateConfigService.updateById(exist);
            return Result.success(privateConfigService.getById(exist.getId()));
        } else {
            privateConfigService.save(config);
            return Result.success(config);
        }
    }

    /**
     * 按主键更新配置.
     *
     * @param id     配置主键
     * @param config 待更新字段
     * @return 更新后的配置实体
     */
    @Operation(summary = "更新配置")
    @PutMapping
    @BizApi(summary = "按主键更新配置", description = "按主键更新配置.", tags = { "z-meta" })
    public Result<ZPrivateConfigDO> update(
            @Parameter(description = "配置ID") @RequestParam @BizParam(name = "id", type = BizField.FieldType.INTEGER, description = "配置主键", position = BizParam.Position.QUERY) Long id,
            @RequestBody @BizParam(name = "config", type = BizField.FieldType.STRING, description = "待更新字段", position = BizParam.Position.BODY) ZPrivateConfigDO config) {
        config.setId(id);
        privateConfigService.updateById(config);
        return Result.success(privateConfigService.getById(id));
    }

    /**
     * 按主键删除配置.
     *
     * @param id 配置主键
     * @return 成功结果
     */
    @Operation(summary = "删除配置")
    @DeleteMapping
    @BizApi(summary = "按主键删除配置", description = "按主键删除配置.", tags = { "z-meta" })
    public Result<Object> delete(@Parameter(description = "配置ID") @RequestParam @BizParam(name = "id", type = BizField.FieldType.INTEGER, description = "配置主键", position = BizParam.Position.QUERY) Long id) {
        privateConfigService.removeById(id);
        return Result.success();
    }
}



