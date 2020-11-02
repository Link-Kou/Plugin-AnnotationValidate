package com.linkkou.annotationvalidate.treeInliner;

import com.linkkou.annotationvalidate.Validated;
import com.linkkou.annotationvalidate.utils.FluentException;
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

/**
 * 当 {@link Validated} 用于方法上面的时候
 *
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
    private FluentException fluentException;

    public InlinerMethod(Trees trees, TreeMaker make, Name.Table names, Context context, RoundEnvironment roundEnv) {
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
     * 查询所有带有{@link Validated 注解的}
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
            //方法上
            if (e.getKind() != ElementKind.METHOD) {
                continue;
            }
            targetClassMap.add(e);
        }
        return targetClassMap;
    }

    private class Inliner extends TreeTranslator {

        final List<Element> annoationElement;

        public Inliner(List<Element> annoationElement) {
            this.annoationElement = annoationElement;
        }

        @Override
        public void visitClassDef(JCTree.JCClassDecl jcClassDecl) {
            BuildCode buildCode = new BuildCode(jcClassDecl);
            //无构造，创建构造
            buildCode.codeStructure();
            final com.sun.tools.javac.util.List<Object> of = com.sun.tools.javac.util.List.nil();
            for (JCTree tree : jcClassDecl.defs) {
                if (tree instanceof JCTree.JCMethodDecl) {
                    final JCTree.JCMethodDecl jcMethodDecl = (JCTree.JCMethodDecl) tree;
                    for (Element element : annoationElement) {
                        if (element.equals(jcMethodDecl.sym)) {
                            final String id = UUID.randomUUID().toString();
                            //创建注解
                            final JCTree.JCAnnotation annotation = make.Annotation(
                                    jcHelp.selectFieldAccess("com.linkkou.annotationvalidate.ValidatedName"),
                                    com.sun.tools.javac.util.List.of(
                                            make.Assign(make.Ident(names.fromString("value")), make.Literal(id))
                                    )
                            );
                            final JCTree.JCModifiers modifiers = jcMethodDecl.getModifiers();
                            modifiers.annotations = modifiers.annotations.append(annotation);
                            //JCTree.JCVariableDecl jcVariableDecl2 = buildCode.codeNewThis(jcMethodDecl);
                            //获取所有参数
                            List<JCTree.JCExpression> accesses = new ArrayList<>();
                            //变量名称
                            List<String> fieldName = new ArrayList<>();
                            //方法名称
                            accesses.add(make.Literal(jcMethodDecl.getName().toString()));
                            accesses.add(make.Literal(id));
                            for (JCTree.JCVariableDecl param : jcMethodDecl.params) {
                                fieldName.add(param.name.toString());
                                //accesses.add(jcHelp.selectFieldAccess(name.toString() + ".class"));
                            }
                            List<JCTree.JCStatement> jcStatements = new ArrayList<>();
                            jcStatements.add(make.Block(0, com.sun.tools.javac.util.List.of(buildCode.codeComplexResult(jcMethodDecl, fieldName, accesses))));
                            if (jcMethodDecl.body != null) {
                                for (JCTree.JCStatement jcStatement : jcMethodDecl.body.stats) {
                                    jcStatements.add(jcStatement);
                                }
                                JCTree.JCBlock bodyBlock = make.Block(0, com.sun.tools.javac.util.List.from(jcStatements));
                                JCTree.JCMethodDecl methodDecl = make.MethodDef(
                                        modifiers,
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

    /**
     * 构建代码
     */
    private class BuildCode {

        private JCTree.JCClassDecl jcClassDecl;

        public BuildCode(JCTree.JCClassDecl jcClassDecl) {
            this.jcClassDecl = jcClassDecl;
        }

        /**
         * 查询Class是否存在无参数构造
         * 没有就创建无参数构造方法
         */
        @Deprecated
        public void codeStructure() {
            boolean checkInit = false;
            for (JCTree tree : jcClassDecl.defs) {
                if (tree.getKind().equals(Tree.Kind.METHOD)) {
                    final JCTree.JCMethodDecl tree1 = (JCTree.JCMethodDecl) tree;
                    if (tree1.getName().toString().equals("<init>")) {
                        /*if (null == tree1.restype && tree1.params.size() == 0) {
                            checkInit = true;
                        }*/
                        checkInit = true;
                        break;
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
        public JCTree.JCNewClass codeNewThis(JCTree.JCMethodDecl jcMethodDecl) {
            com.sun.tools.javac.util.List<?> nil = com.sun.tools.javac.util.List.nil();
            //获取到构造参数
            for (JCTree tree : jcClassDecl.defs) {
                if (tree.getKind().equals(Tree.Kind.METHOD)) {
                    final JCTree.JCMethodDecl tree1 = (JCTree.JCMethodDecl) tree;
                    if (tree1.getName().toString().equals("<init>")) {
                        if (null == tree1.restype && tree1.params.size() == 0) {
                            nil = com.sun.tools.javac.util.List.nil();
                            break;
                        } else {
                            nil = com.sun.tools.javac.util.List.from(tree1.params.stream().map(x -> make.Literal(TypeTag.BOT, null)).toArray());
                            break;
                        }
                    }
                }
            }

            final JCTree.JCNewClass jcNewClass = make.NewClass(null,
                    null,
                    //类名称 会自己导入包
                    make.Ident(names.fromString(jcMethodDecl.sym.owner.name.toString())),
                    //参数
                    (com.sun.tools.javac.util.List<JCTree.JCExpression>) nil,
                    //com.sun.tools.javac.util.List.of(make.Literal(TypeTag.BOT, null), make.Literal(TypeTag.BOT, null)),
                    null);

            JCTree.JCVariableDecl jcVariableDecl2 = make.VarDef(
                    make.Modifiers(0),
                    names.fromString("jcVariableDecl2"),
                    //类型
                    make.Ident(names.fromString(jcMethodDecl.sym.owner.name.toString())),
                    jcNewClass
            );
            return jcNewClass;
        }

        /**
         * 构建校验代码
         *
         * @param fieldName 参数 变量
         * @param accesses  参数 变量
         * @return
         */
        public JCTree.JCBlock codeComplexResult(JCTree.JCMethodDecl jcMethodDecl, List<String> fieldName, List<JCTree.JCExpression> accesses) {
            final JCTree.JCExpression hibernateValidator = fluentValidatorCode.getHibernateValidator();
            //获取到所有变量名称
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
            final JCTree.JCFieldAccess jcFieldAccess = jcHelp.selectFieldAccess(jcMethodDecl.sym.owner.name.toString() + ".class");
            accesses.add(1, jcFieldAccess);
            //当前类的的class对象
            /*
             * 构建代码 new HibernateValidator<String>().validator()
             */
            JCTree.JCMethodInvocation validator2 = make.Apply(
                    com.sun.tools.javac.util.List.nil(),
                    make.Select(hibernateValidator, names.fromString("ValidateParameters")),
                    com.sun.tools.javac.util.List.from(
                            //make.Literal(entry.getKey().toString()),
                            //make.Ident(names.fromString("methods")),
                            //newObjs,
                            accesses
                    )
            );


            JCTree.JCMethodInvocation applyitem = fluentValidatorCode.getFluentValidator();

            //初始化New
            final JCTree.JCNewClass jcNewClass = codeNewThis(jcMethodDecl);

            /**
             * fluentValidator.on().on()
             */
            JCTree.JCMethodInvocation apply3 = make.Apply(
                    com.sun.tools.javac.util.List.nil(),
                    make.Select(applyitem, names.fromString("on")),
                    com.sun.tools.javac.util.List.of(
                            //make.Literal(entry.getKey().toString()),
                            jcNewClass,
                            validator2
                    )
            );

            JCTree.JCVariableDecl jcVariableDecl4 = make.VarDef(
                    make.Modifiers(0),
                    names.fromString("complexresultapt"),
                    jcHelp.selectFieldAccess("com.baidu.unbiz.fluentvalidator.ComplexResult"),//类型
                    fluentValidatorCode.getComplexResult(apply3)
            );

            //构建错误
            final JCTree.JCStatement exception = fluentException.getException();
            final JCTree.JCIf isSuccess = fluentValidatorCode.getIsSuccess(exception);
            return make.Block(0, com.sun.tools.javac.util.List.of(jcVariableDecl4, isSuccess));

        }

        /**
         * 构建 getMethod
         *
         * @param jcMethodDecl
         */
        @Deprecated
        public void codegetMethod(JCTree.JCMethodDecl jcMethodDecl) {

            final JCTree.JCMethodInvocation apply = make.Apply(com.sun.tools.javac.util.List.nil(),
                    jcHelp.selectFieldAccess("jcVariableDecl2.getClass"),
                    com.sun.tools.javac.util.List.nil());

            //获取所有参数
            List<JCTree.JCExpression> accesses = new ArrayList<>();
            List<String> fieldName = new ArrayList<>();
            accesses.add(make.Literal(jcMethodDecl.getName().toString()));
            for (JCTree.JCVariableDecl param : jcMethodDecl.params) {
                fieldName.add(param.name.toString());
                final Name name = ((JCTree.JCIdent) param.vartype).name;
                accesses.add(jcHelp.selectFieldAccess(name.toString() + ".class"));
            }

            final JCTree.JCMethodInvocation getMethod = make.Apply(com.sun.tools.javac.util.List.nil(),
                    make.Select(apply, names.fromString("getMethod")),
                    com.sun.tools.javac.util.List.from(
                            accesses
                    ));
            JCTree.JCVariableDecl jcVariableDecl3 = make.VarDef(
                    make.Modifiers(0),
                    names.fromString("methods"),
                    jcHelp.selectFieldAccess("java.lang.reflect.Method"),//类型
                    getMethod
            );


            final JCTree.JCVariableDecl jcVariableDecl = make.VarDef(make.Modifiers(Flags.ReceiverParamFlags), names.fromString("e"), make.Ident(names.fromString("Exception")), null);
            final JCTree.JCMethodInvocation apply1 = make.Apply(com.sun.tools.javac.util.List.nil(),
                    jcHelp.selectFieldAccess("e.printStackTrace"),
                    com.sun.tools.javac.util.List.nil());
            final JCTree.JCBlock block = make.Block(0, com.sun.tools.javac.util.List.of(make.Exec(apply1)));
            final JCTree.JCCatch aCatch = make.Catch(jcVariableDecl, block);


            JCTree.JCBlock blocks = make.Block(0, com.sun.tools.javac.util.List.of(jcVariableDecl3));
            final JCTree.JCTry aTry = make.Try(blocks, com.sun.tools.javac.util.List.of(aCatch), null);

        }
    }

}
