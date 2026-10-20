package com.zifang.z.meta.core;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * README 里每一条定量主张都必须能从源码重算出来 —— 广告出去的话就是承诺。
 *
 * <p>为什么要有这一层：本仓 README 把「7 个 Controller」「10 种 FieldType」「三条 module」
 * 「8 张表」「8080 / 8094」「React 19 / antd 6 / Vite 6」这些数字都写死了，
 * 而每加一个类、每改一次依赖都会漂一次。没有任何尺子去问源码「现在还是吗」，
 * 那这些数字的保质期就是上一次编辑的时刻。</p>
 *
 * <p>口径五条：</p>
 * <ol>
 *   <li><b>事实对象只装源码侧的事实</b>（{@link Facts}）：README 侧的数字一律在核对<b>当下</b>
 *       从被核对的那份 README 里<b>现解析</b>。要是把 README 的数字也塞进事实对象再自己比自己，
 *       那就是恒等式比较 —— README 改成 8081 尺子照样绿，尺子等于没有。</li>
 *   <li><b>只数 src/main</b>：测试类不是 README 说的「类」；</li>
 *   <li><b>主张抠不到就红</b>：不许「正则没匹配上就跳过」，那正是空跑；</li>
 *   <li><b>按结构树节点名 / 表格行标签定位，不按数字定位</b>：数字是结论不是锚点
 *       （短数字是长数字的子串，按数字定位会匹配到别人的行上）；</li>
 *   <li><b>名单双向比对</b>：README 多一个、少一个，都要红。</li>
 * </ol>
 *
 * <p>数值不等 = 红，且报错必须把<b>重算值</b>打进消息（红了要知道真数是多少，否则只是又一处猜）。
 * 尺自己的牙口由 {@link #rulerActuallyCondemnsWrongNumbers()} 用合成 README 证明，
 * 而「不许冤枉健康页」由 {@link #readmeClaimsMatchTheCode()} 本身证明。</p>
 */
class ReadmeClaimsTest {

    /** z-meta-web 的 Controller 源码目录（README 能力清单表里写的就是这个路径）。 */
    private static final String WEB_API_DIR = "z-meta-web/src/main/java/com/zifang/z/meta/web/api";

    /** 前端主工程（admin 镜像与 _frontend/z-meta-suit 指的是它）。 */
    private static final String FRONTEND_PKG = "_frontend/z-meta-suit/package.json";

    // ==================================================================
    // 仓根定位与文件读取
    // ==================================================================

    /** 仓根：向上找同时有 README.md 与 pom.xml 的目录（测试跑在模块目录，user.dir 不是仓根）。 */
    private static Path repoRoot() {
        Path p = Paths.get(System.getProperty("user.dir")).toAbsolutePath().normalize();
        for (int i = 0; i < 5 && p != null; i++, p = p.getParent()) {
            if (Files.isRegularFile(p.resolve("README.md")) && Files.isRegularFile(p.resolve("pom.xml"))) {
                return p;
            }
        }
        throw new IllegalStateException("定位仓根失败：user.dir=" + System.getProperty("user.dir"));
    }

    private static String read(Path f) {
        try {
            return new String(Files.readAllBytes(f), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static String norm(Path p) {
        return p.toString().replace('\\', '/');
    }

    // ==================================================================
    // 事实对象：只装源码侧的事实（README 侧的数字一律现解析）
    // ==================================================================

    /** 一次重算出的全部源码侧事实。 */
    private static final class Facts {
        /** 根 pom 项目自身的 version。 */
        final String projectVersion;
        /** 根 pom 的 parent version。 */
        final String parentVersion;
        /** 根 pom <modules> 的条数。 */
        final int moduleCount;
        /** 根 pom <modules> 的模块名（有序）。 */
        final List<String> moduleNames;
        /** 根 pom <properties><compile.version>。 */
        final String compileVersion;
        /** z-meta-web 的 Controller 数（只数 src/main）。 */
        final int controllerCount;
        /** Controller 类名 → 类级 @RequestMapping 路径。 */
        final Map<String, String> controllerBasePaths;
        /** Controller 类名 → 方法级映射集合（"METHOD /全路径"）。 */
        final Map<String, Set<String>> controllerEndpoints;
        /** BizField.FieldType 的枚举值个数。 */
        final int fieldTypeCount;
        /** init.sql 建的表名（有序）。 */
        final List<String> sqlTables;
        /** 实体 @TableName 声明的表名（有序）。 */
        final List<String> entityTables;
        /** 根 Dockerfile 的 EXPOSE 端口。 */
        final int dockerExposePort;
        /** 运维脚本 build.sh 里的宿主机映射端口。 */
        final int hostPort;
        /** 运维脚本 build.sh 里的容器端口。 */
        final int containerPort;
        /** 前端镜像 Dockerfile 的 EXPOSE 端口。 */
        final int frontDockerExposePort;
        /** nginx 反代到后端的端口。 */
        final int nginxUpstreamPort;
        /** 根 Dockerfile 的 COPY 源文件路径（README 逐字引用它）。 */
        final String dockerCopySource;
        /** 仓内是否有 yml 真设了 server.port。 */
        final boolean anyYamlSetsServerPort;
        /** 前端依赖名 → 主版本号。 */
        final Map<String, Integer> frontendMajors;
        /** 前端包管理器（npm / pnpm）。 */
        final String frontendPkgManager;

        Facts(String projectVersion, String parentVersion, int moduleCount, List<String> moduleNames,
                String compileVersion, int controllerCount, Map<String, String> controllerBasePaths,
                Map<String, Set<String>> controllerEndpoints, int fieldTypeCount, List<String> sqlTables,
                List<String> entityTables, int dockerExposePort, int hostPort, int containerPort,
                int frontDockerExposePort, int nginxUpstreamPort, String dockerCopySource,
                boolean anyYamlSetsServerPort, Map<String, Integer> frontendMajors, String frontendPkgManager) {
            this.projectVersion = projectVersion;
            this.parentVersion = parentVersion;
            this.moduleCount = moduleCount;
            this.moduleNames = moduleNames;
            this.compileVersion = compileVersion;
            this.controllerCount = controllerCount;
            this.controllerBasePaths = controllerBasePaths;
            this.controllerEndpoints = controllerEndpoints;
            this.fieldTypeCount = fieldTypeCount;
            this.sqlTables = sqlTables;
            this.entityTables = entityTables;
            this.dockerExposePort = dockerExposePort;
            this.hostPort = hostPort;
            this.containerPort = containerPort;
            this.frontDockerExposePort = frontDockerExposePort;
            this.nginxUpstreamPort = nginxUpstreamPort;
            this.dockerCopySource = dockerCopySource;
            this.anyYamlSetsServerPort = anyYamlSetsServerPort;
            this.frontendMajors = frontendMajors;
            this.frontendPkgManager = frontendPkgManager;
        }
    }

    private static Facts recomputed() {
        Path root = repoRoot();
        Element project = projectElement(root.resolve("pom.xml"));

        List<String> modules = new ArrayList<>();
        Element modulesEl = childElement(project, "modules");
        if (modulesEl == null) {
            throw new IllegalStateException("根 pom 里没有 <modules>");
        }
        NodeList moduleNodes = modulesEl.getElementsByTagName("module");
        for (int i = 0; i < moduleNodes.getLength(); i++) {
            modules.add(moduleNodes.item(i).getTextContent().trim());
        }

        String compileVersion = property(project, "compile.version");

        Map<String, String> basePaths = new TreeMap<>();
        Map<String, Set<String>> endpoints = new TreeMap<>();
        for (ControllerInfo ci : controllers(root)) {
            basePaths.put(ci.name, ci.basePath);
            endpoints.put(ci.name, ci.endpoints);
        }

        return new Facts(
                projectVersion(project),
                parentVersion(project),
                modules.size(),
                modules,
                compileVersion,
                basePaths.size(),
                basePaths,
                endpoints,
                fieldTypeCount(root),
                sqlTables(root),
                entityTables(root),
                dockerExposePort(root),
                hostPort(root),
                containerPort(root),
                frontDockerExposePort(root),
                nginxUpstreamPort(root),
                dockerCopySource(root),
                anyYamlSetsServerPort(root),
                frontendMajors(root),
                frontendPkgManager(root));
    }

    // ==================================================================
    // 源码侧重算：pom
    // ==================================================================

    private static Element projectElement(Path pom) {
        try {
            DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
            f.setNamespaceAware(false);
            Document doc = f.newDocumentBuilder().parse(pom.toFile());
            return doc.getDocumentElement();
        } catch (Exception e) {
            throw new IllegalStateException("解析 pom 失败：" + pom, e);
        }
    }

    private static Element childElement(Element project, String name) {
        NodeList children = project.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node n = children.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE && name.equals(n.getNodeName())) {
                return (Element) n;
            }
        }
        return null;
    }

    /** 项目自身的 version（不是 parent 里那个 —— 正则会抓错，XML 解析不会）。 */
    private static String projectVersion(Element project) {
        Element v = childElement(project, "version");
        if (v == null) {
            throw new IllegalStateException("根 pom 里没有项目自身的 <version>");
        }
        return v.getTextContent().trim();
    }

    private static String parentVersion(Element project) {
        Element parent = childElement(project, "parent");
        if (parent == null) {
            throw new IllegalStateException("根 pom 里没有 <parent>");
        }
        Element v = childElement(parent, "version");
        if (v == null) {
            throw new IllegalStateException("根 pom 的 <parent> 里没有 <version>");
        }
        return v.getTextContent().trim();
    }

    /** 根 pom 的 <properties> 里某个属性的字面值。 */
    private static String property(Element project, String name) {
        Element props = childElement(project, "properties");
        if (props == null) {
            throw new IllegalStateException("根 pom 里没有 <properties>");
        }
        NodeList children = props.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node n = children.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE && name.equals(n.getNodeName())) {
                return n.getTextContent().trim();
            }
        }
        throw new IllegalStateException("根 pom 的 <properties> 里没有 " + name);
    }

    // ==================================================================
    // 源码侧重算：Controller
    // ==================================================================

    /** 一个 Controller 的源码侧事实。 */
    private static final class ControllerInfo {
        final String name;
        final String basePath;
        final Set<String> endpoints;

        ControllerInfo(String name, String basePath, Set<String> endpoints) {
            this.name = name;
            this.basePath = basePath;
            this.endpoints = endpoints;
        }
    }

    /** 扫描 z-meta-web/src/main 下的 Controller（只数 src/main，测试类不算）。 */
    private static List<ControllerInfo> controllers(Path root) {
        Path dir = root.resolve(WEB_API_DIR);
        if (!Files.isDirectory(dir)) {
            throw new IllegalStateException("目录不存在：" + WEB_API_DIR);
        }
        List<ControllerInfo> out = new ArrayList<>();
        try (Stream<Path> s = Files.list(dir)) {
            for (Path f : s.filter(Files::isRegularFile)
                    .filter(p -> p.toString().endsWith(".java"))
                    .sorted()
                    .collect(Collectors.toList())) {
                ControllerInfo ci = parseController(f);
                if (ci != null) {
                    out.add(ci);
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return out;
    }

    /**
     * 解析一个 Controller：类名、类级 @RequestMapping、全部方法级映射。
     *
     * <p>类级注解取「class 声明之前」的那一段，避免把方法级映射当成类级；
     * 源码先剥注释，免得 Javadoc 里出现的 {@code @RequestMapping} 字样被当成真映射。</p>
     */
    private static ControllerInfo parseController(Path f) {
        String src = stripComments(read(f));
        Matcher cm = Pattern.compile("(?m)^\\s*(?:public\\s+)?(?:abstract\\s+)?class\\s+(\\w+)\\b").matcher(src);
        if (!cm.find()) {
            return null;
        }
        String name = cm.group(1);
        String head = src.substring(0, cm.start());
        String body = src.substring(cm.start());
        if (!Pattern.compile("@(?:RestController|Controller)(?![\\w])").matcher(head).find()) {
            return null;
        }
        String base = lastMappingPath(head, "@RequestMapping");
        if (base == null) {
            throw new IllegalStateException(f + "：有 @RestController 但没有类级 @RequestMapping");
        }
        return new ControllerInfo(name, base, methodEndpoints(body, base));
    }

    /** 取一段文本里最后一个 {@code @XxxMapping} 的路径（没出现则返回 null）。 */
    private static String lastMappingPath(String text, String annotation) {
        Matcher m = Pattern.compile(Pattern.quote(annotation) + "(?![\\w])"
                + "\\s*(?:\\(\\s*(?:value\\s*=\\s*)?\"([^\"]*)\"[^)]*\\))?").matcher(text);
        String found = null;
        while (m.find()) {
            found = m.group(1) == null ? "" : m.group(1);
        }
        return found;
    }

    /** 收集方法级映射，归一成 "METHOD /全路径"（无子路径的方法落在类级路径上）。 */
    private static Set<String> methodEndpoints(String body, String base) {
        String[][] anns = {
                {"@GetMapping", "GET"}, {"@PostMapping", "POST"}, {"@PutMapping", "PUT"},
                {"@DeleteMapping", "DELETE"}, {"@PatchMapping", "PATCH"}, {"@RequestMapping", "REQUEST"},
        };
        Set<String> out = new TreeSet<>();
        for (String[] a : anns) {
            Matcher m = Pattern.compile(Pattern.quote(a[0]) + "(?![\\w])"
                    + "\\s*(?:\\(\\s*(?:value\\s*=\\s*)?\"([^\"]*)\"[^)]*\\))?").matcher(body);
            while (m.find()) {
                String p = m.group(1) == null ? "" : m.group(1);
                out.add(a[1] + " " + (p.isEmpty() ? base : base + p));
            }
        }
        return out;
    }

    // ==================================================================
    // 源码侧重算：枚举 / 建表脚本 / 实体
    // ==================================================================

    /** BizField.FieldType 的枚举值个数。 */
    private static int fieldTypeCount(Path root) {
        Path f = root.resolve("z-meta-api/src/main/java/com/zifang/z/meta/api/annotation/BizField.java");
        String src = read(f);
        Matcher m = Pattern.compile("enum\\s+FieldType\\s*\\{([^}]*)\\}").matcher(src);
        if (!m.find()) {
            throw new IllegalStateException("BizField.java 里找不到 enum FieldType");
        }
        int n = 0;
        for (String part : m.group(1).split(",")) {
            String t = part.trim();
            if (t.isEmpty() || t.startsWith("//") || t.startsWith("*")) {
                continue;
            }
            n++;
        }
        return n;
    }

    /** init.sql 里 CREATE TABLE 的表名（按出现顺序）。 */
    private static List<String> sqlTables(Path root) {
        Path f = root.resolve("_doc/002_deploy/init.sql");
        String sql = read(f);
        Matcher m = Pattern.compile("(?i)CREATE\\s+TABLE\\s+(?:IF\\s+NOT\\s+EXISTS\\s+)?`?([A-Za-z_][A-Za-z0-9_]*)`?")
                .matcher(sql);
        List<String> out = new ArrayList<>();
        while (m.find()) {
            out.add(m.group(1));
        }
        return out;
    }

    /** 实体 @TableName 声明的表名（按类名排序，保证稳定）。 */
    private static List<String> entityTables(Path root) {
        Path dir = root.resolve("z-meta-core/src/main/java/com/zifang/z/meta/core/domain/entity");
        if (!Files.isDirectory(dir)) {
            throw new IllegalStateException("目录不存在：" + dir);
        }
        List<String> out = new ArrayList<>();
        try (Stream<Path> s = Files.list(dir)) {
            for (Path f : s.filter(Files::isRegularFile)
                    .filter(p -> p.toString().endsWith(".java"))
                    .sorted()
                    .collect(Collectors.toList())) {
                Matcher m = Pattern.compile("@TableName\\(\\s*\"([^\"]+)\"\\s*\\)").matcher(read(f));
                if (m.find()) {
                    out.add(m.group(1));
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return out;
    }

    // ==================================================================
    // 源码侧重算：端口 / yml / 前端
    // ==================================================================

    /** 根 Dockerfile 的 EXPOSE 端口。 */
    private static int dockerExposePort(Path root) {
        String s = read(root.resolve("Dockerfile"));
        Matcher m = Pattern.compile("(?m)^\\s*EXPOSE\\s+(\\d+)").matcher(s);
        if (!m.find()) {
            throw new IllegalStateException("根 Dockerfile 里找不到 EXPOSE 端口");
        }
        return Integer.parseInt(m.group(1));
    }

    /** 运维脚本 build.sh 里的宿主机映射端口。 */
    private static int hostPort(Path root) {
        return scriptInt(root, "_doc/003_script/build.sh", "HOST_PORT");
    }

    /** 运维脚本 build.sh 里的容器端口。 */
    private static int containerPort(Path root) {
        return scriptInt(root, "_doc/003_script/build.sh", "CONTAINER_PORT");
    }

    /** 脚本里 `NAME=数字` 形式的端口常量。 */
    private static int scriptInt(Path root, String rel, String name) {
        String s = read(root.resolve(rel));
        Matcher m = Pattern.compile("(?m)^\\s*" + Pattern.quote(name) + "\\s*=\\s*(\\d+)").matcher(s);
        if (!m.find()) {
            throw new IllegalStateException(rel + " 里找不到 " + name);
        }
        return Integer.parseInt(m.group(1));
    }

    /** 前端镜像 Dockerfile 的 EXPOSE 端口。 */
    private static int frontDockerExposePort(Path root) {
        String s = read(root.resolve("_frontend/z-meta-suit/Dockerfile"));
        Matcher m = Pattern.compile("(?m)^\\s*EXPOSE\\s+(\\d+)").matcher(s);
        if (!m.find()) {
            throw new IllegalStateException("_frontend/z-meta-suit/Dockerfile 里找不到 EXPOSE 端口");
        }
        return Integer.parseInt(m.group(1));
    }

    /** nginx 反代到后端的端口（proxy_pass 的 host:port）。 */
    private static int nginxUpstreamPort(Path root) {
        String s = read(root.resolve("_frontend/z-meta-suit/nginx.conf"));
        Matcher m = Pattern.compile("proxy_pass\\s+https?://[^:/\\s]+:(\\d+)").matcher(s);
        if (!m.find()) {
            throw new IllegalStateException("_frontend/z-meta-suit/nginx.conf 里找不到 proxy_pass 端口");
        }
        return Integer.parseInt(m.group(1));
    }

    /** 根 Dockerfile 的 COPY 源文件路径。 */
    private static String dockerCopySource(Path root) {
        String s = read(root.resolve("Dockerfile"));
        Matcher m = Pattern.compile("(?m)^\\s*COPY\\s+(\\S+)\\s+\\S+\\s*$").matcher(s);
        if (!m.find()) {
            throw new IllegalStateException("根 Dockerfile 里找不到 COPY 源文件");
        }
        return m.group(1);
    }

    /**
     * 仓内有没有 yml 真设了 {@code server.port}。
     *
     * <p>注释里提到 server.port 不算「设置」—— z-meta-admin/application.yml 首两行就是这种注释，
     * 所以先剥注释再判；嵌套写法（{@code server:} 块里的 {@code port:}）与点写法都认。</p>
     */
    private static boolean anyYamlSetsServerPort(Path root) {
        try (Stream<Path> s = Files.walk(root)) {
            for (Path f : s.filter(Files::isRegularFile)
                    .filter(p -> p.toString().endsWith(".yml") || p.toString().endsWith(".yaml"))
                    .filter(p -> !norm(p).contains("/target/"))
                    .filter(p -> !norm(p).contains("/node_modules/"))
                    .collect(Collectors.toList())) {
                if (yamlSetsServerPort(read(f))) {
                    return true;
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return false;
    }

    /** 单个 yml 文件里有没有真的 server.port 设置（注释不算）。 */
    private static boolean yamlSetsServerPort(String yaml) {
        String[] lines = yaml.split("\r?\n", -1);
        boolean inServer = false;
        int serverIndent = -1;
        for (String raw : lines) {
            String line = raw;
            int hash = line.indexOf('#');
            if (hash >= 0) {
                line = line.substring(0, hash);
            }
            if (line.trim().isEmpty()) {
                continue;
            }
            int indent = line.length() - line.trim().length();
            String t = line.trim();
            // 点写法：server.port: 8080
            if (t.matches("^server\\s*\\.\\s*port\\s*:.*")) {
                return true;
            }
            if (t.matches("^server\\s*:.*")) {
                inServer = true;
                serverIndent = indent;
                continue;
            }
            if (inServer && indent <= serverIndent && !t.startsWith("server")) {
                inServer = false;
            }
            if (inServer && t.matches("^port\\s*:.*")) {
                return true;
            }
        }
        return false;
    }

    /** 前端 package.json 里几个关键依赖的主版本号。 */
    private static Map<String, Integer> frontendMajors(Path root) {
        String pkg = read(root.resolve(FRONTEND_PKG));
        Map<String, Integer> out = new LinkedHashMap<>();
        for (String dep : Arrays.asList("react", "antd", "react-router-dom", "vite")) {
            Matcher m = Pattern.compile("\"" + Pattern.quote(dep) + "\"\\s*:\\s*\"([^\"]+)\"").matcher(pkg);
            if (!m.find()) {
                throw new IllegalStateException(FRONTEND_PKG + " 里找不到依赖 " + dep);
            }
            out.put(dep, majorOf(m.group(1)));
        }
        return out;
    }

    /** 前端包管理器：仓内前端目录里有 pnpm 的锁文件/工作区文件就是 pnpm，否则看 npm 的 package-lock.json。 */
    private static String frontendPkgManager(Path root) {
        boolean npmLock = false;
        try (Stream<Path> s = Files.walk(root.resolve("_frontend"))) {
            for (Path f : s.filter(Files::isRegularFile).collect(Collectors.toList())) {
                String n = f.getFileName().toString();
                if ("pnpm-lock.yaml".equals(n) || "pnpm-workspace.yaml".equals(n)) {
                    return "pnpm";
                }
                if ("package-lock.json".equals(n)) {
                    npmLock = true;
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return npmLock ? "npm" : "unknown";
    }

    /** 从 "^19.2.0" / "~6.3.3" / "7.0.0" 里取主版本号。 */
    private static int majorOf(String versionRange) {
        String v = versionRange.trim();
        while (v.startsWith("^") || v.startsWith("~") || v.startsWith(">") || v.startsWith("=")) {
            v = v.substring(1).trim();
        }
        Matcher m = Pattern.compile("^(\\d+)").matcher(v);
        if (!m.find()) {
            throw new IllegalStateException("看不懂的版本号：" + versionRange);
        }
        return Integer.parseInt(m.group(1));
    }

    // ==================================================================
    // README 侧：现解析（不是事实对象的一部分）
    // ==================================================================

    /**
     * 把 README 拆成「逻辑行」。
     *
     * <p>Markdown 的列表续行（缩进 4 空格以上）会拼回上一行 —— README 里
     * init.sql 那条清单就被劈成了两行，不拼的话正则会跨行失配（尺子自己的 bug，不是 README 的问题）。
     * 代码块（```）内容不进逻辑行。</p>
     */
    private static List<String> logicalLines(String readme) {
        List<String> out = new ArrayList<>();
        StringBuilder cur = new StringBuilder();
        boolean inFence = false;
        for (String raw : readme.split("\r?\n", -1)) {
            String trimmed = raw.trim();
            if (trimmed.startsWith("```")) {
                if (cur.length() > 0) {
                    out.add(cur.toString());
                    cur.setLength(0);
                }
                inFence = !inFence;
                continue;
            }
            if (!inFence && raw.startsWith("    ") && !trimmed.isEmpty() && cur.length() > 0) {
                cur.append(' ').append(trimmed);
                continue;
            }
            if (cur.length() > 0) {
                out.add(cur.toString());
                cur.setLength(0);
            }
            cur.append(raw);
        }
        if (cur.length() > 0) {
            out.add(cur.toString());
        }
        return out;
    }

    /** 定位含唯一锚点的逻辑行；找不到或不止一处都算主张抠不到。 */
    private static String requireLine(List<String> lines, String anchor) {
        List<String> hits = new ArrayList<>();
        for (String l : lines) {
            if (l.contains(anchor)) {
                hits.add(l);
            }
        }
        if (hits.isEmpty()) {
            throw new AssertionError("README 里抠不到主张（锚点「" + anchor + "」一条都没匹配上）");
        }
        if (hits.size() > 1) {
            throw new AssertionError("README 里锚点「" + anchor + "」匹配到 " + hits.size() + " 行，锚点不唯一");
        }
        return hits.get(0);
    }

    /**
     * 定位 README 结构树里某个节点的整行。
     *
     * <p>锚点是<b>树形前缀 + 节点名</b>，不是数字：数字会漂，且短数字是长数字的子串
     * （「6 个」⊂「16 个」）⇒ 定位到别人的行上去。节点名之后必须是「/」（目录）或空白/#（文件），
     * 否则 {@code z-meta-web} 会误配 {@code z-meta-web-extra}。</p>
     */
    private static Matcher treeLine(String readme, String node) {
        return Pattern.compile("(?m)^[ \\t]*(?:├──|└──)[ \\t]*" + Pattern.quote(node)
                        + "(?:/|(?=[ \\t#]))[^\\n]*$")
                .matcher(readme);
    }

    private static String requireTreeLine(String readme, String node) {
        Matcher m = treeLine(readme, node);
        if (!m.find()) {
            throw new AssertionError("README 的结构树里找不到节点行：" + node
                    + "（结构树被改过形？还是这行被挪走了？）");
        }
        return m.group();
    }

    /** 从一行里抠第一个捕获组；抠不到就抛（主张抠不到必须红）。 */
    private static String requireGroup(String line, Pattern p, String what) {
        Matcher m = p.matcher(line);
        if (!m.find()) {
            throw new AssertionError("抠不到「" + what + "」： " + line);
        }
        return m.group(1);
    }

    /**
     * 在<b>原始</b> README 上抠第一个捕获组。
     *
     * <p>原始文本里有逻辑行取不到的东西 —— 代码块（```）里的注释行就是。
     * 那几行同样是 README 的主张，不能因为「它不是段落」就放过去。</p>
     */
    private static String requireRawGroup(String readme, Pattern p, String what) {
        Matcher m = p.matcher(readme);
        if (!m.find()) {
            throw new AssertionError("README 原文里抠不到「" + what + "」");
        }
        return m.group(1);
    }

    /** 中文数字（两 / 一~十）转阿拉伯数字。 */
    private static int chineseNumber(String s) {
        Map<String, Integer> digit = new HashMap<>();
        digit.put("零", 0);
        digit.put("一", 1);
        digit.put("两", 2);
        digit.put("二", 2);
        digit.put("三", 3);
        digit.put("四", 4);
        digit.put("五", 5);
        digit.put("六", 6);
        digit.put("七", 7);
        digit.put("八", 8);
        digit.put("九", 9);
        String t = s.trim();
        if (t.length() == 1) {
            Integer v = digit.get(t);
            if (v == null) {
                throw new AssertionError("看不懂的中文数字：" + s);
            }
            return v;
        }
        if (t.startsWith("十")) {
            int rest = t.length() == 1 ? 0 : digit.getOrDefault(t.substring(1), 0);
            return 10 + rest;
        }
        if (t.contains("十")) {
            int idx = t.indexOf("十");
            int high = digit.getOrDefault(t.substring(0, idx), 0);
            int low = t.length() == idx + 1 ? 0 : digit.getOrDefault(t.substring(idx + 1), 0);
            return high * 10 + low;
        }
        throw new AssertionError("看不懂的中文数字：" + s);
    }

    /** 抠出一段文字里反引号包着的所有名字。 */
    private static List<String> backticked(String text) {
        List<String> out = new ArrayList<>();
        Matcher m = Pattern.compile("`([^`]+)`").matcher(text);
        while (m.find()) {
            out.add(m.group(1).trim());
        }
        return out;
    }

    // ==================================================================
    // 判定
    // ==================================================================

    /**
     * README 里的每条主张逐条与源码重算值对账。
     *
     * <p>一次全报式：把所有对不上的都收集齐再抛，省得「撞停式」一次只说一句。
     * 每条都把<b>重算值</b>带进消息 —— 红了要知道真数是多少。</p>
     */
    private static void verifyClaims(String readme, Facts f) {
        List<String> bad = new ArrayList<>();
        List<String> lines = logicalLines(readme);

        // ---- 主张 1：根聚合 POM 的版本（README 里出现两处，各钉各的） ----
        try {
            String row = requireLine(lines, "**当前版本**");
            String v = requireGroup(row, Pattern.compile("\\*\\*当前版本\\*\\*\\s*\\|\\s*`(\\d+\\.\\d+\\.\\d+)`"),
                    "根 POM 版本（基本信息表）");
            if (!v.equals(f.projectVersion)) {
                bad.add("主张 1a（根 POM 版本 · 基本信息表）：README 写 " + v + "，源码重算值 " + f.projectVersion);
            }
        } catch (AssertionError e) {
            bad.add("主张 1a（根 POM 版本 · 基本信息表）：" + e.getMessage());
        }
        try {
            String line = requireTreeLine(readme, "pom.xml");
            String v = requireGroup(line, Pattern.compile("version (\\d+\\.\\d+\\.\\d+)"),
                    "根 POM 版本（结构树 pom.xml 行）");
            if (!v.equals(f.projectVersion)) {
                bad.add("主张 1b（根 POM 版本 · 结构树 pom.xml 行）：README 写 " + v + "，源码重算值 " + f.projectVersion);
            }
        } catch (AssertionError e) {
            bad.add("主张 1b（根 POM 版本 · 结构树 pom.xml 行）：" + e.getMessage());
        }

        // ---- 主张 2：父项目版本（两处） ----
        try {
            String row = requireLine(lines, "**父项目**");
            String v = requireGroup(row, Pattern.compile("\\*\\*父项目\\*\\*\\s*\\|\\s*`[^`]*z-boot-parent:(\\d+\\.\\d+\\.\\d+)`"),
                    "父项目版本（基本信息表）");
            if (!v.equals(f.parentVersion)) {
                bad.add("主张 2a（父项目版本 · 基本信息表）：README 写 " + v + "，源码重算值 " + f.parentVersion);
            }
        } catch (AssertionError e) {
            bad.add("主张 2a（父项目版本 · 基本信息表）：" + e.getMessage());
        }
        try {
            String line = requireTreeLine(readme, "pom.xml");
            String v = requireGroup(line, Pattern.compile("z-boot-parent:(\\d+\\.\\d+\\.\\d+)"),
                    "父项目版本（结构树 pom.xml 行）");
            if (!v.equals(f.parentVersion)) {
                bad.add("主张 2b（父项目版本 · 结构树 pom.xml 行）：README 写 " + v + "，源码重算值 " + f.parentVersion);
            }
        } catch (AssertionError e) {
            bad.add("主张 2b（父项目版本 · 结构树 pom.xml 行）：" + e.getMessage());
        }

        // ---- 主张 3：根 pom <modules> 的条数与名单（数量 + 名单双向） ----
        try {
            String line = requireLine(lines, "根 POM `<modules>`");
            Matcher m = Pattern.compile("只有([一二三四五六七八九十两]+)条[：:](.*)").matcher(line);
            if (!m.find()) {
                throw new AssertionError("抠不到「只有 N 条：名单」： " + line);
            }
            int readmeCount = chineseNumber(m.group(1));
            if (readmeCount != f.moduleCount) {
                bad.add("主张 3a（<modules> 条数）：README 写 " + readmeCount + " 条，源码重算值 " + f.moduleCount);
            }
            List<String> readmeNames = backticked(m.group(2));
            Set<String> missing = new TreeSet<>(f.moduleNames);
            missing.removeAll(readmeNames);
            Set<String> extra = new TreeSet<>(readmeNames);
            extra.removeAll(f.moduleNames);
            if (!missing.isEmpty()) {
                bad.add("主张 3b（<modules> 名单）：README 漏了这些模块 " + missing + "，源码重算值 " + f.moduleNames);
            }
            if (!extra.isEmpty()) {
                bad.add("主张 3b（<modules> 名单）：README 多写了这些模块 " + extra + "，源码重算值 " + f.moduleNames);
            }
        } catch (AssertionError e) {
            bad.add("主张 3（<modules> 条数与名单）：" + e.getMessage());
        }

        // ---- 主张 4：Java 版本口径 <compile.version>（两处） ----
        try {
            String row = requireLine(lines, "**运行口径**");
            String v = requireGroup(row, Pattern.compile("<compile\\.version>(\\d+)</compile\\.version>"),
                    "compile.version（基本信息表 运行口径行）");
            if (!v.equals(f.compileVersion)) {
                bad.add("主张 4a（compile.version · 基本信息表）：README 写 " + v + "，源码重算值 " + f.compileVersion);
            }
        } catch (AssertionError e) {
            bad.add("主张 4a（compile.version · 基本信息表）：" + e.getMessage());
        }
        try {
            String row = requireLine(lines, "语言 / 运行时");
            String v = requireGroup(row, Pattern.compile("compile\\.version=(\\d+)"),
                    "compile.version（技术栈 语言/运行时行）");
            if (!v.equals(f.compileVersion)) {
                bad.add("主张 4b（compile.version · 技术栈）：README 写 " + v + "，源码重算值 " + f.compileVersion);
            }
        } catch (AssertionError e) {
            bad.add("主张 4b（compile.version · 技术栈）：" + e.getMessage());
        }

        // ---- 主张 5：z-meta-web 的 Controller 数 + 名单 + 路径 + 子路径 ----
        try {
            String line = requireTreeLine(readme, "z-meta-web");
            String n = requireGroup(line, Pattern.compile("(\\d+)\\s*个 Controller"),
                    "Controller 数（结构树 z-meta-web 行）");
            if (!n.equals(String.valueOf(f.controllerCount))) {
                bad.add("主张 5a（z-meta-web Controller 数 · 结构树）：README 写 " + n
                        + " 个，源码重算值 " + f.controllerCount + " 个");
            }
        } catch (AssertionError e) {
            bad.add("主张 5a（z-meta-web Controller 数 · 结构树）：" + e.getMessage());
        }
        try {
            Map<String, String> readmeRows = apiTableRows(lines);
            Set<String> readmeNames = new TreeSet<>(readmeRows.keySet());
            Set<String> missing = new TreeSet<>(f.controllerBasePaths.keySet());
            missing.removeAll(readmeNames);
            Set<String> extra = new TreeSet<>(readmeNames);
            extra.removeAll(f.controllerBasePaths.keySet());
            if (!missing.isEmpty()) {
                bad.add("主张 5b（API 表 Controller 名单）：README 漏了这些 Controller " + missing
                        + "，源码重算值 " + f.controllerBasePaths.keySet());
            }
            if (!extra.isEmpty()) {
                bad.add("主张 5b（API 表 Controller 名单）：README 多写了这些 Controller " + extra
                        + "，源码重算值 " + f.controllerBasePaths.keySet());
            }
            for (Map.Entry<String, String> e : readmeRows.entrySet()) {
                String sourcePath = f.controllerBasePaths.get(e.getKey());
                if (sourcePath == null) {
                    continue;
                }
                if (!sourcePath.equals(e.getValue())) {
                    bad.add("主张 5c（API 表路径 · " + e.getKey() + "）：README 写 " + e.getValue()
                            + "，源码重算值 " + sourcePath);
                }
            }
        } catch (AssertionError e) {
            bad.add("主张 5b/5c（API 表 Controller 名单与路径）：" + e.getMessage());
        }
        try {
            for (Map.Entry<String, String> e : apiTableRows(lines).entrySet()) {
                Set<String> source = f.controllerEndpoints.get(e.getKey());
                if (source == null) {
                    continue;
                }
                Set<String> readmeEps = readmeEndpointsOf(e.getKey(), lines);
                Set<String> missing = new TreeSet<>(source);
                missing.removeAll(readmeEps);
                Set<String> extra = new TreeSet<>(readmeEps);
                extra.removeAll(source);
                if (!missing.isEmpty()) {
                    bad.add("主张 5d（API 表子路径 · " + e.getKey() + "）：README 漏了这些端点 " + missing
                            + "，源码重算值 " + source);
                }
                if (!extra.isEmpty()) {
                    bad.add("主张 5d（API 表子路径 · " + e.getKey() + "）：README 多写了这些端点 " + extra
                            + "，源码重算值 " + source);
                }
            }
        } catch (AssertionError e) {
            bad.add("主张 5d（API 表子路径）：" + e.getMessage());
        }

        // ---- 主张 6：@BizField 的 FieldType 枚举值个数 ----
        try {
            String row = requireLine(lines, "@BizField(name/type");
            String n = requireGroup(row, Pattern.compile("(\\d+)\\s*种 `FieldType`"),
                    "FieldType 枚举值个数（能力清单 业务模型采集行）");
            if (!n.equals(String.valueOf(f.fieldTypeCount))) {
                bad.add("主张 6（FieldType 枚举值个数）：README 写 " + n
                        + " 种，源码重算值 " + f.fieldTypeCount + " 种");
            }
        } catch (AssertionError e) {
            bad.add("主张 6（FieldType 枚举值个数）：" + e.getMessage());
        }

        // ---- 主张 7：init.sql 建表清单（名单双向 + 有无实体分开对账） ----
        try {
            String line = requireLine(lines, "_doc/002_deploy/init.sql");
            int split = line.indexOf("代码里没有对应实体的");
            if (split < 0) {
                throw new AssertionError("抠不到「以及代码里没有对应实体的 …」： " + line);
            }
            String withEntity = line.substring(0, split);
            String withoutEntity = line.substring(split);
            int head = withEntity.indexOf("实测含");
            if (head < 0) {
                throw new AssertionError("抠不到「实测含 …」： " + withEntity);
            }
            List<String> readmeWith = backticked(withEntity.substring(head + "实测含".length()));
            List<String> readmeWithout = backticked(withoutEntity);
            Set<String> readmeWithSet = new TreeSet<>(readmeWith);
            Set<String> readmeWithoutSet = new TreeSet<>(readmeWithout);
            Set<String> sourceWith = new TreeSet<>(f.entityTables);
            Set<String> sourceWithout = new TreeSet<>(f.sqlTables);
            sourceWithout.removeAll(sourceWith);
            Set<String> missing = new TreeSet<>(sourceWith);
            missing.removeAll(readmeWithSet);
            Set<String> extra = new TreeSet<>(readmeWithSet);
            extra.removeAll(sourceWith);
            if (!missing.isEmpty()) {
                bad.add("主张 7a（init.sql 有实体的表）：README 漏了 " + missing + "，源码重算值 " + sourceWith);
            }
            if (!extra.isEmpty()) {
                bad.add("主张 7a（init.sql 有实体的表）：README 多写了 " + extra + "，源码重算值 " + sourceWith);
            }
            missing = new TreeSet<>(sourceWithout);
            missing.removeAll(readmeWithoutSet);
            extra = new TreeSet<>(readmeWithoutSet);
            extra.removeAll(sourceWithout);
            if (!missing.isEmpty()) {
                bad.add("主张 7b（init.sql 没有实体的表）：README 漏了 " + missing + "，源码重算值 " + sourceWithout);
            }
            if (!extra.isEmpty()) {
                bad.add("主张 7b（init.sql 没有实体的表）：README 多写了 " + extra + "，源码重算值 " + sourceWithout);
            }
        } catch (AssertionError e) {
            bad.add("主张 7（init.sql 建表清单）：" + e.getMessage());
        }

        // ---- 主张 8：端口（每一处出现都单独钉：基本信息表 / 结构树 / 部署节 / nginx / Dockerfile 引用） ----
        try {
            String row = requireLine(lines, "**默认端口**");
            String v = requireGroup(row, Pattern.compile("\\*\\*默认端口\\*\\*\\s*\\|\\s*`(\\d+)`"),
                    "默认端口（基本信息表）");
            if (!v.equals(String.valueOf(f.dockerExposePort))) {
                bad.add("主张 8a（默认端口 · 基本信息表）：README 写 " + v
                        + "，源码重算值（根 Dockerfile EXPOSE）" + f.dockerExposePort);
            }
            if (!row.contains("没有任何 yml 设置")) {
                bad.add("主张 8a（默认端口 · 基本信息表）：README 少了「仓内没有任何 yml 设置 server.port」这句口径，"
                        + "源码重算值：仓内有 yml 设了 server.port=" + f.anyYamlSetsServerPort);
            }
            // 同一格里还点了「Dockerfile 亦 EXPOSE 8080」和「运维脚本映射宿主机 8094」—— 也要钉
            if (!row.contains("EXPOSE " + f.dockerExposePort)) {
                bad.add("主张 8a（EXPOSE 端口 · 基本信息表）：README 这一格没写「EXPOSE " + f.dockerExposePort
                        + "」，源码重算值（根 Dockerfile EXPOSE）" + f.dockerExposePort);
            }
            if (!row.contains("`" + f.hostPort + "`")) {
                bad.add("主张 8a（宿主机映射端口 · 基本信息表）：README 这一格没写宿主机端口 `" + f.hostPort
                        + "`，源码重算值（build.sh HOST_PORT）" + f.hostPort);
            }
        } catch (AssertionError e) {
            bad.add("主张 8a（默认端口 · 基本信息表）：" + e.getMessage());
        }
        try {
            String line = requireTreeLine(readme, "Dockerfile");
            String v = requireGroup(line, Pattern.compile("EXPOSE (\\d+)"),
                    "EXPOSE 端口（结构树 Dockerfile 行）");
            if (!v.equals(String.valueOf(f.dockerExposePort))) {
                bad.add("主张 8b（EXPOSE 端口 · 结构树 Dockerfile 行）：README 写 " + v
                        + "，源码重算值 " + f.dockerExposePort);
            }
        } catch (AssertionError e) {
            bad.add("主张 8b（EXPOSE 端口 · 结构树 Dockerfile 行）：" + e.getMessage());
        }
        try {
            String line = requireLine(lines, "宿主端口");
            Matcher m = Pattern.compile("宿主端口 `(\\d+)` → 容器 `(\\d+)`").matcher(line);
            if (!m.find()) {
                throw new AssertionError("抠不到「宿主端口 `N` → 容器 `M`」： " + line);
            }
            if (!m.group(1).equals(String.valueOf(f.hostPort))) {
                bad.add("主张 8c（宿主机映射端口 · 部署节）：README 写 " + m.group(1)
                        + "，源码重算值（build.sh HOST_PORT）" + f.hostPort);
            }
            if (!m.group(2).equals(String.valueOf(f.containerPort))) {
                bad.add("主张 8c（容器端口 · 部署节）：README 写 " + m.group(2)
                        + "，源码重算值（build.sh CONTAINER_PORT）" + f.containerPort);
            }
        } catch (AssertionError e) {
            bad.add("主张 8c（宿主机映射端口 · 部署节）：" + e.getMessage());
        }
        if (f.anyYamlSetsServerPort) {
            bad.add("主张 8d（仓内无 yml 设 server.port）：源码重算值 = 有 yml 设了 server.port，README 写「没有任何」");
        }
        // 部署节的 docker build 注释在 ``` 代码块里 —— 逻辑行取不到，直接在原文上抠
        try {
            String v = requireRawGroup(readme, Pattern.compile("根 Dockerfile：[^\\n]*EXPOSE (\\d+)"),
                    "EXPOSE 端口（部署节 docker build 注释）");
            if (!v.equals(String.valueOf(f.dockerExposePort))) {
                bad.add("主张 8e（EXPOSE 端口 · 部署节注释）：README 写 " + v
                        + "，源码重算值 " + f.dockerExposePort);
            }
        } catch (AssertionError e) {
            bad.add("主张 8e（EXPOSE 端口 · 部署节注释）：" + e.getMessage());
        }
        try {
            String v = requireRawGroup(readme, Pattern.compile("_frontend/Dockerfile：[^\\n]*EXPOSE (\\d+)"),
                    "EXPOSE 端口（部署节前端镜像注释）");
            if (!v.equals(String.valueOf(f.frontDockerExposePort))) {
                bad.add("主张 8f（前端镜像 EXPOSE 端口 · 部署节注释）：README 写 " + v
                        + "，源码重算值（_frontend/z-meta-suit/Dockerfile）" + f.frontDockerExposePort);
            }
        } catch (AssertionError e) {
            bad.add("主张 8f（前端镜像 EXPOSE 端口 · 部署节注释）：" + e.getMessage());
        }
        try {
            String v = requireRawGroup(readme, Pattern.compile("反代到 `https?://z-meta:(\\d+)`"),
                    "nginx 反代端口（部署节）");
            if (!v.equals(String.valueOf(f.nginxUpstreamPort))) {
                bad.add("主张 8g（nginx 反代端口 · 部署节）：README 写 " + v
                        + "，源码重算值（nginx.conf proxy_pass）" + f.nginxUpstreamPort);
            }
        } catch (AssertionError e) {
            bad.add("主张 8g（nginx 反代端口 · 部署节）：" + e.getMessage());
        }
        try {
            String line = requireLine(lines, "仍指向");
            String v = requireGroup(line, Pattern.compile("仍指向 `([^`]+)`"),
                    "Dockerfile COPY 路径（部署节陈旧项清单）");
            if (!v.equals(f.dockerCopySource)) {
                bad.add("主张 8h（Dockerfile COPY 路径 · 部署节陈旧项清单）：README 写 " + v
                        + "，源码重算值（根 Dockerfile 的 COPY 源）" + f.dockerCopySource);
            }
        } catch (AssertionError e) {
            bad.add("主张 8h（Dockerfile COPY 路径 · 部署节陈旧项清单）：" + e.getMessage());
        }

        // ---- 主张 9：前端依赖主版本 + 包管理器（两处） ----
        try {
            String row = requireLine(lines, "| 前端 |");
            Matcher m = Pattern.compile("React (\\d+) \\+ antd (\\d+) \\+ react-router-dom (\\d+) \\+ axios，Vite (\\d+) 构建")
                    .matcher(row);
            if (!m.find()) {
                throw new AssertionError("抠不到「React N + antd N + react-router-dom N + axios，Vite N 构建」： " + row);
            }
            checkFrontendMajor(bad, "主张 9a（前端依赖版本 · 技术栈）", "react", m.group(1), f);
            checkFrontendMajor(bad, "主张 9a（前端依赖版本 · 技术栈）", "antd", m.group(2), f);
            checkFrontendMajor(bad, "主张 9a（前端依赖版本 · 技术栈）", "react-router-dom", m.group(3), f);
            checkFrontendMajor(bad, "主张 9a（前端依赖版本 · 技术栈）", "vite", m.group(4), f);
            String pm = requireGroup(row, Pattern.compile("([A-Za-z]+)\\s*(?:工程|workspace)（`_frontend/`"),
                    "前端包管理器（技术栈 前端行）");
            if (!pm.equals(f.frontendPkgManager)) {
                bad.add("主张 9a（前端包管理器 · 技术栈）：README 写 " + pm
                        + "，源码重算值 " + f.frontendPkgManager);
            }
        } catch (AssertionError e) {
            bad.add("主张 9a（前端依赖版本与包管理器 · 技术栈）：" + e.getMessage());
        }
        try {
            String line = requireTreeLine(readme, "_frontend");
            Matcher m = Pattern.compile("Vite \\+ React (\\d+) \\+ antd (\\d+)").matcher(line);
            if (!m.find()) {
                throw new AssertionError("抠不到「Vite + React N + antd N」： " + line);
            }
            checkFrontendMajor(bad, "主张 9b（前端依赖版本 · 结构树 _frontend 行）", "react", m.group(1), f);
            checkFrontendMajor(bad, "主张 9b（前端依赖版本 · 结构树 _frontend 行）", "antd", m.group(2), f);
            String pm = requireGroup(line, Pattern.compile("独立 ([A-Za-z]+) 工程"),
                    "前端包管理器（结构树 _frontend 行）");
            if (!pm.equals(f.frontendPkgManager)) {
                bad.add("主张 9b（前端包管理器 · 结构树 _frontend 行）：README 写 " + pm
                        + "，源码重算值 " + f.frontendPkgManager);
            }
        } catch (AssertionError e) {
            bad.add("主张 9b（前端依赖版本与包管理器 · 结构树）：" + e.getMessage());
        }

        if (!bad.isEmpty()) {
            throw new AssertionError("README 的定量主张与源码重算值对不上，共 " + bad.size() + " 条：\n  - "
                    + String.join("\n  - ", bad));
        }
    }

    /** 前端依赖主版本对账（README 写的是「19」这种主版本，package.json 里是「^19.2.0」）。 */
    private static void checkFrontendMajor(List<String> bad, String claim, String dep, String readmeValue, Facts f) {
        Integer source = f.frontendMajors.get(dep);
        if (source == null) {
            bad.add(claim + "：" + dep + "：源码重算值取不到");
            return;
        }
        if (!readmeValue.equals(String.valueOf(source))) {
            bad.add(claim + "：" + dep + " README 写 " + readmeValue
                    + "，源码重算值（" + FRONTEND_PKG + "）" + source);
        }
    }

    /** API 一览表：Controller 类名 → 表里写的路径。 */
    private static Map<String, String> apiTableRows(List<String> lines) {
        Map<String, String> out = new LinkedHashMap<>();
        Pattern row = Pattern.compile("^\\|\\s*`(/[^`]*)`\\s*\\|\\s*`(\\w+)`\\s*\\|");
        for (String l : lines) {
            Matcher m = row.matcher(l);
            if (m.find()) {
                out.put(m.group(2), m.group(1));
            }
        }
        if (out.isEmpty()) {
            throw new AssertionError("API 一览表里抠不到任何「| `/api/...` | `XController` |」行");
        }
        return out;
    }

    /** API 一览表里某个 Controller 的「主要子路径」单元格。 */
    private static String apiTableCell(String controller, List<String> lines) {
        Pattern row = Pattern.compile("^\\|\\s*`/[^`]*`\\s*\\|\\s*`" + Pattern.quote(controller) + "`\\s*\\|\\s*(.*?)\\s*\\|\\s*$");
        for (String l : lines) {
            Matcher m = row.matcher(l);
            if (m.find()) {
                return m.group(1);
            }
        }
        throw new AssertionError("API 一览表里找不到 " + controller + " 那一行");
    }

    /** 把 README「主要子路径」单元格归一成 "METHOD /全路径" 集合。 */
    private static Set<String> readmeEndpointsOf(String controller, List<String> lines) {
        String cell = apiTableCell(controller, lines);
        String base = apiTableRows(lines).get(controller);
        String s = cell.replace("`", "");
        int arrow = s.indexOf("→");
        if (arrow >= 0) {
            s = s.substring(0, arrow);
        }
        Set<String> out = new TreeSet<>();
        for (String seg : s.split("、")) {
            String t = seg.trim();
            if (t.isEmpty()) {
                continue;
            }
            String[] tokens = t.split("\\s+");
            List<String> methods = new ArrayList<>();
            Matcher mm = Pattern.compile("\\b(GET|POST|PUT|DELETE|PATCH)\\b").matcher(tokens[0]);
            while (mm.find()) {
                methods.add(mm.group(1));
            }
            if (methods.isEmpty()) {
                throw new AssertionError("抠不出 HTTP 方法（" + controller + " 的子路径单元格）： " + cell);
            }
            String path = tokens.length > 1 && tokens[1].startsWith("/") ? tokens[1] : "";
            for (String method : methods) {
                out.add(method + " " + (path.isEmpty() ? base : base + path));
            }
        }
        if (out.isEmpty()) {
            throw new AssertionError("抠不到任何端点（" + controller + " 的子路径单元格）： " + cell);
        }
        return out;
    }

    // ==================================================================
    // 工具：剥注释
    // ==================================================================

    /** 去掉 Java 源码里的注释（字符串字面量里的内容保留），免得 Javadoc 里的字样被当成真注解。 */
    private static String stripComments(String src) {
        StringBuilder sb = new StringBuilder(src.length());
        int i = 0;
        int n = src.length();
        while (i < n) {
            char c = src.charAt(i);
            if (c == '/' && i + 1 < n && src.charAt(i + 1) == '/') {
                while (i < n && src.charAt(i) != '\n') {
                    i++;
                }
            } else if (c == '/' && i + 1 < n && src.charAt(i + 1) == '*') {
                i += 2;
                while (i + 1 < n && !(src.charAt(i) == '*' && src.charAt(i + 1) == '/')) {
                    i++;
                }
                i = Math.min(n, i + 2);
            } else if (c == '"' || c == '\'') {
                char quote = c;
                sb.append(c);
                i++;
                while (i < n) {
                    char d = src.charAt(i);
                    sb.append(d);
                    i++;
                    if (d == '\\' && i < n) {
                        sb.append(src.charAt(i));
                        i++;
                        continue;
                    }
                    if (d == quote) {
                        break;
                    }
                }
            } else {
                sb.append(c);
                i++;
            }
        }
        return sb.toString();
    }

    // ==================================================================
    // 篡改工具（牙口测试用）
    // ==================================================================

    /** 把结构树某一行的「N 个 X」改成别的数。 */
    private static String tamperTreeCount(String readme, String node, int newCount, String what) {
        Matcher m = treeLine(readme, node);
        if (!m.find()) {
            throw new IllegalStateException("找不到节点行：" + node);
        }
        String line = m.group();
        String tampered = line.replaceFirst("(\\d+)\\s*个", newCount + " 个");
        if (tampered.equals(line)) {
            throw new IllegalStateException("该行抠不出「N 个」：" + line);
        }
        return readme.substring(0, m.start()) + tampered + readme.substring(m.end());
    }

    /** 把基本信息表里「**当前版本** | `X.Y.Z`」改成别的版本。 */
    private static String tamperProjectVersion(String readme, String newVersion) {
        Pattern p = Pattern.compile("(\\*\\*当前版本\\*\\*\\s*\\|\\s*`)\\d+\\.\\d+\\.\\d+(`)");
        Matcher m = p.matcher(readme);
        if (!m.find()) {
            throw new IllegalStateException("基本信息表里抠不出「**当前版本** | `X.Y.Z`」");
        }
        if (m.find()) {
            throw new IllegalStateException("「**当前版本**」在 README 里不止一处，锚点不唯一");
        }
        return replaceFirstKeepGroups(readme, p, "$1" + Matcher.quoteReplacement(newVersion) + "$2");
    }

    /** 把 API 一览表里某个 Controller 的类名改错（模拟「README 名单与源码不一致」）。 */
    private static String tamperControllerName(String readme, String path, String newName) {
        Pattern p = Pattern.compile("(\\|\\s*`" + Pattern.quote(path) + "`\\s*\\|\\s*`)\\w+(`)");
        Matcher m = p.matcher(readme);
        if (!m.find()) {
            throw new IllegalStateException("API 一览表里抠不到 " + path + " 那一行");
        }
        return replaceFirstKeepGroups(readme, p, "$1" + Matcher.quoteReplacement(newName) + "$2");
    }

    /**
     * 替换第一处匹配，且<b>保留捕获组</b>。
     *
     * <p>不能把整串替换文本套 {@code Matcher.quoteReplacement} —— 它会把 {@code $1} 也当成字面量，
     * 于是「锚点 + 数字」整段被换掉，锚点自己消失了，尺子就变成「主张抠不到」而不是「数字对不上」，
     * 判红的原因指错了地方。</p>
     */
    private static String replaceFirstKeepGroups(String text, Pattern p, String replacement) {
        Matcher m = p.matcher(text);
        StringBuilder sb = new StringBuilder();
        if (!m.find()) {
            throw new IllegalStateException("匹配不到：" + p.pattern());
        }
        m.appendReplacement(sb, replacement);
        m.appendTail(sb);
        return sb.toString();
    }

    /** 把 init.sql 那条清单里的一个表名抹掉（模拟「README 名单少一个」）。 */
    private static String tamperTableName(String readme, String table) {
        Pattern p = Pattern.compile("`" + Pattern.quote(table) + "`");
        Matcher m = p.matcher(readme);
        if (!m.find()) {
            throw new IllegalStateException("README 里抠不到表名 " + table);
        }
        return p.matcher(readme).replaceFirst(Matcher.quoteReplacement("`__dropped__`"));
    }

    // ==================================================================
    // 用例
    // ==================================================================

    @Test
    @DisplayName("README 的定量主张与源码重算值逐条对账")
    void readmeClaimsMatchTheCode() {
        Path root = repoRoot();
        Facts f = recomputed();
        verifyClaims(read(root.resolve("README.md")), f);
    }

    @Test
    @DisplayName("尺子的牙口：把 README 的数改错，判定必须判红")
    void rulerActuallyCondemnsWrongNumbers() {
        String readme = read(repoRoot().resolve("README.md"));
        Facts f = recomputed();

        // 变异 1：结构树里的 Controller 数改错（数字）
        String tamperedCount = tamperTreeCount(readme, "z-meta-web", f.controllerCount + 1, "Controller 数");
        assertNotEquals(readme, tamperedCount, "篡改失败：z-meta-web 的结构树行变了形？");
        AssertionError e1 = assertThrows(AssertionError.class, () -> verifyClaims(tamperedCount, f));
        assertTrue(e1.getMessage().contains("Controller 数") && e1.getMessage().contains("z-meta-web"),
                "报错要指向真正对不上的那条主张，实际是：" + e1.getMessage());

        // 变异 2：根 POM 版本改错（版本）
        String tamperedVersion = tamperProjectVersion(readme, "9.9.9");
        AssertionError e2 = assertThrows(AssertionError.class, () -> verifyClaims(tamperedVersion, f));
        assertTrue(e2.getMessage().contains("主张 1a") && e2.getMessage().contains("9.9.9"),
                "版本改错必须报出根 POM 版本那条主张，实际是：" + e2.getMessage());

        // 变异 3：API 表的 Controller 名单改错（名单）
        String tamperedName = tamperControllerName(readme, "/api/health", "HealthCtrl");
        AssertionError e3 = assertThrows(AssertionError.class, () -> verifyClaims(tamperedName, f));
        assertTrue(e3.getMessage().contains("Controller 名单") && e3.getMessage().contains("HealthCtrl"),
                "名单改错必须报出名单那条主张，实际是：" + e3.getMessage());

        // 变异 4：init.sql 的表名单改错（名单）
        String tamperedTable = tamperTableName(readme, "z_users");
        AssertionError e4 = assertThrows(AssertionError.class, () -> verifyClaims(tamperedTable, f));
        assertTrue(e4.getMessage().contains("init.sql") && e4.getMessage().contains("z_users"),
                "表名单改错必须报出表清单那条主张，实际是：" + e4.getMessage());
    }

    @Test
    @DisplayName("不许冤枉健康页：真实 README 不得被判红")
    void healthyReadmeIsNotCondemned() {
        String readme = read(repoRoot().resolve("README.md"));
        verifyClaims(readme, recomputed());
    }
}
