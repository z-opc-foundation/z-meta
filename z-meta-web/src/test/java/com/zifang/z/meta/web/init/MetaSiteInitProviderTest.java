package com.zifang.z.meta.web.init;

import com.zifang.ctc.common.spi.site.SiteInitInvokeRequest;
import com.zifang.ctc.common.spi.site.SiteInitInvokeResult;
import com.zifang.ctc.common.spi.site.SiteInitManifest;
import com.zifang.z.meta.core.domain.entity.ZApplicationDO;
import com.zifang.z.meta.core.domain.entity.ZTenantDO;
import com.zifang.z.meta.core.domain.service.IApplicationService;
import com.zifang.z.meta.core.domain.service.ITenantService;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 元数据中台建站初始化测试 (内存桩, 不依赖 DB / JUnit / Spring 容器).
 *
 * <p>验: 全量执行先租户后应用、按 capability 单跑、查重幂等、缺租户时默认应用失败、
 * 未知能力跳过、缺 tenantCode 拒绝。
 */
public class MetaSiteInitProviderTest {

    private static int pass = 0;
    private static int fail = 0;

    public static void main(String[] args) {
        try {
            run();
        } catch (Exception e) {
            fail++;
            System.out.println("  ✗ 测试中断: " + e);
            e.printStackTrace(System.out);
        } finally {
            System.out.println("MetaSiteInitProviderTest: " + pass + " passed, " + fail + " failed");
            System.out.flush();
            System.exit(fail > 0 ? 1 : 0);
        }
    }

    private static void run() {
        Map<String, ZTenantDO> tenantRows = new LinkedHashMap<>();
        Map<String, ZApplicationDO> appRows = new LinkedHashMap<>();
        MetaSiteInitProvider provider = new MetaSiteInitProvider(
                stubTenants(tenantRows), stubApps(appRows));

        // ── 清单 ──
        SiteInitManifest manifest = provider.manifest();
        eq("清单域编码", "z-meta", manifest.getDomainCode());
        eq("清单能力数", 2, manifest.getCapabilities().size());
        eq("清单能力一", "meta.ensure-tenant", manifest.getCapabilities().get(0).getCode());
        eq("清单能力二", "meta.default-application", manifest.getCapabilities().get(1).getCode());
        eq("编排顺序: meta 先于 kb(200)", true, provider.order() < 200);

        // ── 全量执行 (capability 不指定) ──
        SiteInitInvokeRequest full = req("site-001", null, null);
        SiteInitInvokeResult r1 = provider.invoke(full);
        eq("全量执行成功", true, r1.isSuccess());
        eq("产出两项", 2, r1.getItems().size());
        eq("租户已落", true, tenantRows.containsKey("site-001"));
        ZTenantDO tenant = tenantRows.get("site-001");
        eq("租户类型", "enterprise", tenant.getTenantType());
        eq("租户状态启用", Integer.valueOf(1), tenant.getStatus());
        eq("应用已落", true, appRows.containsKey(tenant.getId() + "|default"));
        ZApplicationDO app = appRows.get(tenant.getId() + "|default");
        eq("应用挂 tenantId", tenant.getId(), app.getTenantId());
        eq("应用编码", "default", app.getAppCode());

        // ── 幂等: 再跑一遍不产生第二份 ──
        SiteInitInvokeResult r2 = provider.invoke(req("site-001", null, null));
        eq("重跑成功", true, r2.isSuccess());
        eq("重跑消息带跳过", true, r2.getMessage().contains("跳过"));
        eq("不重复建租户", 1, tenantRows.size());
        eq("不重复建应用", 1, appRows.size());

        // ── 只跑默认应用 (租户已在) ──
        SiteInitInvokeResult r3 = provider.invoke(req("site-001", "meta.default-application", null));
        eq("单跑默认应用成功", true, r3.isSuccess());

        // ── 缺租户时单跑默认应用 → 失败并指引 ──
        SiteInitInvokeResult r4 = provider.invoke(req("site-404", "meta.default-application", null));
        eq("缺租户失败", true, !r4.isSuccess());
        eq("失败指引先建档案", true, r4.getMessage().contains("meta.ensure-tenant"));

        // ── 未知能力跳过 ──
        SiteInitInvokeResult r5 = provider.invoke(req("site-001", "meta.nope", null));
        eq("未知能力成功返回", true, r5.isSuccess());
        eq("消息标注跳过", true, r5.getMessage().contains("跳过"));

        // ── 缺 tenantCode 拒绝 ──
        SiteInitInvokeRequest noTenant = new SiteInitInvokeRequest();
        eq("缺 tenantCode 失败", true, !provider.invoke(noTenant).isSuccess());

        // ── 参数覆盖: 自定义应用编码/名称 (先确保租户档案) ──
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("appCode", "portal");
        params.put("appName", "门户");
        provider.invoke(req("site-002", MetaSiteInitProvider.CAP_ENSURE_TENANT, null));
        SiteInitInvokeResult r6 = provider.invoke(req("site-002", "meta.default-application", params));
        eq("参数建租户先行", true, r6.isSuccess());
        eq("自定义应用已落", true, appRows.containsKey(tenantRows.get("site-002").getId() + "|portal"));
        eq("自定义应用名", "门户", appRows.get(tenantRows.get("site-002").getId() + "|portal").getAppName());
    }

    private static SiteInitInvokeRequest req(String tenantCode, String capability, Map<String, Object> params) {
        SiteInitInvokeRequest r = new SiteInitInvokeRequest();
        r.setTenantCode(tenantCode);
        r.setCapability(capability);
        r.setParams(params);
        return r;
    }

    /** 内存桩: 动态代理只挂协议用到的三个方法, 其余一概不可用 (IService 抽象面太宽, 手写桩追不动) */
    static ITenantService stubTenants(Map<String, ZTenantDO> byCode) {
        return (ITenantService) java.lang.reflect.Proxy.newProxyInstance(
                ITenantService.class.getClassLoader(), new Class<?>[]{ITenantService.class},
                (p, method, margs) -> {
                    switch (method.getName()) {
                        case "getByTenantCode":
                            return byCode.get((String) margs[0]);
                        case "save": {
                            ZTenantDO e = (ZTenantDO) margs[0];
                            e.setId((long) (byCode.size() + 1));
                            byCode.put(e.getTenantCode(), e);
                            return Boolean.TRUE;
                        }
                        case "toString":
                            return "StubTenantService";
                        case "hashCode":
                            return System.identityHashCode(p);
                        case "equals":
                            return p == margs[0];
                        default:
                            throw new UnsupportedOperationException(method.getName());
                    }
                });
    }

    static IApplicationService stubApps(Map<String, ZApplicationDO> byKey) {
        return (IApplicationService) java.lang.reflect.Proxy.newProxyInstance(
                IApplicationService.class.getClassLoader(), new Class<?>[]{IApplicationService.class},
                (p, method, margs) -> {
                    switch (method.getName()) {
                        case "getByAppCode":
                            return byKey.get(margs[0] + "|" + margs[1]);
                        case "save": {
                            ZApplicationDO e = (ZApplicationDO) margs[0];
                            e.setId((long) (byKey.size() + 1));
                            byKey.put(e.getTenantId() + "|" + e.getAppCode(), e);
                            return Boolean.TRUE;
                        }
                        case "toString":
                            return "StubApplicationService";
                        case "hashCode":
                            return System.identityHashCode(p);
                        case "equals":
                            return p == margs[0];
                        default:
                            throw new UnsupportedOperationException(method.getName());
                    }
                });
    }

    private static void eq(String name, Object expected, Object actual) {
        if (expected == null ? actual == null : expected.equals(actual)) {
            pass++;
        } else {
            fail++;
            System.out.println("  ✗ " + name + ": expected=" + expected + " actual=" + actual);
        }
    }
}
