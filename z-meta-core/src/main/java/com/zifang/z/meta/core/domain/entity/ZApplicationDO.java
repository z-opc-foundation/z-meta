package com.zifang.z.meta.core.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.zifang.z.meta.api.annotation.BizField;
import com.zifang.z.meta.api.annotation.BizModel;

import java.io.Serializable;
import java.util.Date;

/**
 * 应用实体
 */
@BizModel(name = "应用实体", module = "z-meta", description = "应用实体")
@TableName("z_application")
@com.fasterxml.jackson.databind.annotation.JsonNaming(com.fasterxml.jackson.databind.PropertyNamingStrategies.LowerCamelCaseStrategy.class)
public class ZApplicationDO implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    @BizField(name = "主键", type = BizField.FieldType.INTEGER)
    private Long id;
    @BizField(name = "租户ID", type = BizField.FieldType.INTEGER)
    private Long tenantId;
    @BizField(name = "应用编码", type = BizField.FieldType.STRING)
    private String appCode;
    @BizField(name = "应用名称", type = BizField.FieldType.STRING)
    private String appName;
    @BizField(name = "应用类型（web/api/mobile/miniapp）", type = BizField.FieldType.STRING)
    private String appType;
    @BizField(name = "描述", type = BizField.FieldType.STRING)
    private String description;
    @BizField(name = "基础URL", type = BizField.FieldType.STRING)
    private String baseUrl;
    @BizField(name = "状态", type = BizField.FieldType.INTEGER)
    private Integer status;
    @BizField(name = "负责人", type = BizField.FieldType.STRING)
    private String owner;
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

    public Long getTenantId() {
        return tenantId;
    }

    public void setTenantId(Long tenantId) {
        this.tenantId = tenantId;
    }

    public String getAppCode() {
        return appCode;
    }

    public void setAppCode(String appCode) {
        this.appCode = appCode;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getAppType() {
        return appType;
    }

    public void setAppType(String appType) {
        this.appType = appType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
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

