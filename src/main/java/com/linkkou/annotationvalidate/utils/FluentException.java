package com.linkkou.annotationvalidate.utils;

import com.linkkou.annotationvalidate.ValidateConfig;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.Name;

/**
 * 构建错误代码
 *
 * @author lk
 * @version 1.0
 * @date 2020/11/2 08:01
 */
public class FluentException {
    private TreeMaker make;
    private Name.Table names;
    private JCHelp jcHelp;

    public FluentException(TreeMaker make, Name.Table names) {
        this.make = make;
        this.names = names;
        this.jcHelp = new JCHelp(make, names);
    }

    public JCTree.JCStatement getException() {
        final JCTree.JCFieldAccess validationException = jcHelp
                .selectFieldAccess("com.linkkou.annotationvalidate.ValidationException");
        JCTree.JCStatement block = make.Throw(make.NewClass(null,
                null,
                //类名称 会自己导入包
                validationException,
                //参数
                com.sun.tools.javac.util.List.of(make.Ident(names.fromString(ValidateConfig.VARNAME))),
                null)
        );
        return block;
    }
}
