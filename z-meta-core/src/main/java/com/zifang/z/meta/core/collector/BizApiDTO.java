package com.zifang.z.meta.core.collector;

import java.util.ArrayList;
import java.util.List;

/**
 * 接口端点契约元数据。
 *
 * @author zifang
 * @since 1.1.0
 */
public class BizApiDTO {

    /** 完整路径（含类级前缀）。 */
    private String path;

    /** HTTP 方法。 */
    private String httpMethod;

    /** 摘要。 */
    private String summary;

    /** 说明。 */
    private String description;

    /** 业务标签。 */
    private List<String> tags = new ArrayList<>();

    /** 响应内容类型。 */
    private String produces;

    /** 是否废弃。 */
    private boolean deprecated;

    /** 所属类全限定名。 */
    private String declaringClass;

    /** 方法名。 */
    private String methodName;

    /** 入参。 */
    private List<BizParamDTO> params = new ArrayList<>();

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getHttpMethod() {
        return httpMethod;
    }

    public void setHttpMethod(String httpMethod) {
        this.httpMethod = httpMethod;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags == null ? new ArrayList<>() : tags;
    }

    public String getProduces() {
        return produces;
    }

    public void setProduces(String produces) {
        this.produces = produces;
    }

    public boolean isDeprecated() {
        return deprecated;
    }

    public void setDeprecated(boolean deprecated) {
        this.deprecated = deprecated;
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

    public List<BizParamDTO> getParams() {
        return params;
    }

    public void setParams(List<BizParamDTO> params) {
        this.params = params == null ? new ArrayList<>() : params;
    }
}
