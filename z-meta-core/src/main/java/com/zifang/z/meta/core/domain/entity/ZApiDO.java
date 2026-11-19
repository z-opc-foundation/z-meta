package com.zifang.z.meta.core.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.zifang.z.meta.api.annotation.BizField;
import com.zifang.z.meta.api.annotation.BizModel;

import java.io.Serializable;
import java.util.Date;

/**
 * 接口实体
 */
@BizModel(name = "接口实体", module = "z-meta", description = "接口实体")
@TableName("z_api")
@com.fasterxml.jackson.databind.annotation.JsonNaming(com.fasterxml.jackson.databind.PropertyNamingStrategies.LowerCamelCaseStrategy.class)
public class ZApiDO implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    @BizField(name = "主键", type = BizField.FieldType.INTEGER)
    private Long id;
    @BizField(name = "应用ID", type = BizField.FieldType.INTEGER)
    private Long appId;
    @BizField(name = "接口路径", type = BizField.FieldType.STRING)
    private String apiPath;
    @BizField(name = "请求方法", type = BizField.FieldType.STRING)
    private String apiMethod;
    @BizField(name = "接口名称", type = BizField.FieldType.STRING)
    private String apiName;
    @BizField(name = "接口版本", type = BizField.FieldType.STRING)
    private String apiVersion;
    @BizField(name = "描述", type = BizField.FieldType.STRING)
    private String description;
    @BizField(name = "请求参数JSON", type = BizField.FieldType.STRING)
    private String requestParams;
    @BizField(name = "响应参数JSON", type = BizField.FieldType.STRING)
    private String responseParams;
    @BizField(name = "认证类型", type = BizField.FieldType.STRING)
    private String authType;
    @BizField(name = "状态", type = BizField.FieldType.INTEGER)
    private Integer status;
    @BizField(name = "是否废弃", type = BizField.FieldType.INTEGER)
    private Integer deprecated;
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

    public Long getAppId() {
        return appId;
    }

    public void setAppId(Long appId) {
        this.appId = appId;
    }

    public String getApiPath() {
        return apiPath;
    }

    public void setApiPath(String apiPath) {
        this.apiPath = apiPath;
    }

    public String getApiMethod() {
        return apiMethod;
    }

    public void setApiMethod(String apiMethod) {
        this.apiMethod = apiMethod;
    }

    public String getApiName() {
        return apiName;
    }

    public void setApiName(String apiName) {
        this.apiName = apiName;
    }

    public String getApiVersion() {
        return apiVersion;
    }

    public void setApiVersion(String apiVersion) {
        this.apiVersion = apiVersion;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getRequestParams() {
        return requestParams;
    }

    public void setRequestParams(String requestParams) {
        this.requestParams = requestParams;
    }

    public String getResponseParams() {
        return responseParams;
    }

    public void setResponseParams(String responseParams) {
        this.responseParams = responseParams;
    }

    public String getAuthType() {
        return authType;
    }

    public void setAuthType(String authType) {
        this.authType = authType;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getDeprecated() {
        return deprecated;
    }

    public void setDeprecated(Integer deprecated) {
        this.deprecated = deprecated;
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

