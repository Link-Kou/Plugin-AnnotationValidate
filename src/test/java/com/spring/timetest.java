package com.spring;

import com.baidu.unbiz.fluentvalidator.ComplexResult;
import com.baidu.unbiz.fluentvalidator.FluentValidator;
import com.baidu.unbiz.fluentvalidator.Result;
import com.plugin.annotationvalidate.HibernateValidator;
import com.plugin.annotationvalidate.hibernatevalidator.constraint.DateTimeRange;
import org.junit.Test;

import java.text.ParseException;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import static com.baidu.unbiz.fluentvalidator.ResultCollectors.toComplex;
import static com.baidu.unbiz.fluentvalidator.ResultCollectors.toSimple;

public class timetest {


    @Test
    public void name() {
        LocalDateTime startTime = LocalDateTime.now();
        LocalDateTime endTime = startTime.plusDays(7);




    }


    @Test
    public void name2() throws ParseException {
        ComplexResult ret = FluentValidator.checkAll().failFast().on(new Car(), new HibernateValidator<Car>().validator())
                .doValidate()
                .result(toComplex());
    }

}
