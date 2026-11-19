package com.zifang.z.meta.core.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.zifang.z.meta.api.annotation.BizField;
import com.zifang.z.meta.api.annotation.BizModel;

import java.time.LocalDateTime;
import java.util.Date;

/**
 * 技术资源实例实体（表 {@code z_resource}）。
 *
 * <p>由各模块的 {@code ResourceProvider} 主动上报，统一落成一张资源台账。</p>
 *
 * @author zifang
 * @since 1.1.0
 */
@TableName("z_resource")
@BizModel(name = "技术资源实例", module = "meta", description = "各模块主动上报的技术资源台账：数据库 / 缓存 / 队列 / 模型服务 / 存储桶")
public class ZResourceDO {

    /** 主键。 */
    @TableId(type = IdType.AUTO)
    @BizField(name = "主键", type = BizField.FieldType.INTEGER)
    private Long id;

    /** 资源编码（全局唯一）。 */
    @BizField(name = "资源编码", type = BizField.FieldType.STRING)
    private String resourceCode;

    /** 资源名称。 */
    @BizField(name = "资源名称", type = BizField.FieldType.STRING)
    private String resourceName;

    /** 资源类型。 */
    @BizField(name = "资源类型", type = BizField.FieldType.ENUM, dictCode = "resource_type")
    private String resourceType;

    /** 所属模块。 */
    @BizField(name = "所属模块", type = BizField.FieldType.STRING)
    private String module;

    /** 状态。 */
    @BizField(name = "状态", type = BizField.FieldType.STRING)
    private String status;

    /** 端点信息。 */
    @BizField(name = "端点", type = BizField.FieldType.STRING)
    private String endpoint;

    /** 规格配置。 */
    @BizField(name = "规格", type = BizField.FieldType.STRING)
    private String spec;

    /** 属性信息（JSON）。 */
    @BizField(name = "属性", type = BizField.FieldType.JSON)
    private String attributes;

    /** 租户编码。 */
    @BizField(name = "租户编码", type = BizField.FieldType.STRING)
    private String tenantCode;

    /** 域编码。 */
    @BizField(name = "域编码", type = BizField.FieldType.STRING)
    private String domainCode;

    /** 采集时间。 */
    @BizField(name = "采集时间", type = BizField.FieldType.DATETIME)
    private LocalDateTime collectedAt;

    /** 过期时间。 */
    @BizField(name = "过期时间", type = BizField.FieldType.DATETIME)
    private LocalDateTime expireAt;

    /** 创建时间。 */
    @BizField(name = "创建时间", type = BizField.FieldType.DATETIME)
    private Date gmtCreate;

    /** 更新时间。 */
    @BizField(name = "更新时间", type = BizField.FieldType.DATETIME)
    private Date gmtModified;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getResourceCode() {
        return resourceCode;
    }

    public void setResourceCode(String resourceCode) {
        this.resourceCode = resourceCode;
    }

    public String getResourceName() {
        return resourceName;
    }

    public void setResourceName(String resourceName) {
        this.resourceName = resourceName;
    }

    public String getResourceType() {
        return resourceType;
    }

    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }

    public String getModule() {
        return module;
    }

    public void setModule(String module) {
        this.module = module;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public String getSpec() {
        return spec;
    }

    public void setSpec(String spec) {
        this.spec = spec;
    }

    public String getAttributes() {
        return attributes;
    }

    public void setAttributes(String attributes) {
        this.attributes = attributes;
    }

    public String getTenantCode() {
        return tenantCode;
    }

    public void setTenantCode(String tenantCode) {
        this.tenantCode = tenantCode;
    }

    public String getDomainCode() {
        return domainCode;
    }

    public void setDomainCode(String domainCode) {
        this.domainCode = domainCode;
    }

    public LocalDateTime getCollectedAt() {
        return collectedAt;
    }

    public void setCollectedAt(LocalDateTime collectedAt) {
        this.collectedAt = collectedAt;
    }

    public LocalDateTime getExpireAt() {
        return expireAt;
    }

    public void setExpireAt(LocalDateTime expireAt) {
        this.expireAt = expireAt;
    }

    public Date getGmtCreate() {
        return gmtCreate;
    }

    public void setGmtCreate(Date gmtCreate) {
        this.gmtCreate = gmtCreate;
    }

    public Date getGmtModified() {
        return gmtModified;
    }

    public void setGmtModified(Date gmtModified) {
        this.gmtModified = gmtModified;
    }
}



