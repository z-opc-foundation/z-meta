package com.zifang.z.meta.core.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.zifang.z.meta.api.annotation.BizField;
import com.zifang.z.meta.api.annotation.BizModel;

import java.io.Serializable;
import java.util.Date;

/**
 * 租户实体
 */
@BizModel(name = "租户实体", module = "z-meta", description = "租户实体")
@TableName("z_tenant")
@com.fasterxml.jackson.databind.annotation.JsonNaming(com.fasterxml.jackson.databind.PropertyNamingStrategies.LowerCamelCaseStrategy.class)
public class ZTenantDO implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    @BizField(name = "主键", type = BizField.FieldType.INTEGER)
    private Long id;
    @BizField(name = "租户编码", type = BizField.FieldType.STRING)
    private String tenantCode;
    @BizField(name = "租户名称", type = BizField.FieldType.STRING)
    private String tenantName;
    @BizField(name = "租户类型（system/enterprise/personal）", type = BizField.FieldType.STRING)
    private String tenantType;
    @BizField(name = "状态（1启用/0禁用）", type = BizField.FieldType.INTEGER)
    private Integer status;
    @BizField(name = "过期时间", type = BizField.FieldType.DATETIME)
    private Date expireTime;
    @BizField(name = "Logo", type = BizField.FieldType.STRING)
    private String logoUrl;
    @BizField(name = "描述", type = BizField.FieldType.STRING)
    private String description;
    @BizField(name = "创建时间", type = BizField.FieldType.DATETIME)
    private Date gmtCreate;
    @BizField(name = "修改时间", type = BizField.FieldType.DATETIME)
    private Date gmtModified;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTenantCode() {
        return tenantCode;
    }

    public void setTenantCode(String tenantCode) {
        this.tenantCode = tenantCode;
    }

    public String getTenantName() {
        return tenantName;
    }

    public void setTenantName(String tenantName) {
        this.tenantName = tenantName;
    }

    public String getTenantType() {
        return tenantType;
    }

    public void setTenantType(String tenantType) {
        this.tenantType = tenantType;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Date getExpireTime() {
        return expireTime;
    }

    public void setExpireTime(Date expireTime) {
        this.expireTime = expireTime;
    }

    public String getLogoUrl() {
        return logoUrl;
    }

    public void setLogoUrl(String logoUrl) {
        this.logoUrl = logoUrl;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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

