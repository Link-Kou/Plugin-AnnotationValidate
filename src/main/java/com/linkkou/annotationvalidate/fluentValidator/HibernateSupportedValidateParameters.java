package com.linkkou.annotationvalidate.fluentValidator;

import com.baidu.unbiz.fluentvalidator.Validator;
import com.baidu.unbiz.fluentvalidator.ValidatorContext;
import com.baidu.unbiz.fluentvalidator.ValidatorHandler;
import com.baidu.unbiz.fluentvalidator.annotation.NotThreadSafe;
import com.baidu.unbiz.fluentvalidator.jsr303.ConstraintViolationTransformer;
import com.baidu.unbiz.fluentvalidator.jsr303.DefaultConstraintViolationTransformer;
import com.baidu.unbiz.fluentvalidator.support.GroupingHolder;
import com.baidu.unbiz.fluentvalidator.util.ArrayUtil;
import com.baidu.unbiz.fluentvalidator.util.CollectionUtil;
import com.linkkou.annotationvalidate.ValidatedName;

import javax.validation.ConstraintViolation;
import java.lang.reflect.Method;
import java.util.Set;


/**
 * @author lk
 * @version 1.0
 * @date 2020/4/12 14:59
 */
@NotThreadSafe
public class HibernateSupportedValidateParameters<T> extends ValidatorHandler<T> implements Validator<T> {

    private static javax.validation.Validator HIBERANTE_VALIDATOR;

    private int hibernateDefaultErrorCode;

    private final ConstraintViolationTransformer constraintViolationTransformer = new DefaultConstraintViolationTransformer();

    private final String method;

    private final Object[] var3;

    private final String id;

    private final Class<?> c;

    /**
     * @param var3   参数
     * @param method 方法名称
     * @param id     id
     */
    public HibernateSupportedValidateParameters(Object[] var3, Class<?> c, String method, String id) {
        this.var3 = var3;
        this.c = c;
        this.method = method;
        this.id = id;
    }

    @Override
    public boolean accept(ValidatorContext context, T t) {
        return true;
    }

    @Override
    public boolean validate(ValidatorContext context, T t) {
        try {
            final Method[] methods = c.getMethods();
            Method method = null;
            for (Method value : methods) {
                if (this.method.equals(value.getName())) {
                    final ValidatedName annotation = value.getAnnotation(ValidatedName.class);
                    if (this.id.equals(annotation.value())) {
                        method = value;
                    }
                }
            }
            if (null == method) {
                return false;
            }
            Class<?>[] groups = GroupingHolder.getGrouping();
            Set<ConstraintViolation<Object>> constraintViolations;
            if (ArrayUtil.isEmpty(groups)) {
                constraintViolations = HIBERANTE_VALIDATOR.forExecutables().validateParameters(t, method, var3);
            } else {
                constraintViolations = HIBERANTE_VALIDATOR.forExecutables().validateParameters(t, method, var3, groups);
            }
            if (CollectionUtil.isEmpty(constraintViolations)) {
                return true;
            } else {
                for (ConstraintViolation<Object> constraintViolation : constraintViolations) {
                    context.addError(constraintViolationTransformer.toValidationError(constraintViolation)
                            .setErrorCode(hibernateDefaultErrorCode));
                }
                return false;
            }
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public void onException(Exception e, ValidatorContext context, T t) {
    }

    public javax.validation.Validator getHiberanteValidator() {
        return HIBERANTE_VALIDATOR;
    }

    public HibernateSupportedValidateParameters<T> setHiberanteValidator(javax.validation.Validator validator) {
        HIBERANTE_VALIDATOR = validator;
        return this;
    }

    public HibernateSupportedValidateParameters<T> setHibernateDefaultErrorCode(int hibernateDefaultErrorCode) {
        this.hibernateDefaultErrorCode = hibernateDefaultErrorCode;
        return this;
    }
}
