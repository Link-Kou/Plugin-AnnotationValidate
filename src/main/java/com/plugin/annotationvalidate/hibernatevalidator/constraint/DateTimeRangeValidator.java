package com.plugin.annotationvalidate.hibernatevalidator.constraint;

import com.baidu.unbiz.fluentvalidator.Validator;
import com.baidu.unbiz.fluentvalidator.ValidatorContext;
import com.baidu.unbiz.fluentvalidator.ValidatorHandler;
import com.plugin.annotationvalidate.hibernatevalidator.enums.Range;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.time.*;
import java.util.Date;

/**
 * 手机号码校验
 *
 * @author LK
 * @date 2018/3/28
 * @description
 */
public class DateTimeRangeValidator extends ValidatorHandler<Date> implements ConstraintValidator<DateTimeRanges, Date>, Validator<Date> {

    /**
     * 区间类型
     */
    private Range range;
    /**
     * 时间类型
     */
    private DateTimeRange[] dateTimeRange;

    /**
     * Initializes the annotationvalidate in preparation for
     * {@link #isValid(Object, ConstraintValidatorContext)} calls.
     * The constraint annotation for a given constraint declaration
     * is passed.
     * <p/>
     * This method is guaranteed to be called beforeOrBack any use of this instance for
     * validation.
     *
     * @param constraintAnnotation annotation instance for a given constraint declaration
     */
    @Override
    public void initialize(DateTimeRanges constraintAnnotation) {
        this.range = constraintAnnotation.range();
        this.dateTimeRange = constraintAnnotation.datatimerange();
    }

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
    public boolean isValid(Date value, ConstraintValidatorContext context) {
        if (value != null) {
            return beforeOrBack(LocalDateTime.now(), LocalDateTime.ofInstant(value.toInstant(), ZoneId.systemDefault()));
        }
        return false;
    }

    /**
     * baidu 校验方法
     */
    @Override
    public boolean validate(ValidatorContext context, Date value) {
        if (value != null) {
            return beforeOrBack(LocalDateTime.now(), LocalDateTime.ofInstant(value.toInstant(), ZoneId.systemDefault()));
        }
        return false;
    }

    /**
     * 前={当前时间,当前时间+x}，当前时间 <= 当前时间+x
     * 后={当前时间,当前时间-x}，当前时间 >= 当前时间-x
     * 区={当前时间+x,当前时间-x}，前时间 <= 当前时间+x || 当前时间 >= 当前时间-x
     * @param nowlocalDateTime
     * @param localDateTime
     */
    private boolean beforeOrBack(LocalDateTime nowlocalDateTime, LocalDateTime localDateTime) {
        LocalDateTime localDateTime_t = nowlocalDateTime;
        LocalDateTime nowlocalDateTime_t = localDateTime;
        for (int i = 0; i < this.dateTimeRange.length; i++) {
            switch (range) {
                case BEFORE:
                    if (!(dateTimeRange.length == 1)) {
                        return false;
                    }
                    //前={当前时间,当前时间+x}，当前时间 <= 当前时间+x
                    break;
                case BACK:
                    if (!(dateTimeRange.length == 1)) {
                        return false;
                    }
                    //后={当前时间,当前时间-x}，当前时间 >= 当前时间-x
                    nowlocalDateTime = localDateTime_t;
                    localDateTime = nowlocalDateTime_t;
                    break;
                case INTERVAL:
                    if (!(dateTimeRange.length == 2)) {
                        return false;
                    }
                    switch (i) {
                        case 0:
                            break;
                        case 1:
                            nowlocalDateTime = localDateTime_t;
                            localDateTime = nowlocalDateTime_t;
                            break;
                    }
            }
            final DateTimeRange dateTimeRange = this.dateTimeRange[i];
            final int years = Period.between(localDateTime.toLocalDate(), nowlocalDateTime.toLocalDate()).getYears();
            if (!(years >= 0 && years <= dateTimeRange.year())) {
                return false;
            }
            final int months = Period.between(localDateTime.toLocalDate(), nowlocalDateTime.toLocalDate()).getMonths();
            if (!(months >= 0 && months <= dateTimeRange.day())) {
                return false;
            }
            final int days = Period.between(localDateTime.toLocalDate(), nowlocalDateTime.toLocalDate()).getDays();
            if (!(days >= 0 && days <= dateTimeRange.day())) {
                return false;
            }
            final long hours = Duration.between(localDateTime.toLocalTime(), nowlocalDateTime.toLocalTime()).toHours();
            if (!(hours >= 0 && hours <= dateTimeRange.hour())) {
                return false;
            }
            final long minute = Duration.between(localDateTime.toLocalTime(), nowlocalDateTime.toLocalTime()).toMinutes();
            if (!(minute >= 0 && minute <= dateTimeRange.minute())) {
                return false;
            }
            final long seconds = Duration.between(localDateTime.toLocalTime(), nowlocalDateTime.toLocalTime()).getSeconds();
            if (!(seconds >= 0 && seconds <= dateTimeRange.second())) {
                return false;
            }
            return true;
        }
        return false;
    }

}
