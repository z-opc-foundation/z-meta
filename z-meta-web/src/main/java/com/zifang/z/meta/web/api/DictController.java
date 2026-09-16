package com.zifang.z.meta.web.api;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zifang.util.core.meta.Result;
import com.zifang.z.meta.core.domain.entity.ZDictItemDO;
import com.zifang.z.meta.core.domain.entity.ZDictTypeDO;
import com.zifang.z.meta.core.domain.service.IDictItemService;
import com.zifang.z.meta.core.domain.service.IDictService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 字典管理 Controller.
 * <p>
 * API 基础路径: /api/dict
 * 所属模块: z-meta-admin
 * 鉴权: 由 z-ctc 统一拦截
 *
 * <p>提供两类资源:
 * <ul>
 *   <li>字典类型 ({@code /type/**}) — 字典的元信息 (编码/名称/状态 等)</li>
 *   <li>字典项 ({@code /item/**}, {@code /items/get}) — 字典下的实际键值对</li>
 * </ul>
 */
@Tag(name = "字典管理")
@RestController
@RequestMapping("/api/dict")
public class DictController {

    @Autowired
    private IDictService dictService;

    @Autowired
    private IDictItemService dictItemService;

    // ==================== 字典类型 ====================

    /**
     * 分页查询字典类型, 支持 tenantId / dictCode / dictName / status 过滤.
     *
     * @param pageNum  页码
     * @param pageSize 每页数量
     * @param tenantId 租户 ID, 可空
     * @param dictCode 字典编码, 可空
     * @param dictName 字典名称, 可空
     * @param status   状态, 可空
     * @return 分页结果
     */
    @Operation(summary = "分页查询字典类型")
    @GetMapping("/type/list")
    public Result<IPage<ZDictTypeDO>> listDictType(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") long pageNum,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") long pageSize,
            @Parameter(description = "租户ID") @RequestParam(required = false) Long tenantId,
            @Parameter(description = "字典编码") @RequestParam(required = false) String dictCode,
            @Parameter(description = "字典名称") @RequestParam(required = false) String dictName,
            @Parameter(description = "状态") @RequestParam(required = false) Integer status) {

        ZDictTypeDO dictType = new ZDictTypeDO();
        dictType.setTenantId(tenantId);
        if (StringUtils.hasText(dictCode)) {
            dictType.setDictCode(dictCode);
        }
        if (StringUtils.hasText(dictName)) {
            dictType.setDictName(dictName);
        }
        dictType.setStatus(status);

        Page<ZDictTypeDO> page = new Page<>(pageNum, pageSize);
        IPage<ZDictTypeDO> result = dictService.pageDictType(page, dictType);
        return Result.success(result);
    }

    /**
     * 按 id 获取字典类型详情.
     *
     * @param id 字典类型主键
     * @return 字典类型实体, 不存在时为 null
     */
    @Operation(summary = "获取字典类型详情")
    @GetMapping("/type/get")
    public Result<ZDictTypeDO> getDictTypeById(@Parameter(description = "字典类型ID") @RequestParam Long id) {
        ZDictTypeDO dictType = dictService.getById(id);
        return Result.success(dictType);
    }

    /**
     * 创建字典类型, 同租户下编码重复时返回失败.
     *
     * @param dictType 字典类型实体 (请求体, 内部填充 id)
     * @return 保存后的字典类型
     */
    @Operation(summary = "创建字典类型")
    @PostMapping("/type")
    public Result<ZDictTypeDO> createDictType(@RequestBody ZDictTypeDO dictType) {
        // 检查字典编码是否已存在
        ZDictTypeDO exist = dictService.getByDictCode(dictType.getTenantId(), dictType.getDictCode());
        if (exist != null) {
            return Result.fail("字典编码已存在");
        }
        dictService.save(dictType);
        return Result.success(dictType);
    }

    /**
     * 按 id 更新字典类型.
     *
     * @param id       字典类型主键
     * @param dictType 待更新字段
     * @return 更新后的字典类型
     */
    @Operation(summary = "更新字典类型")
    @PutMapping("/type")
    public Result<ZDictTypeDO> updateDictType(@Parameter(description = "字典类型ID") @RequestParam Long id, @RequestBody ZDictTypeDO dictType) {
        dictType.setId(id);
        dictService.updateById(dictType);
        return Result.success(dictService.getById(id));
    }

    /**
     * 按 id 删除字典类型.
     *
     * @param id 字典类型主键
     * @return 成功结果
     */
    @Operation(summary = "删除字典类型")
    @DeleteMapping("/type")
    public Result<Object> deleteDictType(@Parameter(description = "字典类型ID") @RequestParam Long id) {
        dictService.removeById(id);
        return Result.success();
    }

    // ==================== 字典项 ====================

    /**
     * 按字典类型 id 获取其下所有字典项.
     *
     * @param dictId 字典类型主键
     * @return 字典项列表
     */
    @Operation(summary = "根据字典ID获取字典项列表")
    @GetMapping("/item/list")
    public Result<List<ZDictItemDO>> listDictItem(@Parameter(description = "字典类型ID") @RequestParam Long dictId) {
        List<ZDictItemDO> items = dictService.listByDictId(dictId);
        return Result.success(items);
    }

    /**
     * 按字典编码 (业务编码) 获取其下所有字典项, 供业务运行时按名称查字典.
     *
     * @param dictCode 字典编码
     * @return 字典项列表
     */
    @Operation(summary = "根据字典编码获取字典项列表")
    @GetMapping("/items/get")
    public Result<List<ZDictItemDO>> listDictItemByCode(@Parameter(description = "字典编码") @RequestParam String dictCode) {
        List<ZDictItemDO> items = dictService.listByDictCode(dictCode);
        return Result.success(items);
    }

    /**
     * 创建一个新字典项.
     *
     * @param dictItem 字典项实体 (请求体, 内部填充 id)
     * @return 保存后的字典项
     */
    @Operation(summary = "创建字典项")
    @PostMapping("/item")
    public Result<ZDictItemDO> createDictItem(@RequestBody ZDictItemDO dictItem) {
        dictItemService.save(dictItem);
        return Result.success(dictItem);
    }

    /**
     * 按 id 更新字典项.
     *
     * @param id       字典项主键
     * @param dictItem 待更新字段
     * @return 更新后的字典项
     */
    @Operation(summary = "更新字典项")
    @PutMapping("/item")
    public Result<ZDictItemDO> updateDictItem(@Parameter(description = "字典项ID") @RequestParam Long id, @RequestBody ZDictItemDO dictItem) {
        dictItem.setId(id);
        dictItemService.updateById(dictItem);
        return Result.success(dictItem);
    }

    /**
     * 按 id 删除字典项.
     *
     * @param id 字典项主键
     * @return 成功结果
     */
    @Operation(summary = "删除字典项")
    @DeleteMapping("/item")
    public Result<Object> deleteDictItem(@Parameter(description = "字典项ID") @RequestParam Long id) {
        dictItemService.removeById(id);
        return Result.success();
    }
}