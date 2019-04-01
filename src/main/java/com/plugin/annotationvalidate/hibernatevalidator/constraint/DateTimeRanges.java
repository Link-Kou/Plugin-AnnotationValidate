package com.plugin.annotationvalidate.hibernatevalidator.constraint;


import com.plugin.annotationvalidate.hibernatevalidator.enums.Range;

import javax.validation.Constraint;
import javax.validation.Payload;
import javax.validation.ReportAsSingleViolation;
import javax.validation.constraints.NotNull;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * 时间区间校验
 * 支持:中国大陆地区
 * @author: LK
 * @date: 2018/3/28
 * @description:
 */
@Documented
@Constraint(validatedBy = DateTimeRangeValidator.class)
@Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER })
@Retention(RUNTIME)
@ReportAsSingleViolation
@NotNull
public @interface DateTimeRanges {

    /**
     * 区间 前={当前时间,当前时间+x} 后={当前时间,当前时间-X} 区间={当前时间+X,当前时间-X}
     */
    Range range() default Range.INTERVAL;

    /**
     * 区间
     */
    DateTimeRange[] datatimerange() default {@DateTimeRange};

    String message() default "{时间不在范围内}";

    Class<?>[] groups() default { };

    Class<? extends Payload>[] payload() default { };

    /**
     * Defines several {@code @NotEmpty} annotations on the same element.
     */
    @Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER })
    @Retention(RUNTIME)
    @Documented
    public @interface List {
        DateTimeRanges[] value();
    }

}
