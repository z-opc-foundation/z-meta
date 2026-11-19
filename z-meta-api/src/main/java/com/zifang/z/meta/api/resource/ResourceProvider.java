package com.zifang.z.meta.api.resource;

import java.util.List;
import java.util.Map;

/**
 * 技术资源实例上报接口。
 *
 * <p>各业务模块实现此接口，声明自己正在用什么技术资源（数据库、缓存、队列、模型服务、
 * 存储桶……），由 z-meta 统一登记、周期采集、落库成一张 {@code z_resource} 资源台账。</p>
 *
 * <p>这是**技术资源**，不是企业资产。实物设备、域名证书、软件许可这类有归属、有生命周期、
 * 有到期日的东西归 z-asset 管；这里的「资源」只回答「这个系统在跑在什么上面」。</p>
 *
 * @author zifang
 * @since 1.1.0
 */
public interface ResourceProvider {

    /**
     * 上报方模块名。
     *
     * @return 模块名称
     */
    String getModule();

    /**
     * 资源类型（database / cache / queue / model / storage ...）。
     *
     * @return 资源类型
     */
    String getResourceType();

    /**
     * 列出本模块提供的资源实例。
     *
     * @return 资源列表
     */
    List<ResourceDTO> listResources();

    /**
     * 查单个资源实例。
     *
     * @param resourceCode 资源编码
     * @return 资源详情
     */
    ResourceDTO getResourceDetail(String resourceCode);

    /**
     * 上报通道是否可用；不可用时采集器跳过本模块（不抛异常、不拖垮整体）。
     *
     * @return 是否健康
     */
    boolean isHealthy();

    /**
     * 资源实例数据传输对象。
     */
    class ResourceDTO {

        /** 资源编码（全局唯一）。 */
        private String resourceCode;

        /** 资源名称。 */
        private String resourceName;

        /** 资源类型。 */
        private String resourceType;

        /** 所属模块。 */
        private String module;

        /** 状态。 */
        private String status;

        /** 端点信息。 */
        private String endpoint;

        /** 规格配置。 */
        private String spec;

        /** 属性信息，落库时序列化为 JSON。 */
        private Map<String, String> attributes;

        /** 租户编码。 */
        private String tenantCode;

        /** 域编码。 */
        private String domainCode;

        public ResourceDTO() {
        }

        public ResourceDTO(String resourceCode, String resourceName, String resourceType,
                           String module, String status) {
            this.resourceCode = resourceCode;
            this.resourceName = resourceName;
            this.resourceType = resourceType;
            this.module = module;
            this.status = status;
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

        public Map<String, String> getAttributes() {
            return attributes;
        }

        public void setAttributes(Map<String, String> attributes) {
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
    }
}
