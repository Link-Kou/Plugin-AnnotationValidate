package com.spring;


import com.baidu.unbiz.fluentvalidator.ComplexResult;
import com.baidu.unbiz.fluentvalidator.FluentValidator;
import com.baidu.unbiz.fluentvalidator.Result;
import com.plugin.annotationvalidate.HibernateValidator;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.text.ParseException;

import static com.baidu.unbiz.fluentvalidator.ResultCollectors.toComplex;
import static com.baidu.unbiz.fluentvalidator.ResultCollectors.toSimple;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"classpath*:/config/spring/spring-mvc.xml"})
public class spring {

    @Test
    public void Test() throws ParseException {
        Car car = new Car();
        Result result = FluentValidator.checkAll().failOver()
                .on(car.getManufacturer(), new CarManufacturerValidator())
                .doValidate().result(toSimple());
    }

    @Test
    public void Test2() {
        FluentValidator fluentValidator = FluentValidator.checkAll().failFast();
        ComplexResult ret = fluentValidator.on("", new HibernateValidator<String>().validator())
                .doValidate()
                .result(toComplex());
        if (!ret.isSuccess()) {
            System.out.print("123ßß");
        }
    }
}
