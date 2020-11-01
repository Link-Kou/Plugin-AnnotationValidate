package com.linkkou.annotationvalidate.fluentValidator;

import com.baidu.unbiz.fluentvalidator.jsr303.HibernateSupportedValidator;

import javax.validation.Validation;

/**
 * 减化百度校验框架Hibernate初始化繁琐
 *
 * @param <T>
 */
public class HibernateValidator<T> {

    /**
     * 校验方法参数上面实现
     *
     * @return HibernateSupportedValidator校验器
     */
    public HibernateSupportedValidator<T> validator() {
        return new HibernateSupportedValidator<T>().setHiberanteValidator(Validation.buildDefaultValidatorFactory().getValidator());
    }

    /**
     * 作用于方法上面使用
     *
     * @param var3   方法参数
     * @param c      当前类
     * @param method 方法名称(方法重载的问题)，可以通过反射查询出来方法。相比较还是id方便
     * @param id     识别id{@link com.linkkou.annotationvalidate}
     * @return HibernateSupportedValidateParameters校验器
     */
    public HibernateSupportedValidateParameters<T> ValidateParameters(Object[] var3, Class<?> c, String method, String id) {
        return new HibernateSupportedValidateParameters<T>(var3, c, method, id).setHiberanteValidator(Validation.buildDefaultValidatorFactory().getValidator());
    }
}
