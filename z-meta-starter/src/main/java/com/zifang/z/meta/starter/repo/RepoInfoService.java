package com.zifang.z.meta.starter.repo;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 本仓身份服务 —— 组装「我是谁」这份答卷。
 *
 * <p>坐标与版本取自**运行中的 classpath**：jar 里有 {@code META-INF/maven/<g>/<a>/pom.properties}
 * 与 manifest，Spring Boot fat jar 也有（外层 jar 的 pom.properties 记的是宿主坐标）。
 * 读不到就退到属性与 {@code Package.getImplementationVersion()}，绝不编造。</p>
 *
 * @author zifang
 * @since 1.0.3
 */
public class RepoInfoService {

    /** 自述结果。 */
    private volatile RepoSelfDescriptor descriptor;

    /** 采集器。 */
    private final RepoSelfCollector collector;

    /** Spring 上下文。 */
    private final ApplicationContext context;

    /** 启动时刻。 */
    private final LocalDateTime startedAt = LocalDateTime.now();

    /** 显式配置的仓库名（可选）。 */
    @Value("${z.repo.name:}")
    private String configuredRepoName;

    public RepoInfoService(ApplicationContext context, RepoSelfCollector collector) {
        this.context = context;
        this.collector = collector;
    }

    /**
     * 自述结果（懒加载：路由要等容器刷新完才有）。
     *
     * @return 自述结果
     */
    public RepoSelfDescriptor descriptor() {
        RepoSelfDescriptor current = descriptor;
        if (current == null) {
            synchronized (this) {
                current = descriptor;
                if (current == null) {
                    current = collector.collect(context);
                    descriptor = current;
                }
            }
        }
        return current;
    }

    /**
     * 本仓身份与运行时信息。
     *
     * @return 身份信息
     */
    public Map<String, Object> identity() {
        Map<String, Object> info = new LinkedHashMap<>();
        info.put("repo", repoName());
        info.put("artifactId", artifactId());
        info.put("groupId", groupId());
        info.put("version", version());
        info.put("gitCommit", gitCommit());
        info.put("gitBranch", gitBranch());
        info.put("application", applicationName());
        info.put("port", port());
        info.put("contextPath", contextPath());
        info.put("profiles", activeProfiles());
        info.put("startedAt", startedAt.toString());
        info.put("uptimeSeconds", Duration.between(startedAt, LocalDateTime.now()).getSeconds());
        info.put("javaVersion", System.getProperty("java.version"));
        info.put("javaVendor", System.getProperty("java.vendor"));
        info.put("os", System.getProperty("os.name") + " " + System.getProperty("os.arch"));
        info.put("hostname", hostname());
        info.put("pid", ProcessHandle.current().pid());
        info.put("endpointCount", descriptor().getEndpoints().size());
        info.put("modelCount", descriptor().getModels().size());
        info.put("methodCount", descriptor().getMethods().size());
        return info;
    }

    /** 仓库名：显式配置优先，其次 spring.application.name。 */
    private String repoName() {
        if (configuredRepoName != null && !configuredRepoName.isEmpty()) return configuredRepoName;
        String app = applicationName();
        if (app != null && !app.isEmpty()) return app;
        return artifactId();
    }

    /** 应用名。 */
    private String applicationName() {
        return environment().getProperty("spring.application.name");
    }

    /** 端口。 */
    private String port() {
        return environment().getProperty("server.port");
    }

    /** 上下文路径。 */
    private String contextPath() {
        return environment().getProperty("server.servlet.context-path", "");
    }

    /** 激活的 profile。 */
    private List<String> activeProfiles() {
        List<String> profiles = new ArrayList<>();
        for (String p : environment().getActiveProfiles()) profiles.add(p);
        return profiles;
    }

    /** 环境。 */
    private Environment environment() {
        return context.getEnvironment();
    }

    /**
     * 坐标：读 classpath 上 jar 的 pom.properties（META-INF/maven 下按 group/artifact 落盘）。
     * 普通classpath（exploded 目录 / 扁平 jar）直接读；Spring Boot nested jar 里目录条目不产生
     * classloader URL，退到自己字节码所在的 jar 文件里翻。
     *
     * @return groupId
     */
    private String groupId() {
        return mavenProperty("groupId");
    }

    /**
     * 构件名。
     *
     * @return artifactId
     */
    private String artifactId() {
        return mavenProperty("artifactId");
    }

    /**
     * 版本：manifest 的 Implementation-Version 优先，其次 pom.properties。
     *
     * @return 版本
     */
    private String version() {
        String v = RepoInfoController.class.getPackage().getImplementationVersion();
        if (v != null && !v.isEmpty()) return v;
        return mavenProperty("version");
    }

    /** 从 classpath 的 pom.properties 里取一项。 */
    private String mavenProperty(String key) {
        String v = mavenPropertyFromClasspath(key);
        if (v != null) return v;
        return mavenPropertyFromOwnJar(key);
    }

    private String mavenPropertyFromClasspath(String key) {
        try {
            java.util.Enumeration<java.net.URL> urls = context.getClassLoader()
                    .getResources("META-INF/maven/");
            while (urls.hasMoreElements()) {
                java.net.URL url = urls.nextElement();
                java.io.File dir = new java.io.File(url.toURI());
                java.io.File[] groups = dir.listFiles();
                if (groups == null) continue;
                for (java.io.File group : groups) {
                    java.io.File[] artifacts = group.listFiles();
                    if (artifacts == null) continue;
                    for (java.io.File artifact : artifacts) {
                        java.io.File props = new java.io.File(artifact, "pom.properties");
                        if (!props.isFile()) continue;
                        java.util.Properties p = new java.util.Properties();
                        try (java.io.Reader reader = new java.io.InputStreamReader(
                                new java.io.FileInputStream(props), StandardCharsets.UTF_8)) {
                            p.load(reader);
                        } catch (Exception ignored) {
                            continue;
                        }
                        if (key.equals("groupId")) return p.getProperty("groupId");
                        if (key.equals("artifactId")) {
                            String artifactId = p.getProperty("artifactId");
                            if (artifactId != null) return artifactId;
                        }
                        if (key.equals("version")) return p.getProperty("version");
                    }
                }
            }
        } catch (Exception ignored) {
            // 读不到坐标不是致命问题，返回 null 即可
        }
        return null;
    }

    /**
     * 从**本 jar 文件**的 {@code META-INF/maven} 里读宿主坐标 —— 兜 Spring Boot nested jar：
     * fat jar 里的 {@code BOOT-INF/lib/**} 不会以目录形式出现在 classloader 的
     * {@code META-INF/maven/} 枚举里；nested 形态下 CodeSource 指向 starter 自己的嵌套 jar，
     * 而**宿主**坐标在外层 fat jar 的 pom.properties 里，所以一律剥到外层文件再读。
     */
    private String mavenPropertyFromOwnJar(String key) {
        try {
            java.security.CodeSource cs = RepoInfoService.class.getProtectionDomain().getCodeSource();
            if (cs == null || cs.getLocation() == null) return null;
            String loc = cs.getLocation().toString();
            // 形态一：file:.../z-xxx.jar（普通 jar 运行）
            // 形态二：jar:nested:/app/app.jar/!BOOT-INF/lib/z-meta-starter-1.0.2.jar（fat jar 运行）
            // 形态三：jar:file:/app/app.jar!/...（老 Boot loader）
            String jarPath;
            int bang = loc.indexOf('!');
            String noScheme = loc.startsWith("jar:nested:") ? loc.substring("jar:nested:".length())
                    : loc.startsWith("jar:file:") ? loc.substring("jar:file:".length())
                    : loc.startsWith("file:") ? loc.substring("file:".length()) : null;
            if (noScheme == null) return null;
            jarPath = bang >= 0 ? noScheme.substring(0, noScheme.indexOf('!')) : noScheme;
            while (jarPath.endsWith("/")) jarPath = jarPath.substring(0, jarPath.length() - 1);
            if (!jarPath.endsWith(".jar")) return null;
            java.io.File jarFile = new java.io.File(jarPath);
            if (!jarFile.isFile()) return null;
            try (java.util.jar.JarFile jar = new java.util.jar.JarFile(jarFile)) {
                java.util.Enumeration<java.util.jar.JarEntry> entries = jar.entries();
                while (entries.hasMoreElements()) {
                    java.util.jar.JarEntry e = entries.nextElement();
                    if (isPomProperties(e.getName())) {
                        return loadPomProperties(key, jar.getInputStream(e));
                    }
                }
            }
            return null;
        } catch (Exception ignored) {
            return null;
        }
    }

    private boolean isPomProperties(String name) {
        return name.startsWith("META-INF/maven/") && name.endsWith("/pom.properties");
    }

    private String loadPomProperties(String key, java.io.InputStream in) throws java.io.IOException {
        java.util.Properties p = new java.util.Properties();
        try (java.io.Reader reader = new java.io.InputStreamReader(in, StandardCharsets.UTF_8)) {
            p.load(reader);
        }
        return p.getProperty(key);
    }

    /**
     * git commit：优先读构建期写进 classpath 的 {@code git.properties}，读不到返回 null。
     *
     * @return commit sha
     */
    private String gitCommit() {
        return gitProperty("git.commit.id");
    }

    /**
     * git 分支。
     *
     * @return 分支名
     */
    private String gitBranch() {
        return gitProperty("git.branch");
    }

    /** 读 classpath 上的 git.properties。 */
    private String gitProperty(String key) {
        try (java.io.InputStream in = context.getClassLoader().getResourceAsStream("git.properties")) {
            if (in == null) return null;
            java.util.Properties p = new java.util.Properties();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(in, StandardCharsets.UTF_8))) {
                p.load(reader);
            }
            return p.getProperty(key);
        } catch (Exception ignored) {
            return null;
        }
    }

    /** 主机名。 */
    private String hostname() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (Exception e) {
            return ManagementFactory.getRuntimeMXBean().getName();
        }
    }
}
