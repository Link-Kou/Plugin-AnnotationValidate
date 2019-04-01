package com.plugin.annotationvalidate.hibernatevalidator.constraint;

/**
 *
 * @author lk
 * @date 2018/9/15
 */
public @interface DateTimeRange {
    int year() default 0;
    int month() default 0;
    int day() default 0;
    int hour() default 0;
    int minute() default 0;
    int second() default 0;
}
//取 >= 0 值,用于决定时间的范围 比较以>= || <= 执行
