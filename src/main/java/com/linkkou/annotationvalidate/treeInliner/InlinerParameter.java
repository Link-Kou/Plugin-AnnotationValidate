package com.linkkou.annotationvalidate.treeInliner;

import com.linkkou.annotationvalidate.Validated;
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

    public InlinerParameter(Trees trees, TreeMaker make, Name.Table names, Context context, RoundEnvironment roundEnv) {
        this.trees = trees;
        this.make = make;
        this.names = names;
        this.context = context;
        this.roundEnv = roundEnv;
        this.jcHelp = new JCHelp(make, names);
        this.fluentValidatorCode = new FluentValidatorCode(make, names);
    }

    public void process() {
        List<TypeElement> annoationElement = findAnnoationElement(roundEnv);
        //遍历所有元素
        for (TypeElement item : annoationElement) {
            JCTree tree = (JCTree) trees.getTree(item);
            TreeTranslator visitor = new Inliner();
            tree.accept(visitor);
        }
    }

    /**
     * 查询所有带有{@link Validated ConfigValue注解的}
     *
     * @param roundEnvironment
     * @return
     */
    private List<TypeElement> findAnnoationElement(RoundEnvironment roundEnvironment) {
        List<TypeElement> targetClassMap = new ArrayList<>();
        //找到所有跟AnDataCollect注解相关元素
        Collection<? extends Element> anLogSet = roundEnvironment.getElementsAnnotatedWith(Validated.class);
        HashSet<String> hstrings = new HashSet<>();
        //遍历所有元素
        for (Element e : anLogSet) {
            //方法参数上
            if (e.getKind() != ElementKind.PARAMETER) {
                continue;
            }
            Element owner = (Element) ((Symbol.VarSymbol) e).owner;
            String methodname = owner.getSimpleName().toString();
            if (!hstrings.contains(methodname)) {
                hstrings.add(methodname);
                //此处找到的是类的描述类型，因为我们的AnDataCollect的注解描述是method的所以closingElement元素是类
                TypeElement enclosingElement = (TypeElement) owner.getEnclosingElement();
                //对类做一个缓存
                targetClassMap.add(enclosingElement);
            }
        }
        return targetClassMap;
    }

    private class Inliner extends TreeTranslator {

        @Override
        public void visitMethodDef(JCTree.JCMethodDecl jcMethodDecl) {
            super.visitMethodDef(jcMethodDecl);

            //过滤特点的方法
            if (jcMethodDecl.getName().toString().equals("<init>")) {
                return;
            }

            HashMap<Name, JCTree.JCExpression> hashMap = new LinkedHashMap<>();
            List<JCTree.JCAnnotation> jcAnnotations = new ArrayList<>();
            List<JCTree.JCVariableDecl> jcVariableDecls = new ArrayList<>();
            for (JCTree.JCVariableDecl jcVariableDecl : jcMethodDecl.getParameters()) {
                for (JCTree.JCAnnotation jcAnnotation : jcVariableDecl.mods.annotations) {
                    if (jcAnnotation.attribute.type.toString().equals(Validated.class.getName())) {
                        hashMap.put(jcVariableDecl.name, jcVariableDecl.vartype);
                    } else {
                        jcAnnotations.add(jcAnnotation);

                    }
                }
                //去除@Validated，防止重复的扫描
                jcVariableDecl.mods.annotations = com.sun.tools.javac.util.List.from(jcAnnotations);
                jcVariableDecls.add(jcVariableDecl);
            }

            if (hashMap.size() < 1) {
                return;
            }


            //region 构建代码 fluentValidator.on("", new HibernateValidator<String>().annotationvalidate()).on().on()
            JCTree.JCMethodInvocation applyitem = fluentValidatorCode.getFluentValidator();

            for (Map.Entry<Name, JCTree.JCExpression> entry : hashMap.entrySet()) {
                /*
                 * 构建代码  new HibernateValidator<String>()
                 */

                JCTree.JCExpression loggerNewClass = make.NewClass(null,
                        null,
                        //类名称 会自己导入包
                        make.TypeApply(
                                jcHelp.selectFieldAccess("com.linkkou.annotationvalidate.fluentValidator.HibernateValidator"),
                                com.sun.tools.javac.util.List.of(entry.getValue())),
                        //参数
                        com.sun.tools.javac.util.List.nil(),
                        null);

                /*
                 * 构建代码 new HibernateValidator<String>().validator()
                 */
                JCTree.JCMethodInvocation validator2 = make.Apply(
                        com.sun.tools.javac.util.List.nil(),
                        make.Select(loggerNewClass, names.fromString("validator")),
                        com.sun.tools.javac.util.List.nil()
                );

                /**
                 * fluentValidator.on().on()
                 */
                JCTree.JCMethodInvocation apply3 = make.Apply(
                        com.sun.tools.javac.util.List.nil(),
                        make.Select(applyitem, names.fromString("on")),
                        com.sun.tools.javac.util.List.of(
                                //make.Literal(entry.getKey().toString()),
                                make.Ident(entry.getKey()),
                                validator2
                        )
                );
                applyitem = apply3;
            }
            //endregion

            //region 构建代码 ComplexResult complexresultapt = fluentValidator.on("", new HibernateValidator<String>().annotationvalidate()).on().on().doValidate().result(toComplex());
            /*
             *
             */
            // ComplexResult complexresultapt =
            JCTree.JCVariableDecl jcVariableDecl2 = make.VarDef(
                    make.Modifiers(0),
                    names.fromString("complexresultapt"),
                    jcHelp.selectFieldAccess("com.baidu.unbiz.fluentvalidator.ComplexResult"),//类型
                    fluentValidatorCode.getComplexResult(applyitem)
            );
            //endregion


            JCTree.JCStatement block = make.Throw(make.NewClass(null,
                    null,
                    //类名称 会自己导入包
                    make.Select(make.Select(make.Ident(names.fromString("javax")), names.fromString("validation")), names.fromString("ValidationException"))
                    ,
                    //参数
                    com.sun.tools.javac.util.List.of(make.Literal("Validation Is Error")),
                    null)
            );
            //系统类型
            if (!(jcMethodDecl.restype instanceof JCTree.JCPrimitiveTypeTree)) {
                if (jcMethodDecl.restype instanceof JCTree.JCIdent) {
                    if (!jcMethodDecl.restype.type.toString().startsWith("java.lang")) {
                        //根据返回对象构建 new 方法返回类型(rex)
                        block = make.Block(0, com.sun.tools.javac.util.List.of(make.Return(make.NewClass(null,
                                null,
                                //类名称 会自己导入包
                                jcMethodDecl.restype,
                                //参数
                                com.sun.tools.javac.util.List.of(make.Ident(names.fromString("complexresultapt"))),
                                null))));
                    }
                }
            }
            /*
             * if (!ret.isSuccess()) { System.out.print("123ßß"); }
             */
            JCTree.JCIf anIf = make.If(make.Parens(make.Unary(JCTree.Tag.NOT, make.Apply(
                    com.sun.tools.javac.util.List.nil(),
                    make.Select(make.Ident(names.fromString("complexresultapt")), names.fromString("isSuccess")),
                    com.sun.tools.javac.util.List.nil()
            ))), block, null);


            //执行代码纯文本构建完毕后需要执行 make.Exec(apply2);

            /**
             * 复制方法内其他的代码，插入新的代码，重新构建方法
             */
            List<JCTree.JCStatement> jcStatements = new ArrayList<>();
            jcStatements.add(jcVariableDecl2);
            jcStatements.add(anIf);
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
