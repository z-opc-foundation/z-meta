package com.zifang.z.meta.web.init;

import com.zifang.ctc.common.spi.site.SiteInitCapability;
import com.zifang.ctc.common.spi.site.SiteInitInvokeRequest;
import com.zifang.ctc.common.spi.site.SiteInitInvokeResult;
import com.zifang.ctc.common.spi.site.SiteInitItem;
import com.zifang.ctc.common.spi.site.SiteInitManifest;
import com.zifang.ctc.common.spi.site.SiteInitProvider;
import com.zifang.z.meta.core.domain.entity.ZApplicationDO;
import com.zifang.z.meta.core.domain.entity.ZTenantDO;
import com.zifang.z.meta.core.domain.service.IApplicationService;
import com.zifang.z.meta.core.domain.service.ITenantService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 元数据中台 · 建站初始化实现 (标准协议域侧).
 *
 * <p>能力项:
 * <ul>
 *   <li>meta.ensure-tenant —— 确保站点(租户)在元数据中台的档案行存在
 *       (元数据面一切数据都挂 tenant_id, 这一行是别的域初始化的地基)</li>
 *   <li>meta.default-application —— 为站点建默认应用档案
 *       (接口清单/技术资源台账按应用归档, 站点得先有一个默认应用可挂)</li>
 * </ul>
 *
 * <p>幂等: 租户按 tenant_code 查重、应用按 (tenant_id, app_code) 查重,
 * 重复建站不会产生第二份。
 */
@Component
public class MetaSiteInitProvider implements SiteInitProvider {

    private static final Logger log = LogManager.getLogger(MetaSiteInitProvider.class);

    /** 域编码 (与注册报文一致) */
    public static final String DOMAIN_CODE = "z-meta";

    /** 确保租户档案能力编码 */
    public static final String CAP_ENSURE_TENANT = "meta.ensure-tenant";

    /** 默认应用档案能力编码 */
    public static final String CAP_DEFAULT_APPLICATION = "meta.default-application";

    /** 默认应用编码 */
    public static final String DEFAULT_APP_CODE = "default";

    /** 默认应用名称 */
    public static final String DEFAULT_APP_NAME = "默认应用";

    private final ITenantService tenantService;
    private final IApplicationService applicationService;

    public MetaSiteInitProvider(ITenantService tenantService, IApplicationService applicationService) {
        this.tenantService = tenantService;
        this.applicationService = applicationService;
    }

    @Override
    public String domainCode() {
        return DOMAIN_CODE;
    }

    @Override
    public String domainName() {
        return "元数据中台";
    }

    @Override
    public int order() {
        return 100;
    }

    @Override
    public SiteInitManifest manifest() {
        List<SiteInitCapability> capabilities = new ArrayList<>();
        capabilities.add(new SiteInitCapability(CAP_ENSURE_TENANT, "站点租户档案",
                "确保站点在元数据中台的租户档案行存在 (元数据面数据的地基)", 100));
        capabilities.add(new SiteInitCapability(CAP_DEFAULT_APPLICATION, DEFAULT_APP_NAME,
                "为站点建默认应用档案 (接口清单/技术资源台账的挂靠点)", 200));
        return new SiteInitManifest(DOMAIN_CODE, domainName(), capabilities);
    }

    @Override
    public SiteInitInvokeResult invoke(SiteInitInvokeRequest request) {
        if (request == null || request.getTenantCode() == null || request.getTenantCode().isEmpty()) {
            return SiteInitInvokeResult.fail("tenantCode 必填");
        }
        String capability = request.getCapability();
        if (capability == null || capability.isEmpty()) {
            return runAll(request);
        }
        if (CAP_ENSURE_TENANT.equals(capability)) {
            return runEnsureTenant(request);
        }
        if (CAP_DEFAULT_APPLICATION.equals(capability)) {
            return runDefaultApplication(request);
        }
        SiteInitInvokeResult skipped = SiteInitInvokeResult.ok();
        skipped.setMessage("未知能力, 跳过: " + capability);
        return skipped;
    }

    /** 未指定能力 = 全量执行: 先地基 (租户档案) 后挂靠 (默认应用) */
    private SiteInitInvokeResult runAll(SiteInitInvokeRequest request) {
        List<SiteInitItem> items = new ArrayList<>();
        SiteInitInvokeResult tenantResult = runEnsureTenant(request);
        if (!tenantResult.isSuccess()) {
            return tenantResult;
        }
        items.addAll(tenantResult.getItems());
        SiteInitInvokeResult appResult = runDefaultApplication(request);
        items.addAll(appResult.getItems());

        SiteInitInvokeResult ok = SiteInitInvokeResult.ok();
        ok.setMessage(tenantResult.getMessage() + "; " + appResult.getMessage());
        ok.setItems(items);
        return ok;
    }

    private SiteInitInvokeResult runEnsureTenant(SiteInitInvokeRequest request) {
        String tenantCode = request.getTenantCode();
        try {
            ZTenantDO exist = tenantService.getByTenantCode(tenantCode);
            if (exist != null) {
                return okWithItem(CAP_ENSURE_TENANT, "站点租户档案", "租户档案已存在, 跳过",
                        item(CAP_ENSURE_TENANT, "站点租户档案", true, "已存在", Map.of("tenantId", exist.getId())));
            }
            ZTenantDO tenant = new ZTenantDO();
            tenant.setTenantCode(tenantCode);
            tenant.setTenantName(stringParam(request, "tenantName", tenantCode));
            tenant.setTenantType("enterprise");
            tenant.setStatus(1);
            tenant.setDescription("建站初始化自动创建");
            tenantService.save(tenant);

            ZTenantDO saved = tenantService.getByTenantCode(tenantCode);
            log.info("[z-meta-site-init] 站点 {} 租户档案已创建 id={}", tenantCode, saved == null ? "?" : saved.getId());
            return okWithItem(CAP_ENSURE_TENANT, "站点租户档案", "租户档案已创建",
                    item(CAP_ENSURE_TENANT, "站点租户档案", true, "已创建",
                            saved == null ? Map.of() : Map.of("tenantId", saved.getId())));
        } catch (Exception e) {
            log.warn("[z-meta-site-init] 站点 {} 租户档案初始化失败: {}", tenantCode, e.getMessage());
            SiteInitInvokeResult fail = SiteInitInvokeResult.fail("租户档案初始化失败: " + e.getMessage());
            fail.setItems(List.of(item(CAP_ENSURE_TENANT, "站点租户档案", false, e.getMessage(), Map.of())));
            return fail;
        }
    }

    private SiteInitInvokeResult runDefaultApplication(SiteInitInvokeRequest request) {
        String tenantCode = request.getTenantCode();
        String appCode = stringParam(request, "appCode", DEFAULT_APP_CODE);
        try {
            ZTenantDO tenant = tenantService.getByTenantCode(tenantCode);
            if (tenant == null) {
                return SiteInitInvokeResult.fail("租户档案不存在, 请先执行 " + CAP_ENSURE_TENANT);
            }
            ZApplicationDO exist = applicationService.getByAppCode(tenant.getId(), appCode);
            if (exist != null) {
                return okWithItem(CAP_DEFAULT_APPLICATION, DEFAULT_APP_NAME, "默认应用已存在, 跳过",
                        item(CAP_DEFAULT_APPLICATION, DEFAULT_APP_NAME, true, "已存在", Map.of("appId", exist.getId())));
            }
            ZApplicationDO app = new ZApplicationDO();
            app.setTenantId(tenant.getId());
            app.setAppCode(appCode);
            app.setAppName(stringParam(request, "appName", DEFAULT_APP_NAME));
            app.setStatus(1);
            app.setDescription("建站初始化自动创建");
            applicationService.save(app);

            ZApplicationDO saved = applicationService.getByAppCode(tenant.getId(), appCode);
            log.info("[z-meta-site-init] 站点 {} 默认应用已创建 id={}", tenantCode, saved == null ? "?" : saved.getId());
            return okWithItem(CAP_DEFAULT_APPLICATION, DEFAULT_APP_NAME, "默认应用已创建",
                    item(CAP_DEFAULT_APPLICATION, DEFAULT_APP_NAME, true, "已创建",
                            saved == null ? Map.of() : Map.of("appId", saved.getId())));
        } catch (Exception e) {
            log.warn("[z-meta-site-init] 站点 {} 默认应用初始化失败: {}", tenantCode, e.getMessage());
            SiteInitInvokeResult fail = SiteInitInvokeResult.fail("默认应用初始化失败: " + e.getMessage());
            fail.setItems(List.of(item(CAP_DEFAULT_APPLICATION, DEFAULT_APP_NAME, false, e.getMessage(), Map.of())));
            return fail;
        }
    }

    private static String stringParam(SiteInitInvokeRequest request, String key, String fallback) {
        Map<String, Object> params = request.getParams();
        if (params == null) {
            return fallback;
        }
        Object v = params.get(key);
        return v == null || String.valueOf(v).isEmpty() ? fallback : String.valueOf(v);
    }

    private static SiteInitInvokeResult okWithItem(String code, String name, String message, SiteInitItem item) {
        SiteInitInvokeResult ok = SiteInitInvokeResult.ok();
        ok.setMessage(message);
        ok.setItems(List.of(item));
        return ok;
    }

    private static SiteInitItem item(String code, String name, boolean success, String message, Map<String, Object> data) {
        SiteInitItem i = new SiteInitItem(code, name, success, message);
        if (data != null && !data.isEmpty()) {
            i.setData(new LinkedHashMap<>(data));
        }
        return i;
    }
}
