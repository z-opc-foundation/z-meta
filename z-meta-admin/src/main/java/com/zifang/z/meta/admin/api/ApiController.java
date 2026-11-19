package com.zifang.z.meta.admin.api;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zifang.util.core.meta.Result;
import com.zifang.util.core.meta.ResultCode;
import com.zifang.z.meta.api.annotation.BizApi;
import com.zifang.z.meta.api.annotation.BizField;
import com.zifang.z.meta.api.annotation.BizParam;
import com.zifang.z.meta.core.domain.entity.ZApiDO;
import com.zifang.z.meta.core.domain.service.IApiService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 接口管理 Controller.
 * <p>
 * API 基础路径: /api/interface
 * 所属模块: z-meta-admin
 * 鉴权: 由 z-ctc 统一拦截, 业务层无需关心
 *
 * <p>主要端点:
 * <ul>
 *   <li>GET    /api/interface/list       — 分页查询接口</li>
 *   <li>GET    /api/interface/get        — 按 id 获取详情</li>
 *   <li>GET    /api/interface/app/list   — 按 appId 列出接口</li>
 *   <li>POST   /api/interface            — 新建接口</li>
 *   <li>PUT    /api/interface/{id}       — 更新接口</li>
 *   <li>DELETE /api/interface/{id}       — 删除接口</li>
 * </ul>
 */
@Tag(name = "接口管理")
@RestController
@RequestMapping("/api/interface")
public class ApiController {

    @Autowired
    private IApiService apiService;

    /**
     * 分页查询接口列表, 支持 appId / path / name / method / status / deprecated 多条件过滤.
     *
     * @param pageNum    页码
     * @param pageSize   每页数量
     * @param appId      应用 ID, 可空
     * @param apiPath    接口路径, 可空 (精确匹配)
     * @param apiName    接口名称, 可空 (精确匹配)
     * @param apiMethod  请求方法, 可空
     * @param status     状态, 可空
     * @param deprecated 是否废弃, 可空
     * @return 分页结果 {@link IPage}
     */
    @Operation(summary = "分页查询接口")
    @GetMapping("/list")
    @BizApi(summary = "分页查询接口列表, 支持 appId / path / name / method / status / deprecated 多条件过滤", description = "分页查询接口列表, 支持 appId / path / name / method / status / deprecated 多条件过滤.", tags = { "z-meta" })
    public Result<IPage<ZApiDO>> list(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1")
            @BizParam(name = "pageNum", type = BizField.FieldType.INTEGER, description = "页码", position = BizParam.Position.QUERY) long pageNum,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10")
            @BizParam(name = "pageSize", type = BizField.FieldType.INTEGER, description = "每页数量", position = BizParam.Position.QUERY) long pageSize,
            @Parameter(description = "应用ID") @RequestParam(required = false)
            @BizParam(name = "appId", type = BizField.FieldType.INTEGER, description = "应用 ID, 可空", position = BizParam.Position.QUERY) Long appId,
            @Parameter(description = "接口路径") @RequestParam(required = false)
            @BizParam(name = "apiPath", type = BizField.FieldType.STRING, description = "接口路径, 可空 (精确匹配)", position = BizParam.Position.QUERY) String apiPath,
            @Parameter(description = "接口名称") @RequestParam(required = false)
            @BizParam(name = "apiName", type = BizField.FieldType.STRING, description = "接口名称, 可空 (精确匹配)", position = BizParam.Position.QUERY) String apiName,
            @Parameter(description = "请求方法") @RequestParam(required = false)
            @BizParam(name = "apiMethod", type = BizField.FieldType.STRING, description = "请求方法, 可空", position = BizParam.Position.QUERY) String apiMethod,
            @Parameter(description = "状态") @RequestParam(required = false)
            @BizParam(name = "status", type = BizField.FieldType.INTEGER, description = "状态, 可空", position = BizParam.Position.QUERY) Integer status,
            @Parameter(description = "是否废弃") @RequestParam(required = false)
            @BizParam(name = "deprecated", type = BizField.FieldType.INTEGER, description = "是否废弃, 可空", position = BizParam.Position.QUERY) Integer deprecated) {

        ZApiDO api = new ZApiDO();
        api.setAppId(appId);
        if (StringUtils.hasText(apiPath)) {
            api.setApiPath(apiPath);
        }
        if (StringUtils.hasText(apiName)) {
            api.setApiName(apiName);
        }
        if (StringUtils.hasText(apiMethod)) {
            api.setApiMethod(apiMethod);
        }
        api.setStatus(status);
        api.setDeprecated(deprecated);

        Page<ZApiDO> page = new Page<>(pageNum, pageSize);
        IPage<ZApiDO> result = apiService.pageApi(page, api);
        return Result.success(result);
    }

    /**
     * 按主键获取接口详情.
     *
     * @param id 接口主键
     * @return {@link ZApiDO}, 不存在时为 null
     */
    @Operation(summary = "获取接口详情")
    @GetMapping("/get")
    @BizApi(summary = "按主键获取接口详情", description = "按主键获取接口详情.", tags = { "z-meta" })
    public Result<ZApiDO> getById(@Parameter(description = "接口ID") @RequestParam @BizParam(name = "id", type = BizField.FieldType.INTEGER, description = "接口主键", position = BizParam.Position.QUERY) Long id) {
        ZApiDO api = apiService.getById(id);
        return Result.success(api);
    }

    /**
     * 列出指定应用下的所有接口.
     *
     * @param appId 应用主键
     * @return 该应用下的接口列表
     */
    @Operation(summary = "根据应用ID获取接口列表")
    @GetMapping("/app/list")
    @BizApi(summary = "列出指定应用下的所有接口", description = "列出指定应用下的所有接口.", tags = { "z-meta" })
    public Result<List<ZApiDO>> listByAppId(@Parameter(description = "应用ID") @RequestParam @BizParam(name = "appId", type = BizField.FieldType.INTEGER, description = "应用主键", position = BizParam.Position.QUERY) Long appId) {
        List<ZApiDO> apis = apiService.listByAppId(appId);
        return Result.success(apis);
    }

    /**
     * 创建一个新接口.
     *
     * @param api 接口实体 (请求体, 内部会填充 id)
     * @return 保存后的接口实体
     */
    @Operation(summary = "创建接口")
    @PostMapping
    @BizApi(summary = "创建一个新接口", description = "创建一个新接口.", tags = { "z-meta" })
    public Result<ZApiDO> create(@RequestBody @BizParam(name = "api", type = BizField.FieldType.STRING, description = "接口实体 (请求体, 内部会填充 id)", position = BizParam.Position.BODY) ZApiDO api) {
        apiService.save(api);
        return Result.success(api);
    }

    /**
     * 按主键更新接口.
     *
     * @param id  接口主键
     * @param api 待更新的字段
     * @return 更新后的接口实体
     */
    @Operation(summary = "更新接口")
    @PutMapping
    @BizApi(summary = "按主键更新接口", description = "按主键更新接口.", tags = { "z-meta" })
    public Result<ZApiDO> update(@Parameter(description = "接口ID") @RequestParam @BizParam(name = "id", type = BizField.FieldType.INTEGER, description = "接口主键", position = BizParam.Position.QUERY) Long id, @RequestBody @BizParam(name = "api", type = BizField.FieldType.STRING, description = "待更新的字段", position = BizParam.Position.BODY) ZApiDO api) {
        api.setId(id);
        apiService.updateById(api);
        return Result.success(apiService.getById(id));
    }

    /**
     * 按主键删除接口.
     *
     * @param id 接口主键
     * @return 成功结果
     */
    @Operation(summary = "删除接口")
    @DeleteMapping
    @BizApi(summary = "按主键删除接口", description = "按主键删除接口.", tags = { "z-meta" })
    public Result<Object> delete(@Parameter(description = "接口ID") @RequestParam @BizParam(name = "id", type = BizField.FieldType.INTEGER, description = "接口主键", position = BizParam.Position.QUERY) Long id) {
        apiService.removeById(id);
        return Result.success();
    }
}

