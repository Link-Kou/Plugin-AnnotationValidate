package com.linkkou.annotationvalidate.utils;

import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.Name;

/**
 * @author lk
 * @version 1.0
 * @date 2020/4/12 17:36
 */
public class FluentValidatorCode {

    private TreeMaker make;
    private Name.Table names;
    private JCHelp jcHelp;

    public FluentValidatorCode(TreeMaker make, Name.Table names) {
        this.make = make;
        this.names = names;
        this.jcHelp = new JCHelp(make, names);
    }

    /**
     * 构建静态类 com.baidu.unbiz.fluentvalidator.FluentValidator.checkAll().failFast()
     *
     * @return
     */
    public JCTree.JCMethodInvocation getFluentValidator() {
        final JCTree.JCFieldAccess getCheckAllCall = jcHelp
                .selectFieldAccess("com.baidu.unbiz.fluentvalidator.FluentValidator.checkAll");
        // checkAll();
        JCTree.JCMethodInvocation apply = make.Apply(
                com.sun.tools.javac.util.List.nil(),
                getCheckAllCall,
                com.sun.tools.javac.util.List.nil()
        );

        // failFast();
        JCTree.JCMethodInvocation apply2 = make.Apply(
                com.sun.tools.javac.util.List.nil(),
                make.Select(apply, names.fromString("failFast")),
                com.sun.tools.javac.util.List.nil()
        );
        return apply2;
    }

    /**
     * 构建 .doValidate().result(toComplex())
     *
     * @param applyitem new HibernateValidator<String>().annotationvalidate()).on().on()
     * @return
     */
    public JCTree.JCMethodInvocation getComplexResult(JCTree.JCMethodInvocation applyitem) {
        //.doValidate()
        JCTree.JCMethodInvocation apply4 = make.Apply(
                com.sun.tools.javac.util.List.nil(),
                make.Select(applyitem, names.fromString("doValidate")),
                com.sun.tools.javac.util.List.nil()
        );

        //.doValidate().result(toComplex())
        JCTree.JCMethodInvocation apply5 = make.Apply(
                com.sun.tools.javac.util.List.nil(),
                make.Select(apply4, names.fromString("result")),
                com.sun.tools.javac.util.List.of(
                        make.Apply(
                                com.sun.tools.javac.util.List.nil(),
                                jcHelp.selectFieldAccess("com.baidu.unbiz.fluentvalidator.ResultCollectors.toComplex"),
                                com.sun.tools.javac.util.List.nil()
                        )
                )
        );

        return apply5;
    }


}
