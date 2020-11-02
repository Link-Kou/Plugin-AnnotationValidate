package com.linkkou.annotationvalidate;

import com.baidu.unbiz.fluentvalidator.ComplexResult;

/**
 * 错误输出
 * @author lk
 * @version 1.0
 * @date 2020/11/1 21:38
 */
public class ValidationException extends RuntimeException {


    public ValidationException(ComplexResult complexresultapt) {
        super(complexresultapt.toString());
    }

    public ValidationException() {
    }

    public ValidationException(ComplexResult complexresultapt, Throwable cause) {
        super(complexresultapt.toString(), cause);
    }

    public ValidationException(Throwable cause) {
        super(cause);
    }
}
