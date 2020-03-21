package com.plugin.annotationvalidate.hibernatevalidator;

import com.baidu.unbiz.fluentvalidator.Validator;
import com.baidu.unbiz.fluentvalidator.ValidatorContext;
import com.baidu.unbiz.fluentvalidator.ValidatorHandler;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 手机号码校验
 *
 * @author LK
 * @date 2018/3/28
 * @description
 */
public class PhoneValidator extends ValidatorHandler<String> implements ConstraintValidator<Phone, String>, Validator<String> {

    /**
     * 国际化类型
     */
    private String type;

    /**
     * Initializes the annotationvalidate in preparation for
     * {@link #isValid(Object, ConstraintValidatorContext)} calls.
     * The constraint annotation for a given constraint declaration
     * is passed.
     * <p/>
     * This method is guaranteed to be called before any use of this instance for
     * validation.
     *
     * @param constraintAnnotation annotation instance for a given constraint declaration
     */
    @Override
    public void initialize(Phone constraintAnnotation) {
        this.type = constraintAnnotation.type();
    }

    private static Pattern p = Pattern.compile("^((13[0-9])|(14[57])|(15[0-9])|(17[0-8])|(18[0-9])|(19[0-9]))\\d{8}$");

    /**
     * Implements the validation logic.
     * The state of {@code value} must not be altered.
     * <p/>
     * This method can be accessed concurrently, thread-safety must be ensured
     * by the implementation.
     *
     * @param value   object to validate
     * @param context context in which the constraint is evaluated
     * @return {@code false} if {@code value} does not pass the constraint
     */
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value != null) {
            Matcher m = p.matcher(value);
            if (m.matches()) {
                return true;
            }
        }
        return false;
    }

    /**
     * @param context
     * @param str
     * @return boolean
     * @author lk
     * @date sds
     */
    @Override
    public boolean validate(ValidatorContext context, String str) {
        Matcher m = p.matcher(str);
        if (m.matches()) {
            return true;
        } else {
            context.addErrorMsg(String.format("%s 不是合法的手机号码", str));
        }
        return false;
    }

}
