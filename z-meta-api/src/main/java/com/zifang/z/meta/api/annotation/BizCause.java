package com.zifang.z.meta.api.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 因果链注解：声明「因为什么，所以在什么前提下，导致了什么」。
 *
 * <p>因果关系是代码里最难写下来的知识 —— 它散落在注释和脑子里，代码本身不含这条边。
 * 这个注解把它变成可采集的一等公民：采集器产出因果三元组
 * （subject --causes--> effect，附带 because / condition 与权重），
 * 认知板块把整个组织的方法级因果边汇成一张图。</p>
 *
 * <p>挂在方法上表示「执行这个方法的效果」；挂在类上表示「这个领域对象本身的效果」。</p>
 *
 * @author zifang
 * @since 1.1.0
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface BizCause {

    /**
     * 效果：这个动作最终导致什么。
     */
    String effect();

    /**
     * 原因：是什么导致了该效果。
     */
    String because() default "";

    /**
     * 前提条件：效果成立需要什么条件同时成立。
     */
    String condition() default "";

    /**
     * 主体：谁导致；留空则回落到所在类/方法的类名。
     */
    String subject() default "";

    /**
     * 因果边权重（0~1），认知板块按权重决定推断强度。
     */
    double weight() default 1.0;
}
