package com.linkkou.annotationvalidate.fluentValidator;

import com.baidu.unbiz.fluentvalidator.jsr303.HibernateSupportedValidator;
import com.linkkou.annotationvalidate.fluentValidator.HibernateSupportedValidateParameters;

import javax.validation.Validation;
import java.lang.reflect.Method;

/**
 * 减化百度校验框架Hibernate初始化繁琐
 *
 * @param <T>
 */
public class HibernateValidator<T> {

    public HibernateSupportedValidator<T> validator() {
        return new HibernateSupportedValidator<T>().setHiberanteValidator(Validation.buildDefaultValidatorFactory().getValidator());
    }

    public HibernateSupportedValidateParameters<T> ValidateParameters(Object[] var3, String method, Class<?>... parameterTypes) {
        return new HibernateSupportedValidateParameters<T>(var3, method, parameterTypes).setHiberanteValidator(Validation.buildDefaultValidatorFactory().getValidator());
    }
}
