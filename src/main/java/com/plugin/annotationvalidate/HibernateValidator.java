package com.plugin.annotationvalidate;

import com.baidu.unbiz.fluentvalidator.jsr303.HibernateSupportedValidator;

import javax.validation.Validation;

/**
 * 减化百度校验框架Hibernate初始化繁琐
 *
 * @param <T>
 */
public class HibernateValidator<T> {

    public HibernateSupportedValidator<T> validator() {
        return new HibernateSupportedValidator<T>().setHiberanteValidator(Validation.buildDefaultValidatorFactory().getValidator());
    }
}
