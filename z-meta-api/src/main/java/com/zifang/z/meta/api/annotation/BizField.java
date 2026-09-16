package com.zifang.z.meta.api.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface BizField {
    String name();

    FieldType type() default FieldType.STRING;

    String refModel() default "";

    String refModule() default "";

    String dictCode() default "";

    enum FieldType {
        STRING, INTEGER, DECIMAL, BOOLEAN, DATE, DATETIME, ENUM, REF, TEXT, JSON
    }
}
