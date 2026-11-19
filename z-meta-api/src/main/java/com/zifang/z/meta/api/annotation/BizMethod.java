package com.zifang.z.meta.api.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 业务方法注解：给一个方法标上「它在业务上做什么」。
 *
 * <p>{@link BizModel} 说「这是什么数据」，{@code @BizMethod} 说「拿这些数据能干什么」。
 * 采集器把两者拼起来，就得到一条完整的业务语义链：
 * 数据 → 方法 → 产出。</p>
 *
 * <p>方法上再挂 {@link BizApi} 可额外标出它是哪个对外端点；
 * 挂 {@link BizCause} 可声明它的因果效果。</p>
 *
 * @author zifang
 * @since 1.1.0
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface BizMethod {

    /**
     * 方法业务名（人类可读）。
     */
    String name();

    /**
     * 所属业务模块；留空则回落到所在 {@link BizModel} 的 module。
     */
    String module() default "";

    /**
     * 方法说明。
     */
    String description() default "";

    /**
     * 业务操作类型。
     */
    Operation operation() default Operation.OTHER;

    /**
     * 是否幂等：重复执行不产生额外副作用。
     */
    boolean idempotent() default false;

    /**
     * 业务操作类型。刻意不叫 CRUD —— 业务动作往往不是增删改查。
     */
    enum Operation {
        /** 查询 */
        QUERY,
        /** 创建 */
        CREATE,
        /** 更新 */
        UPDATE,
        /** 删除 */
        DELETE,
        /** 业务动作（非 CRUD） */
        ACTION,
        /** 其它 */
        OTHER
    }
}
