package com.linkkou.annotationvalidate;


import com.linkkou.annotationvalidate.treeInliner.InlinerMethod;
import com.linkkou.annotationvalidate.treeInliner.InlinerParameter;
import com.sun.source.util.Trees;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.TypeTag;
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
@SupportedAnnotationTypes({"com.linkkou.annotationvalidate.Validated"})
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
        new InlinerParameter(trees, make, names, context, roundEnv).process();
        new InlinerMethod(trees, make, names, context, roundEnv).process();
        return true;
    }






}