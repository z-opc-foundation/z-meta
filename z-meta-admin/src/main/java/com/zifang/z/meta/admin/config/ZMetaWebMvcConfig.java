package com.zifang.z.meta.admin.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.PathResourceResolver;

import java.io.IOException;

/**
 * SPA history fallback。
 *
 * <p>前端 BrowserRouter 刷新子路由时 Spring 找不到静态资源就返回 JSON 404，
 * 浏览器拿不到 index.html 直接白屏。vite dev/preview 自带 fallback，本地测不出来，
 * 只在生产 jar 现形。
 *
 * <p>api/ 与 assets/ 前缀必须返回 null 保持 404 —— 否则前端会拿到一坨 HTML 当 JSON 解析。
 */
@Configuration
public class ZMetaWebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/")
                .resourceChain(true)
                .addResolver(new PathResourceResolver() {
                    @Override
                    protected Resource getResource(String resourcePath, Resource location) throws IOException {
                        Resource requested = location.createRelative(resourcePath);
                        if (requested.exists() && requested.isReadable()) {
                            return requested;
                        }
                        if (resourcePath.startsWith("api/") || resourcePath.startsWith("assets/")) {
                            return null;
                        }
                        // 带扩展名的不是前端路由：vite base 配错时 index.html 会去请求
                        // /xxx/assets/x.js，回 index.html 会得到一个 200 text/html 的假成功，
                        // 浏览器只报 "Failed to fetch dynamically imported module"，看不出是 base 错了。
                        if (resourcePath.lastIndexOf('.') > resourcePath.lastIndexOf('/')) {
                            return null;
                        }
                        return location.createRelative("index.html");
                    }
                });
    }
}
