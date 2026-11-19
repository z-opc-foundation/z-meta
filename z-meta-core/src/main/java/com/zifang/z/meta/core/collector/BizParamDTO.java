package com.zifang.z.meta.core.collector;

/**
 * 业务参数元数据。
 *
 * @author zifang
 * @since 1.1.0
 */
public class BizParamDTO {

    /** 参数业务名。 */
    private String name;

    /** Java 参数名。 */
    private String fieldName;

    /** 业务类型。 */
    private String type;

    /** 是否必填。 */
    private boolean required;

    /** 说明。 */
    private String description;

    /** 引用模型编码。 */
    private String refModel;

    /** 引用模型所属模块。 */
    private String refModule;

    /** 字典编码。 */
    private String dictCode;

    /** 参数位置（QUERY/PATH/BODY/HEADER）。 */
    private String position;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getRefModel() {
        return refModel;
    }

    public void setRefModel(String refModel) {
        this.refModel = refModel;
    }

    public String getRefModule() {
        return refModule;
    }

    public void setRefModule(String refModule) {
        this.refModule = refModule;
    }

    public String getDictCode() {
        return dictCode;
    }

    public void setDictCode(String dictCode) {
        this.dictCode = dictCode;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }
}
