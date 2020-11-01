package com.linkkou.annotationvalidate;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 编译后,ValidatedName注解将替换{@link com.linkkou.annotationvalidate.Validated}作用到方法上面。
 * 方便查询相关方法.解决方法重载等问题产生
 *
 * @author lk
 * @version 1.0
 * @date 2020/4/30 09:16
 */
@Target({ElementType.PARAMETER, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidatedName {
    String value();
}
