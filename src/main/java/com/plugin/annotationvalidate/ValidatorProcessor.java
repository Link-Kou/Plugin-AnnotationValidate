package com.plugin.annotationvalidate;


import com.sun.source.util.Trees;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.tree.TreeTranslator;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.Name;
import com.sun.tools.javac.util.Names;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;

import java.util.*;

/**
 * APT
 *
 * @author LK
 * @date 2018-05-31 10:46
 */
@SupportedAnnotationTypes({"com.plugin.annotationvalidate.Validated"})
@SupportedSourceVersion(SourceVersion.RELEASE_7)
public class ValidatorProcessor extends AbstractProcessor {

    private Trees trees;
    private TreeMaker make;
    private Name.Table names;
    private Context context;

    private final HashMap<String, List<ExecutableElement>> remainingMapperTypes = new HashMap<String, List<ExecutableElement>>();


    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        trees = Trees.instance(processingEnv);
        context = ((JavacProcessingEnvironment)
                processingEnv).getContext();
        make = TreeMaker.instance(context);
        names = Names.instance(context).table;
    }

    /**
     * java版本支持
     *
     * @return
     */
    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    /**
     * 值生成针对 Validated 生成的注解类
     */
    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return Collections.singleton(Validated.class.getCanonicalName());
    }


    /**
     * 执行方法
     * {@inheritDoc}
     *
     * @param annotations
     * @param roundEnv
     */
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        List<TypeElement> annoationElement = findAnnoationElement(roundEnv);
        //遍历所有元素
        for (TypeElement item : annoationElement) {
            JCTree tree = (JCTree) trees.getTree(item);
            TreeTranslator visitor = new Inliner();
            tree.accept(visitor);
        }
        return true;
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
                    if (jcAnnotation.attribute.type.toString().equals("com.plugin.annotationvalidate.Validated")) {
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


            //region 构建代码 com.baidu.unbiz.fluentvalidator.FluentValidator.checkAll().failFast();
            /*
             * 构建代码 com.baidu.unbiz.fluentvalidator.FluentValidator.checkAll;
             */
            JCTree.JCFieldAccess getCheckAllCall = make.Select(
                    make.Select(
                            make.Select(
                                    make.Select(
                                            make.Select(
                                                    make.Ident(names.fromString("com")), names.fromString("baidu"))
                                            , names.fromString("unbiz")
                                    ), names.fromString("fluentvalidator")
                            ), names.fromString("FluentValidator")),
                    names.fromString("checkAll")
            );

            /*
             * 构建代码 com.baidu.unbiz.fluentvalidator.FluentValidator.checkAll();
             */
            JCTree.JCMethodInvocation apply = make.Apply(
                    com.sun.tools.javac.util.List.nil(),
                    getCheckAllCall,
                    com.sun.tools.javac.util.List.nil()
            );

            /*
             * 构建代码 com.baidu.unbiz.fluentvalidator.FluentValidator.checkAll().failFast();
             */
            JCTree.JCMethodInvocation apply2 = make.Apply(
                    com.sun.tools.javac.util.List.nil(),
                    make.Select(apply, names.fromString("failFast")),
                    com.sun.tools.javac.util.List.nil()
            );
            //endregion


            //region 构建代码 fluentValidator.on("", new HibernateValidator<String>().annotationvalidate()).on().on()
            JCTree.JCMethodInvocation applyitem = apply2;
            for (Map.Entry<Name, JCTree.JCExpression> entry : hashMap.entrySet()) {
                /*
                 * 构建代码  new HibernateValidator<String>()
                 */
                JCTree.JCExpression loggerNewClass = make.NewClass(null,
                        null,
                        //类名称 会自己导入包
                        make.TypeApply(make.Select(
                                make.Select(
                                        make.Select(make.Ident(names.fromString("com")), names.fromString("plugin"))
                                        , names.fromString("annotationvalidate")
                                )
                                , names.fromString("HibernateValidator")
                        ), com.sun.tools.javac.util.List.of(entry.getValue())),
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


            //region 构建代码 fluentValidator.on("", new HibernateValidator<String>().annotationvalidate()).on().on().doValidate().result(toComplex());
            /*
             * .doValidate()
             */
            JCTree.JCMethodInvocation apply4 = make.Apply(
                    com.sun.tools.javac.util.List.nil(),
                    make.Select(applyitem, names.fromString("doValidate")),
                    com.sun.tools.javac.util.List.nil()
            );

            /*
             * toComplex()
             */
            JCTree.JCFieldAccess getLoggerCall4 = make.Select(
                    make.Select(
                            make.Select(
                                    make.Select(
                                            make.Select(
                                                    make.Ident(names.fromString("com")), names.fromString("baidu"))
                                            , names.fromString("unbiz")
                                    ), names.fromString("fluentvalidator")
                            ), names.fromString("ResultCollectors")),
                    names.fromString("toComplex")
            );

            /*
             * .doValidate().result(toComplex())
             */
            JCTree.JCMethodInvocation apply5 = make.Apply(
                    com.sun.tools.javac.util.List.nil(),
                    make.Select(apply4, names.fromString("result")),
                    com.sun.tools.javac.util.List.of(
                            make.Apply(
                                    com.sun.tools.javac.util.List.nil(),
                                    getLoggerCall4,
                                    com.sun.tools.javac.util.List.nil()
                            )
                    )
            );
            //endregion


            //region 构建代码 ComplexResult complexresultapt = fluentValidator.on("", new HibernateValidator<String>().annotationvalidate()).on().on().doValidate().result(toComplex());
            /*
             *
             */
            JCTree.JCFieldAccess getLoggerCall3 = make.Select(
                    make.Select(
                            make.Select(
                                    make.Select(
                                            make.Ident(names.fromString("com")), names.fromString("baidu"))
                                    , names.fromString("unbiz")
                            ), names.fromString("fluentvalidator")
                    ), names.fromString("ComplexResult")
            );
            // ComplexResult complexresultapt =
            JCTree.JCVariableDecl jcVariableDecl2 = make.VarDef(
                    make.Modifiers(0),
                    names.fromString("complexresultapt"),
                    getLoggerCall3,//类型
                    apply5
            );
            //endregion


            JCTree.JCStatement block = make.Throw(make.NewClass(null,
                    null,
                    //类名称 会自己导入包
                    make.Select(make.Select(make.Ident(names.fromString("java")), names.fromString("lang")), names.fromString("IllegalStateException"))
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
                                jcMethodDecl.restype
                                ,
                                //参数
                                com.sun.tools.javac.util.List.of(make.Ident(names.fromString("complexresultapt"))),
                                null))));
                    }
                }
                /*
                 * 返回代码
                 */
                /*JCTree.JCMethodInvocation apply6 = make.Apply(
                        com.sun.tools.javac.util.List.nil(),
                        make.Select(make.Select(make.Ident(names.fromString("System")), names.fromString("out")), names.fromString("print")),
                        com.sun.tools.javac.util.List.of(make.Literal(""))
                );
                block = make.Block(0, com.sun.tools.javac.util.List.of(make.Exec(apply6)));*/
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
            if(jcMethodDecl.body != null){
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