package com.linkkou.annotationvalidate.treeInliner;

import com.baidu.unbiz.fluentvalidator.ComplexResult;
import com.linkkou.annotationvalidate.Validated;
import com.linkkou.annotationvalidate.utils.FluentValidatorCode;
import com.linkkou.annotationvalidate.utils.JCHelp;
import com.sun.source.tree.Tree;
import com.sun.source.util.Trees;
import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.TypeTag;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.tree.TreeTranslator;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.Name;

import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author lk
 * @version 1.0
 * @date 2020/4/11 22:25
 */
public class InlinerMethod {

    private Trees trees;
    private TreeMaker make;
    private Name.Table names;
    private Context context;
    private RoundEnvironment roundEnv;

    private JCHelp jcHelp;
    private FluentValidatorCode fluentValidatorCode;

    public InlinerMethod(Trees trees, TreeMaker make, Name.Table names, Context context, RoundEnvironment roundEnv) {
        this.trees = trees;
        this.make = make;
        this.names = names;
        this.context = context;
        this.roundEnv = roundEnv;
        this.jcHelp = new JCHelp(make, names);
        this.fluentValidatorCode = new FluentValidatorCode(make, names);
    }

    public void process() {
        final List<Symbol> annoationElementToClass = findAnnoationElementToClass(roundEnv);
        final List<Element> annoationElement = findAnnoationElement(roundEnv);
        //遍历所有元素
        for (Element item : annoationElementToClass) {
            JCTree tree = (JCTree) trees.getTree(item);
            TreeTranslator visitor = new Inliner(annoationElement);
            tree.accept(visitor);
        }
    }

    /**
     * 查询所有带有{@link Validated ConfigValue注解的}
     *
     * @param roundEnvironment
     * @return
     */
    private List<Symbol> findAnnoationElementToClass(RoundEnvironment roundEnvironment) {
        List<Symbol> targetClassMap = new ArrayList<>();
        //找到所有跟AnDataCollect注解相关元素
        Collection<? extends Element> anLogSet = roundEnvironment.getElementsAnnotatedWith(Validated.class);
        HashSet<String> hstrings = new HashSet<>();
        //遍历所有元素
        for (Element e : anLogSet) {
            //方法参数上
            if (e.getKind() != ElementKind.METHOD) {
                continue;
            }

            //region class
            final Symbol enclosingElement = ((Symbol.MethodSymbol) e).owner;
            final String namepath = enclosingElement.toString();
            if (!hstrings.contains(namepath)) {
                hstrings.add(namepath);
                targetClassMap.add(enclosingElement);
            }
            //endregion

        }
        return targetClassMap;
    }

    /**
     * 查询所有带有{@link Validated ConfigValue注解的}
     *
     * @param roundEnvironment
     * @return
     */
    private List<Element> findAnnoationElement(RoundEnvironment roundEnvironment) {
        List<Element> targetClassMap = new ArrayList<>();
        //找到所有跟AnDataCollect注解相关元素
        Collection<? extends Element> anLogSet = roundEnvironment.getElementsAnnotatedWith(Validated.class);
        //遍历所有元素
        for (Element e : anLogSet) {
            //方法参数上
            if (e.getKind() != ElementKind.METHOD) {
                continue;
            }
            targetClassMap.add(e);
        }
        return targetClassMap;
    }

    /**
     * 文本构建FieldAccess对象
     */
    private JCTree.JCFieldAccess buildJCFieldAccess(String path) {
        final String com = path;
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

    private class Inliner extends TreeTranslator {

        final List<Element> annoationElement;

        public Inliner(List<Element> annoationElement) {
            this.annoationElement = annoationElement;
        }

        @Override
        public void visitClassDef(JCTree.JCClassDecl jcClassDecl) {
            BuildCode buildCode = new BuildCode(jcClassDecl);
            buildCode.CodeStructure();

            final com.sun.tools.javac.util.List<Object> of = com.sun.tools.javac.util.List.nil();
            for (JCTree tree : jcClassDecl.defs) {
                if (tree instanceof JCTree.JCMethodDecl) {
                    final JCTree.JCMethodDecl jcMethodDecl = (JCTree.JCMethodDecl) tree;
                    for (Element element : annoationElement) {
                        if (element.equals(jcMethodDecl.sym)) {
                            JCTree.JCVariableDecl jcVariableDecl2 = buildCode.CodeNewThis(jcMethodDecl);
                            //获取所有参数
                            List<JCTree.JCExpression> accesses = new ArrayList<>();
                            List<String> fieldName = new ArrayList<>();
                            accesses.add(make.Literal(jcMethodDecl.getName().toString()));
                            for (JCTree.JCVariableDecl param : jcMethodDecl.params) {
                                fieldName.add(param.name.toString());
                                final Name name = ((JCTree.JCIdent) param.vartype).name;
                                accesses.add(buildJCFieldAccess(name.toString() + ".class"));
                            }
                            List<JCTree.JCStatement> jcStatements = new ArrayList<>();
                            jcStatements.add(make.Block(0, com.sun.tools.javac.util.List.of(jcVariableDecl2, buildCode.CodeComplexResult(fieldName, accesses))));
                            if (jcMethodDecl.body != null) {
                                for (JCTree.JCStatement jcStatement : jcMethodDecl.body.stats) {
                                    jcStatements.add(jcStatement);
                                }
                                JCTree.JCBlock bodyBlock = make.Block(0, com.sun.tools.javac.util.List.from(jcStatements));
                                JCTree.JCMethodDecl methodDecl = make.MethodDef(
                                        jcMethodDecl.getModifiers(),
                                        jcMethodDecl.name,
                                        //names.fromString("vvve"),
                                        jcMethodDecl.restype,
                                        jcMethodDecl.getTypeParameters(),
                                        jcMethodDecl.getParameters(),
                                        jcMethodDecl.getThrows(),
                                        bodyBlock,//jcMethodDecl.getBody(),
                                        jcMethodDecl.defaultValue);
                                //this.result = methodDecl;
                                final com.sun.tools.javac.util.List<?> from = com.sun.tools.javac.util.List.from(jcClassDecl.defs.stream().filter(x -> !x.equals(jcMethodDecl)).toArray());
                                jcClassDecl.defs = (com.sun.tools.javac.util.List<JCTree>) from;
                                jcClassDecl.defs = jcClassDecl.defs.append(methodDecl);
                            }
                        }
                    }
                }
            }
            super.visitClassDef(jcClassDecl);
        }
    }

    private class BuildCode {

        private JCTree.JCClassDecl jcClassDecl;

        public BuildCode(JCTree.JCClassDecl jcClassDecl) {
            this.jcClassDecl = jcClassDecl;
        }

        /**
         * 查询Class是否存在无参数构造
         * 没有就创建无参数构造方法
         */
        public void CodeStructure() {
            boolean checkInit = false;
            for (JCTree tree : jcClassDecl.defs) {
                if (tree.getKind().equals(Tree.Kind.METHOD)) {
                    final JCTree.JCMethodDecl tree1 = (JCTree.JCMethodDecl) tree;
                    if (tree1.getName().toString().equals("<init>")) {
                        if (null == tree1.restype && tree1.params.size() == 0) {
                            checkInit = true;
                        }
                    }
                }
            }
            if (!checkInit) {
                jcClassDecl.defs = jcClassDecl.defs.prepend(make.MethodDef(
                        make.Modifiers(Flags.PUBLIC),// public方法
                        names.fromString("<init>"),// 方法名称
                        null,// 方法返回的类型
                        com.sun.tools.javac.util.List.nil(),// 泛型参数
                        com.sun.tools.javac.util.List.nil(),// 方法参数
                        com.sun.tools.javac.util.List.nil(),// throw表达式
                        make.Block(0, com.sun.tools.javac.util.List.nil()),// 方法体
                        null// 默认值
                ));
                //this.result = jcClassDecl;
            }
        }

        /**
         * 对方法所在类进行初始化
         */
        public JCTree.JCVariableDecl CodeNewThis(JCTree.JCMethodDecl jcMethodDecl) {
            final JCTree.JCNewClass jcNewClass = make.NewClass(null,
                    null,
                    //类名称 会自己导入包
                    make.Ident(names.fromString(jcMethodDecl.sym.owner.name.toString())),
                    //参数
                    com.sun.tools.javac.util.List.nil(),
                    //com.sun.tools.javac.util.List.of(make.Literal(TypeTag.BOT, null), make.Literal(TypeTag.BOT, null)),
                    null);

            JCTree.JCVariableDecl jcVariableDecl2 = make.VarDef(
                    make.Modifiers(0),
                    names.fromString("jcVariableDecl2"),
                    //类型
                    make.Ident(names.fromString(jcMethodDecl.sym.owner.name.toString())),
                    jcNewClass
            );
            return jcVariableDecl2;
        }

        /**
         * 构建校验代码
         *
         * @param fieldName 参数
         * @return
         */
        public JCTree.JCBlock CodeComplexResult(List<String> fieldName, List<JCTree.JCExpression> accesses) {

            /*
             * 构建代码  new HibernateValidator<String>()
             */
            JCTree.JCExpression loggerNewClass = make.NewClass(null,
                    null,
                    //类名称 会自己导入包
                    make.TypeApply(
                            jcHelp.selectFieldAccess("com.linkkou.annotationvalidate.fluentValidator.HibernateValidator"),
                            com.sun.tools.javac.util.List.nil()),
                    //参数
                    com.sun.tools.javac.util.List.nil(),
                    null);


            List<JCTree.JCIdent> jcLiterals = new ArrayList<>();
            for (String s : fieldName) {
                jcLiterals.add(make.Ident(names.fromString(s)));
            }
            JCTree.JCExpression newObjs = make.NewArray(
                    make.Ident(names.fromString("Object")),
                    com.sun.tools.javac.util.List.nil(),
                    com.sun.tools.javac.util.List.from(jcLiterals)
            );
            accesses.add(0, newObjs);
            /*
             * 构建代码 new HibernateValidator<String>().validator()
             */
            JCTree.JCMethodInvocation validator2 = make.Apply(
                    com.sun.tools.javac.util.List.nil(),
                    make.Select(loggerNewClass, names.fromString("ValidateParameters")),
                    com.sun.tools.javac.util.List.from(
                            //make.Literal(entry.getKey().toString()),
                            //make.Ident(names.fromString("methods")),
                            //newObjs,
                            accesses
                    )
            );

            JCTree.JCMethodInvocation applyitem = fluentValidatorCode.getFluentValidator();

            /**
             * fluentValidator.on().on()
             */
            JCTree.JCMethodInvocation apply3 = make.Apply(
                    com.sun.tools.javac.util.List.nil(),
                    make.Select(applyitem, names.fromString("on")),
                    com.sun.tools.javac.util.List.of(
                            //make.Literal(entry.getKey().toString()),
                            make.Ident(names.fromString("jcVariableDecl2")),
                            validator2
                    )
            );

            JCTree.JCVariableDecl jcVariableDecl4 = make.VarDef(
                    make.Modifiers(0),
                    names.fromString("complexresultapt"),
                    jcHelp.selectFieldAccess("com.baidu.unbiz.fluentvalidator.ComplexResult"),//类型
                    fluentValidatorCode.getComplexResult(apply3)
            );

            JCTree.JCStatement blockif = make.Throw(make.NewClass(null,
                    null,
                    //类名称 会自己导入包
                    make.Select(make.Select(make.Ident(names.fromString("javax")), names.fromString("validation")), names.fromString("ValidationException"))
                    ,
                    //参数
                    com.sun.tools.javac.util.List.of(make.Literal("Validation Is Error")),
                    null)
            );
            /*
             * if (!ret.isSuccess()) { System.out.print("123ßß"); }
             */
            JCTree.JCIf anIf = make.If(make.Parens(make.Unary(JCTree.Tag.NOT, make.Apply(
                    com.sun.tools.javac.util.List.nil(),
                    make.Select(make.Ident(names.fromString("complexresultapt")), names.fromString("isSuccess")),
                    com.sun.tools.javac.util.List.nil()
            ))), blockif, null);

            return make.Block(0, com.sun.tools.javac.util.List.of(jcVariableDecl4, anIf));

        }

        /**
         * 构建 getMethod
         *
         * @param jcMethodDecl
         */
        @Deprecated
        public void CodegetMethod(JCTree.JCMethodDecl jcMethodDecl) {

            final JCTree.JCMethodInvocation apply = make.Apply(com.sun.tools.javac.util.List.nil(),
                    buildJCFieldAccess("jcVariableDecl2.getClass"),
                    com.sun.tools.javac.util.List.nil());

            //获取所有参数
            List<JCTree.JCExpression> accesses = new ArrayList<>();
            List<String> fieldName = new ArrayList<>();
            accesses.add(make.Literal(jcMethodDecl.getName().toString()));
            for (JCTree.JCVariableDecl param : jcMethodDecl.params) {
                fieldName.add(param.name.toString());
                final Name name = ((JCTree.JCIdent) param.vartype).name;
                accesses.add(buildJCFieldAccess(name.toString() + ".class"));
            }

            final JCTree.JCMethodInvocation getMethod = make.Apply(com.sun.tools.javac.util.List.nil(),
                    make.Select(apply, names.fromString("getMethod")),
                    com.sun.tools.javac.util.List.from(
                            accesses
                    ));
            JCTree.JCVariableDecl jcVariableDecl3 = make.VarDef(
                    make.Modifiers(0),
                    names.fromString("methods"),
                    buildJCFieldAccess("java.lang.reflect.Method"),//类型
                    getMethod
            );


            final JCTree.JCVariableDecl jcVariableDecl = make.VarDef(make.Modifiers(Flags.ReceiverParamFlags), names.fromString("e"), make.Ident(names.fromString("Exception")), null);
            final JCTree.JCMethodInvocation apply1 = make.Apply(com.sun.tools.javac.util.List.nil(),
                    buildJCFieldAccess("e.printStackTrace"),
                    com.sun.tools.javac.util.List.nil());
            final JCTree.JCBlock block = make.Block(0, com.sun.tools.javac.util.List.of(make.Exec(apply1)));
            final JCTree.JCCatch aCatch = make.Catch(jcVariableDecl, block);


            JCTree.JCBlock blocks = make.Block(0, com.sun.tools.javac.util.List.of(jcVariableDecl3));
            final JCTree.JCTry aTry = make.Try(blocks, com.sun.tools.javac.util.List.of(aCatch), null);

        }
    }

}
