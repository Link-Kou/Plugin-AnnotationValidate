package com.spring;

import com.baidu.unbiz.fluentvalidator.ComplexResult;
import com.baidu.unbiz.fluentvalidator.FluentValidator;
import com.plugin.annotationvalidate.HibernateValidator;
import com.plugin.annotationvalidate.Validated;
import org.junit.Test;

import java.text.ParseException;
import java.time.*;

import static com.baidu.unbiz.fluentvalidator.ResultCollectors.toComplex;

public class timetest {


    @Test
    public void name() {
        LocalDateTime startTime = LocalDateTime.now();
        LocalDateTime endTime = startTime.plusDays(7);
        //namessss names = new namessss(new beantest());
        NameValidated(null);

    }

    public void NameValidated(@Validated beantest car) {

    }


    @Test
    public void name2() throws ParseException {
        ComplexResult ret = FluentValidator.checkAll().failFast().on(new Car(), new HibernateValidator<Car>().validator())
                .doValidate()
                .result(toComplex());
    }

}
