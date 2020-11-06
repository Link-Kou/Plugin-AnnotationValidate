package com.linkkou.annotationvalidate.utils;

import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.Name;

import java.util.ArrayList;
import java.util.List;

/**
 * @author lk
 * @version 1.0
 * @date 2020/4/12 17:06
 */
public class JCHelp {

    private TreeMaker make;

    private Name.Table names;

    public JCHelp(TreeMaker make, Name.Table names) {
        this.make = make;
        this.names = names;
    }

    public JCTree.JCFieldAccess selectFieldAccess(String classPath) {
        final String com = classPath;
        final String[] split = com.split("\\.");
        List<Name> nameList = new ArrayList<>();
        for (String key : split) {
            nameList.add(names.fromString(key));
        }
        //遍历的对象路径必须大于1
        final int size = nameList.size();
        if (size >= 2) {
            List<JCTree.JCFieldAccess> jcIdentList = new ArrayList<>();
            for (int i = 0; i < size; i++) {
                if (i == 0) {
                    jcIdentList.add(make.Select(make.Ident(nameList.get(i)), nameList.get(i + 1)));
                } else if (i >= 2) {
                    jcIdentList.add(make.Select(jcIdentList.get(i - 2), nameList.get(i)));
                }
            }
            return jcIdentList.get(jcIdentList.size() - 1);
        }
        return null;
    }
}
