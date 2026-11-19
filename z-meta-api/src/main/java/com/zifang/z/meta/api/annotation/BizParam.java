package com.zifang.z.meta.api.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 业务参数注解：给方法入参标上业务语义。
 *
 * <p>方法参数在 Java 反射里只剩类型和名字，业务含义全丢了。这个注解把
 * 「这个参数是哪张表的哪个字段、是不是必填、查哪个字典」补回来，
 * 采集器据此生成接口契约，认知板块据此理解参数之间的引用关系。</p>
 *
 * @author zifang
 * @since 1.1.0
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface BizParam {

    /**
     * 参数业务名。
     */
    String name();

    /**
     * 参数业务类型。
     */
    BizField.FieldType type() default BizField.FieldType.STRING;

    /**
     * 是否必填。
     */
    boolean required() default true;

    /**
     * 参数说明。
     */
    String description() default "";

    /**
     * 引用模型编码（指向另一个 {@link BizModel}）。
     */
    String refModel() default "";

    /**
     * 引用模型所属模块。
     */
    String refModule() default "";

    /**
     * 字典编码。
     */
    String dictCode() default "";

    /**
     * 参数位置。
     */
    Position position() default Position.QUERY;

    /**
     * 参数位置。
     */
    enum Position {
        /** 查询串 */
        QUERY,
        /** 路径变量 */
        PATH,
        /** 请求体 */
        BODY,
        /** 请求头 */
        HEADER
    }
}
