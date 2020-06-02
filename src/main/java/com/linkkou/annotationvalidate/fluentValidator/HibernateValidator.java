package com.linkkou.annotationvalidate.fluentValidator;

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

    /**
     * @param var3   方法变量名称
     * @param method 方法名称
     * @return
     */
    public HibernateSupportedValidateParameters<T> ValidateParameters(Object[] var3, Class<?> c, String method, String id) {
        return new HibernateSupportedValidateParameters<T>(var3, c, method, id).setHiberanteValidator(Validation.buildDefaultValidatorFactory().getValidator());
    }
}
