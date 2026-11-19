package com.zifang.z.meta.core.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.zifang.z.meta.api.annotation.BizField;
import com.zifang.z.meta.api.annotation.BizModel;

import java.io.Serializable;
import java.util.Date;

/**
 * 私有配置实体 — 特殊 KV 配置
 */
@BizModel(name = "私有配置实体 — 特殊 KV 配置", module = "z-meta", description = "私有配置实体 — 特殊 KV 配置")
@TableName("z_private_config")
@com.fasterxml.jackson.databind.annotation.JsonNaming(com.fasterxml.jackson.databind.PropertyNamingStrategies.LowerCamelCaseStrategy.class)
public class ZPrivateConfigDO implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    @BizField(name = "主键", type = BizField.FieldType.INTEGER)
    private Long id;
    /**
     * 配置键（唯一）
     */
    @BizField(name = "配置键（唯一）", type = BizField.FieldType.STRING)
    private String configKey;
    /**
     * 配置值（可为 JSON / 文本）
     */
    @BizField(name = "配置值（可为 JSON / 文本）", type = BizField.FieldType.STRING)
    private String configValue;
    /**
     * 说明
     */
    @BizField(name = "说明", type = BizField.FieldType.STRING)
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

    public String getConfigKey() {
        return configKey;
    }

    public void setConfigKey(String configKey) {
        this.configKey = configKey;
    }

    public String getConfigValue() {
        return configValue;
    }

    public void setConfigValue(String configValue) {
        this.configValue = configValue;
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

