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

    private ConstraintViolationTransformer constraintViolationTransformer = new DefaultConstraintViolationTransformer();

    private String method;

    private Object[] var3;

    private Class[] parameterTypes;


    public HibernateSupportedValidateParameters(Object[] var3, String method, Class<?>... parameterTypes) {
        this.var3 = var3;
        this.method = method;
        this.parameterTypes = parameterTypes;
    }

    @Override
    public boolean accept(ValidatorContext context, T t) {
        return true;
    }

    @Override
    public boolean validate(ValidatorContext context, T t) {
        try {
            final Method method = t.getClass().getMethod(this.method, parameterTypes);
            Class<?>[] groups = GroupingHolder.getGrouping();
            Set<ConstraintViolation<T>> constraintViolations;
            if (ArrayUtil.isEmpty(groups)) {
                constraintViolations = HIBERANTE_VALIDATOR.forExecutables().validateParameters(t, method, var3);
            } else {
                constraintViolations = HIBERANTE_VALIDATOR.forExecutables().validateParameters(t, method, var3, groups);
            }
            if (CollectionUtil.isEmpty(constraintViolations)) {
                return true;
            } else {
                for (ConstraintViolation<T> constraintViolation : constraintViolations) {
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
