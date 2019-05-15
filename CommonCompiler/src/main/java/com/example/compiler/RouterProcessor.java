package com.example.compiler;

import com.chenxf.annotation.RouterMap;
import com.google.auto.service.AutoService;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.WildcardTypeName;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;

/**
这个类，将在编译期间执行，引用本模块的其他模块，如果用了RouterMap注解，则会生成一个类，在
 build/generated/source/apt/debug/...
 生成的类，可以直接被使用
 */
@AutoService(Processor.class)
public class RouterProcessor extends AbstractProcessor {
    private static final String DEFAULT_REGISTER = "chenxf://router/";//请把chenxf换成你公司的名字，好看多了

    private Map<String, TypeElement> mRouterSchemes = new LinkedHashMap<>();
    private Map<String, TypeElement> mRegistryIds = new LinkedHashMap<>();

    private Filer mFiler;
    private Elements elementUtils;
    private String targetModuleName = "";
    private Messager mMessager;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        mFiler = processingEnv.getFiler();
        elementUtils = processingEnv.getElementUtils();
        mMessager = processingEnv.getMessager();
        Map<String, String> map = processingEnv.getOptions();
        Set<String> keys = map.keySet();
        for (String key : keys) {
            if ("targetModuleName".equals(key)) {
                this.targetModuleName = map.get(key);
            }
        }
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> types = new LinkedHashSet<>();
        types.add(RouterMap.class.getCanonicalName());
        return types;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        try {
            processRouterMap(roundEnv);
        } catch (ProcessingException e) {
            error(e.getElement(), e.getMessage());
        } catch (IOException e) {
            error(null, e.getMessage());
        }
        return true;
    }

    private void processRouterMap(RoundEnvironment roundEnv) throws IOException, ProcessingException {
        Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(RouterMap.class);
        TypeSpec type = getRouterTableInitializer(elements);

        if (type != null) {
            JavaFile.builder("com.chenxf.router", type).build().writeTo(mFiler);
        } else {
            info("getRouterTableInitializer return type null!");
        }
    }

    //----------generate RouterTableInitializer<ModuleName>----------
    private TypeSpec getRouterTableInitializer(Set<? extends Element> elements) throws ProcessingException {
        if (elements == null || elements.size() == 0) {
            return null;
        }

        MethodSpec.Builder initRouterBuilder = moduleInitRouterTableMethodBuilder();//准备创建initRouterTable方法，为了初始化 url -> activity对应关系
        MethodSpec.Builder initMappingBuilder = moduleInitMappingTableMethodBuilder();//准备创建initMappingTable方法，为了让一个数字，对应一个url
        for (Element element : elements) {

            if (element.getKind() != ElementKind.CLASS) {
                continue;
            }
            RouterMap router = element.getAnnotation(RouterMap.class);
            checkValidRouterMap((TypeElement) element, router);
            String routerUrl = router.value();
            String[] registryIds = router.registry();
            initRouterBuilder.addStatement("router.put($S, $T.class)", routerUrl, ClassName.get((TypeElement) element));
            for (String bizId : registryIds) {
                if (routerUrl.startsWith(DEFAULT_REGISTER)) {
                    initMappingBuilder.addStatement("mapping.put($S, $S)", bizId, routerUrl.replace(DEFAULT_REGISTER, ""));
                } else {
                    throw new ProcessingException(element, "scheme for class %s is invalid ! router scheme MUST starts with '%s'",
                            ((TypeElement) element).getQualifiedName(), DEFAULT_REGISTER);
                }
            }
        }
        //本模块不需要依赖IRouterTableInitializer，只需要使用的地方（如模块app，能依赖到IRouterTableInitializer，就可以检索到）
        TypeElement routerInitializerType = elementUtils.getTypeElement("com.chenxf.router.IRouterTableInitializer");

        TypeSpec result = TypeSpec.classBuilder("RouterTableInitializer" + targetModuleName)
                .addSuperinterface(ClassName.get(routerInitializerType))
                .addModifiers(Modifier.PUBLIC)
                .addMethod(initRouterBuilder.build())
                .addMethod(initMappingBuilder.build())
                .build();

        return result;
    }

    private void checkValidRouterMap(TypeElement element, RouterMap annotation) throws ProcessingException {
        String scheme = annotation.value();
        String[] ids = annotation.registry();
        if (mRouterSchemes.containsKey(scheme)) {
            throw new ProcessingException(element, "class %s annotated with scheme='%s' is conflict with class %s",
                    element.getQualifiedName(), scheme, mRouterSchemes.get(scheme).getQualifiedName());
        }
        mRouterSchemes.put(scheme, element);
        for (String id : ids) {
            if (mRegistryIds.containsKey(id)) {
                throw new ProcessingException(element, "class %s annotated with registry='%s' is conflict with class %s",
                        element.getQualifiedName(), id, mRegistryIds.get(id).getQualifiedName());
            }
            mRegistryIds.put(id, element);
        }
    }

    private MethodSpec.Builder moduleInitRouterTableMethodBuilder() {
        TypeElement activityType = elementUtils.getTypeElement("android.app.Activity");
        ParameterizedTypeName mapTypeName = ParameterizedTypeName
                .get(ClassName.get(Map.class), ClassName.get(String.class),
                        ParameterizedTypeName.get(ClassName.get(Class.class),
                                WildcardTypeName.subtypeOf(ClassName.get(activityType))));
        ParameterSpec mapParameterSpec = ParameterSpec.builder(mapTypeName, "router").build();
        return MethodSpec.methodBuilder("initRouterTable")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .addParameter(mapParameterSpec);
    }

    private MethodSpec.Builder moduleInitMappingTableMethodBuilder() {
        TypeName paramType = ParameterizedTypeName.get(ClassName.get(Map.class),
                ClassName.get(String.class), ClassName.get(String.class));
        return MethodSpec.methodBuilder("initMappingTable")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .addParameter(paramType, "mapping");
    }

    private void info(String msg, Object... args) {
        mMessager.printMessage(Diagnostic.Kind.NOTE, String.format(msg, args));
    }

    private void error(Element e, String msg, Object... args) {
        mMessager.printMessage(
                Diagnostic.Kind.ERROR,
                String.format(msg, args),
                e);
    }

}