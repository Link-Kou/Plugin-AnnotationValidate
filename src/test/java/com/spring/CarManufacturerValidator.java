package com.spring;

import com.baidu.unbiz.fluentvalidator.Validator;
import com.baidu.unbiz.fluentvalidator.ValidatorContext;
import com.baidu.unbiz.fluentvalidator.ValidatorHandler;
import com.baidu.unbiz.fluentvalidator.util.Preconditions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @author zhangxu
 */
@Component
public class CarManufacturerValidator extends ValidatorHandler<String> implements Validator<String> {

    @Autowired
    private beantest beantest;

    @Override
    public boolean validate(ValidatorContext context, String t) {
        Preconditions.checkNotNull(beantest, "car should not be null");
        return true;
    }

}
