package com.zifang.z.meta.core.collector;

import java.util.ArrayList;
import java.util.List;

/**
 * 业务方法元数据。
 *
 * @author zifang
 * @since 1.1.0
 */
public class BizMethodDTO {

    /** 方法唯一编码：类名#方法名。 */
    private String code;

    /** 所在类全限定名。 */
    private String declaringClass;

    /** 方法名。 */
    private String methodName;

    /** 业务名。 */
    private String name;

    /** 所属模块。 */
    private String module;

    /** 说明。 */
    private String description;

    /** 操作类型（QUERY/CREATE/UPDATE/DELETE/ACTION/OTHER）。 */
    private String operation;

    /** 是否幂等。 */
    private boolean idempotent;

    /** 入参。 */
    private List<BizParamDTO> params = new ArrayList<>();

    /** 该方法的因果声明。 */
    private List<BizCauseDTO> causes = new ArrayList<>();

    /** 该方法对外的接口契约，非空表示它是端点。 */
    private BizApiDTO api;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDeclaringClass() {
        return declaringClass;
    }

    public void setDeclaringClass(String declaringClass) {
        this.declaringClass = declaringClass;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getModule() {
        return module;
    }

    public void setModule(String module) {
        this.module = module;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public boolean isIdempotent() {
        return idempotent;
    }

    public void setIdempotent(boolean idempotent) {
        this.idempotent = idempotent;
    }

    public List<BizParamDTO> getParams() {
        return params;
    }

    public void setParams(List<BizParamDTO> params) {
        this.params = params == null ? new ArrayList<>() : params;
    }

    public List<BizCauseDTO> getCauses() {
        return causes;
    }

    public void setCauses(List<BizCauseDTO> causes) {
        this.causes = causes == null ? new ArrayList<>() : causes;
    }

    public BizApiDTO getApi() {
        return api;
    }

    public void setApi(BizApiDTO api) {
        this.api = api;
    }
}
