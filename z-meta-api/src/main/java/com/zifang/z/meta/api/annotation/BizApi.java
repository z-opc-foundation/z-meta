package com.zifang.z.meta.api.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 业务接口注解：给一个对外端点标上业务契约。
 *
 * <p>HTTP 方法与路径默认从 Spring 的映射注解读取；本注解负责补业务侧读不懂的那部分：
 * 这个端点属于哪个业务、给谁用、返回什么语义、是否废弃。</p>
 *
 * <p>采集器产出 OpenAPI 文档时，Spring 映射给「怎么调」，本注解给「调它意味着什么」。</p>
 *
 * @author zifang
 * @since 1.1.0
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface BizApi {

    /**
     * 显式路径；留空则取 Spring 映射注解上的值。
     */
    String path() default "";

    /**
     * 显式 HTTP 方法（GET/POST/...）；留空则取 Spring 映射注解上的值。
     */
    String httpMethod() default "";

    /**
     * 一句话摘要。
     */
    String summary() default "";

    /**
     * 详细说明。
     */
    String description() default "";

    /**
     * 业务标签。
     */
    String[] tags() default {};

    /**
     * 响应内容类型。
     */
    String produces() default "application/json";

    /**
     * 是否已废弃。
     */
    boolean deprecated() default false;
}
