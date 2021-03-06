package com.linkkou.annotationvalidate.inliner;

import com.linkkou.annotationvalidate.Validated;
import com.linkkou.annotationvalidate.utils.FluentException;
import com.linkkou.annotationvalidate.utils.FluentValidatorCode;
import com.linkkou.annotationvalidate.utils.JCHelp;
import com.sun.source.util.Trees;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.tree.TreeTranslator;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.Name;

import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import java.util.*;

/**
 * 当 {@link Validated} 用于方法参数上面的时候，参数必须配合相关的
 * 下面的代码是繁琐的代码构建过程
 * @author lk
 * @version 1.0
 * @date 2020/4/11 22:25
 */
public class InlinerParameter {

    private Trees trees;
    private TreeMaker make;
    private Name.Table names;
    private Context context;
    private RoundEnvironment roundEnv;
    private JCHelp jcHelp;
    private FluentValidatorCode fluentValidatorCode;
    private FluentException fluentException;

    public InlinerParameter(Trees trees, TreeMaker make, Name.Table names, Context context, RoundEnvironment roundEnv) {
        this.trees = trees;
        this.make = make;
        this.names = names;
        this.context = context;
        this.roundEnv = roundEnv;
        this.jcHelp = new JCHelp(make, names);
        this.fluentValidatorCode = new FluentValidatorCode(make, names);
        this.fluentException = new FluentException(make, names);
    }

    public void process() {
        List<TypeElement> annoationElement = findAnnoationElement(roundEnv);
        //遍历所有元素,修改代码
        for (TypeElement item : annoationElement) {
            JCTree tree = (JCTree) trees.getTree(item);
            TreeTranslator visitor = new Inliner();
            tree.accept(visitor);
        }
    }

    /**
     * 查询所有带有{@link Validated ConfigValue注解的}
     *
     * @param roundEnvironment 对象
     * @return List<TypeElement>
     */
    private List<TypeElement> findAnnoationElement(RoundEnvironment roundEnvironment) {
        List<TypeElement> targetClassMap = new ArrayList<>();
        //找到所有跟Validated注解相关元素
        Collection<? extends Element> anLogSet = roundEnvironment.getElementsAnnotatedWith(Validated.class);
        HashSet<String> hstrings = new HashSet<>();
        //遍历所有元素
        for (Element e : anLogSet) {
            //必须要求在方法参数上
            if (e.getKind() != ElementKind.PARAMETER) {
                continue;
            }
            Element owner = (Element) ((Symbol.VarSymbol) e).owner;
            String methodname = owner.getSimpleName().toString();
            //去重
            if (!hstrings.contains(methodname)) {
                hstrings.add(methodname);
                //此处找到的是类的描述类型
                TypeElement enclosingElement = (TypeElement) owner.getEnclosingElement();
                //对类做一个缓存
                targetClassMap.add(enclosingElement);
            }
        }
        return targetClassMap;
    }

    /**
     * 修改代码
     */
    private class Inliner extends TreeTranslator {

        /**
         * 修改方法
         *
         * @param jcMethodDecl 对象
         */
        @Override
        public void visitMethodDef(JCTree.JCMethodDecl jcMethodDecl) {
            super.visitMethodDef(jcMethodDecl);
            //过滤特点的方法
            if (jcMethodDecl.getName().toString().equals("<init>")) {
                return;
            }

            //region 查询方法上面所有参数。将带有Validated注解过滤出来
            HashMap<Name, JCTree.JCExpression> hashMap = new LinkedHashMap<>();
            //参数上面的注解
            List<JCTree.JCAnnotation> jcAnnotations = new ArrayList<>();
            //所有参数
            List<JCTree.JCVariableDecl> jcVariableDecls = new ArrayList<>();
            for (JCTree.JCVariableDecl jcVariableDecl : jcMethodDecl.getParameters()) {
                //遍历所有注解
                for (JCTree.JCAnnotation jcAnnotation : jcVariableDecl.mods.annotations) {
                    if (jcAnnotation.attribute.type.toString().equals(Validated.class.getName())) {
                        hashMap.put(jcVariableDecl.name, jcVariableDecl.vartype);
                    } else {
                        jcAnnotations.add(jcAnnotation);
                    }
                }
                //去除@Validated，防止重复的扫描,塞回其他的注解
                jcVariableDecl.mods.annotations = com.sun.tools.javac.util.List.from(jcAnnotations);
                jcVariableDecls.add(jcVariableDecl);
            }
            if (hashMap.size() < 1) {
                return;
            }
            //endregion

            //region 构建代码 fluentValidator.on("", new HibernateValidator<String>().annotationvalidate()).on().on()
            JCTree.JCMethodInvocation applyitem = fluentValidatorCode.getFluentValidator();
            for (Map.Entry<Name, JCTree.JCExpression> entry : hashMap.entrySet()) {
                //是否是基本类型
                //entry.getValue().type.isPrimitive();
                //基本类型以及包装类型是不会参与验证,编译是通过的
                /*
                 * 构建代码  new HibernateValidator<String>()
                 */
                final JCTree.JCExpression hibernateValidator = fluentValidatorCode.getHibernateValidator();
                /*
                 * 构建代码 new HibernateValidator<String>().validator()
                 */
                JCTree.JCMethodInvocation validator2 = make.Apply(
                        com.sun.tools.javac.util.List.nil(),
                        make.Select(hibernateValidator, names.fromString("validator")),
                        com.sun.tools.javac.util.List.nil()
                );
                /**
                 * fluentValidator.on().on()
                 */
                JCTree.JCMethodInvocation apply3 = make.Apply(
                        com.sun.tools.javac.util.List.nil(),
                        make.Select(applyitem, names.fromString("on")),
                        com.sun.tools.javac.util.List.of(
                                make.Ident(entry.getKey()),
                                validator2
                        )
                );
                applyitem = apply3;
            }
            //endregion

            //region 构建代码 ComplexResult complexresultapt = fluentValidator.on("", new HibernateValidator<String>().annotationvalidate()).on().on().doValidate().result(toComplex());
            // ComplexResult complexresultapt =
            JCTree.JCVariableDecl jcVariableDecl2 = make.VarDef(
                    make.Modifiers(0),
                    names.fromString("complexresultapt"),
                    jcHelp.selectFieldAccess("com.baidu.unbiz.fluentvalidator.ComplexResult"),//类型
                    fluentValidatorCode.getComplexResult(applyitem)
            );
            //endregion

            //构建错误
            final JCTree.JCStatement exception = fluentException.getException();
            final JCTree.JCIf isSuccess = fluentValidatorCode.getIsSuccess(exception);


            //复制方法内其他的代码，插入新的代码，重新构建方法
            List<JCTree.JCStatement> jcStatements = new ArrayList<>();
            jcStatements.add(jcVariableDecl2);
            jcStatements.add(isSuccess);
            //用于接口的时候代码为空
            if (jcMethodDecl.body != null) {
                for (JCTree.JCStatement jcStatement : jcMethodDecl.body.stats) {
                    jcStatements.add(jcStatement);
                }
                JCTree.JCBlock bodyBlock = make.Block(0, com.sun.tools.javac.util.List.from(jcStatements));
                JCTree.JCMethodDecl methodDecl = make.MethodDef(
                        jcMethodDecl.getModifiers(),
                        jcMethodDecl.name,
                        jcMethodDecl.restype,
                        jcMethodDecl.getTypeParameters(),
                        //jcMethodDecl.getParameters(),
                        com.sun.tools.javac.util.List.from(jcVariableDecls),
                        jcMethodDecl.getThrows(),
                        bodyBlock,//jcMethodDecl.getBody(),
                        jcMethodDecl.defaultValue);
                this.result = methodDecl;
            }
        }

    }
}
