package com.developer.bsince.ioc.core;

import static javax.lang.model.element.ElementKind.CLASS;
import static javax.lang.model.element.ElementKind.INTERFACE;
import static javax.lang.model.element.ElementKind.METHOD;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.STATIC;
import static javax.tools.Diagnostic.Kind.ERROR;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.JavaFileObject;

import android.view.View;

import com.developer.bsince.ioc.CallMethod;
import com.developer.bsince.ioc.Callback;
import com.developer.bsince.ioc.CheckedChanged;
import com.developer.bsince.ioc.Click;
import com.developer.bsince.ioc.EditorAction;
import com.developer.bsince.ioc.FocusChange;
import com.developer.bsince.ioc.Id;
import com.developer.bsince.ioc.Ids;
import com.developer.bsince.ioc.ItemClick;
import com.developer.bsince.ioc.ItemLongClick;
import com.developer.bsince.ioc.ItemSelected;
import com.developer.bsince.ioc.LongClick;
import com.developer.bsince.ioc.PageChange;
import com.developer.bsince.ioc.ResBool;
import com.developer.bsince.ioc.ResColor;
import com.developer.bsince.ioc.ResDimen;
import com.developer.bsince.ioc.ResDrawable;
import com.developer.bsince.ioc.ResInt;
import com.developer.bsince.ioc.ResStr;
import com.developer.bsince.ioc.TextChanged;
import com.developer.bsince.ioc.Touch;


/**
 * Created by oeager on 2015/4/26.
 */
public class BsinceProcessor extends AbstractProcessor {

    public static final String SUFFIX = "$$Ejector";
    public static final String ANDROID_PREFIX = "android.";
    public static final String JAVA_PREFIX = "java.";
    static final String VIEW_TYPE = "android.view.View";
    private static final String COLOR_STATE_LIST_TYPE = "android.content.res.ColorStateList";
    private static final String DRAWABLE_TYPE = "android.graphics.drawable.Drawable";
    private static final String NULLABLE_ANNOTATION_NAME = "Nullable";
    private static final String LIST_TYPE = List.class.getCanonicalName();
    private static final List<Class<? extends Annotation>> LISTENERS = Arrays.asList(//
            CheckedChanged.class, //
            Click.class, //
            EditorAction.class, //
            FocusChange.class, //
            ItemClick.class, //
            ItemLongClick.class, //
            ItemSelected.class, //
            LongClick.class, //
            PageChange.class, //
            TextChanged.class, //
            Touch.class //
    );

    private Elements elementUtils;
    private Types typeUtils;
    private Filer filer;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        elementUtils = processingEnv.getElementUtils();
        typeUtils = processingEnv.getTypeUtils();
        filer = processingEnv.getFiler();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> types = new LinkedHashSet<String>();

        types.add(Id.class.getCanonicalName());
        types.add(Ids.class.getCanonicalName());

        for (Class<? extends Annotation> listener : LISTENERS) {
            types.add(listener.getCanonicalName());
        }

        types.add(ResBool.class.getCanonicalName());
        types.add(ResColor.class.getCanonicalName());
        types.add(ResDimen.class.getCanonicalName());
        types.add(ResDrawable.class.getCanonicalName());
        types.add(ResInt.class.getCanonicalName());
        types.add(com.developer.bsince.ioc.ResStr.class.getCanonicalName());

        return types;
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        Map<TypeElement,InjectClass> targetClassMap = findAndParseTargets(roundEnv);
        for (Map.Entry<TypeElement,InjectClass> entry :targetClassMap.entrySet()){
            TypeElement typeElement = entry.getKey();
            InjectClass injectClass = entry.getValue();
            try {
                JavaFileObject jfo = filer.createSourceFile(injectClass.getFqcn(), typeElement);
                Writer writer = jfo.openWriter();
                writer.write(injectClass.brewJava());
                writer.flush();
                writer.close();
            } catch (IOException e) {
                error(typeElement, "Unable to write view binder for type %s: %s", typeElement,
                        e.getMessage());
            }
        }
        return true;
    }

    private Map<TypeElement,InjectClass> findAndParseTargets(RoundEnvironment env){
        Map<TypeElement,InjectClass> targetClassMap = new LinkedHashMap<>();
        Set<String> erasedTargetNames = new LinkedHashSet<>();
        // Process each @Id element.
        for (Element element : env.getElementsAnnotatedWith(Id.class)) {
            try {
                parseId(element, targetClassMap, erasedTargetNames);
            } catch (Exception e) {
                logParsingError(element, Id.class, e);
            }
        }

        // Process each @Ids element.
        for (Element element : env.getElementsAnnotatedWith(Ids.class)) {
            try {
                parseIds(element, targetClassMap, erasedTargetNames);
            } catch (Exception e) {
                logParsingError(element, Ids.class, e);
            }
        }
        // Process each annotation that corresponds to a listener.
        for (Class<? extends Annotation> listener : LISTENERS) {
            findAndParseListener(env, listener, targetClassMap, erasedTargetNames);
        }

        // Process each @ResBool element.
        for (Element element : env.getElementsAnnotatedWith(ResBool.class)) {
            try {
                parseResourceBool(element, targetClassMap, erasedTargetNames);
            } catch (Exception e) {
                logParsingError(element, ResBool.class, e);
            }
        }

        // Process each @ResColor element.
        for (Element element : env.getElementsAnnotatedWith(ResColor.class)) {
            try {
                parseResourceColor(element, targetClassMap, erasedTargetNames);
            } catch (Exception e) {
                logParsingError(element, ResColor.class, e);
            }
        }

        // Process each @ResDimen element.
        for (Element element : env.getElementsAnnotatedWith(ResDimen.class)) {
            try {
                parseResourceDimen(element, targetClassMap, erasedTargetNames);
            } catch (Exception e) {
                logParsingError(element, ResDimen.class, e);
            }
        }

        // Process each @ResDrawable element.
        for (Element element : env.getElementsAnnotatedWith(ResDrawable.class)) {
            try {
                parseResourceDrawable(element, targetClassMap, erasedTargetNames);
            } catch (Exception e) {
                logParsingError(element, ResDrawable.class, e);
            }
        }

        // Process each @ResInt element.
        for (Element element : env.getElementsAnnotatedWith(ResInt.class)) {
            try {
                parseResourceInt(element, targetClassMap, erasedTargetNames);
            } catch (Exception e) {
                logParsingError(element, ResInt.class, e);
            }
        }

        // Process each @ResStr element.
        for (Element element : env.getElementsAnnotatedWith(ResStr.class)) {
            try {
                parseResourceString(element, targetClassMap, erasedTargetNames);
            } catch (Exception e) {
                logParsingError(element,ResStr.class, e);
            }
        }

        // Try to find a parent binder for each.
        for (Map.Entry<TypeElement, InjectClass> entry : targetClassMap.entrySet()) {
            String parentClassFqcn = findParentFqcn(entry.getKey(), erasedTargetNames);
            if (parentClassFqcn != null) {
                entry.getValue().setParentViewBinder(parentClassFqcn + SUFFIX);
            }
        }

        return targetClassMap;
    }
    private void parseId(Element element, Map<TypeElement, InjectClass> targetClassMap,
                               Set<String> erasedTargetNames) {
        boolean hasError = false;
        TypeElement enclosingElement = (TypeElement) element.getEnclosingElement();

        // Verify that the target type extends from View.
        TypeMirror elementType = element.asType();
        if (elementType.getKind() == TypeKind.TYPEVAR) {
            TypeVariable typeVariable = (TypeVariable) elementType;
            elementType = typeVariable.getUpperBound();
        }
        if (!isSubtypeOfType(elementType, VIEW_TYPE) && !isInterface(elementType)) {
            error(element, "@Id fields must extend from View or be an interface. (%s.%s)",
                    enclosingElement.getQualifiedName(), element.getSimpleName());
            hasError = true;
        }

        // Verify common generated code restrictions.
        hasError |= isInaccessibleViaGeneratedCode(Id.class, "fields", element);
        hasError |= isInjectInWrongPackage(Id.class, element);

        // Check for the other field annotation.
        if (element.getAnnotation(Ids.class) != null) {
            error(element, "Only one of @Id and @Ids is allowed. (%s.%s)",
                    enclosingElement.getQualifiedName(), element.getSimpleName());
            hasError = true;
        }

        if (hasError) {
            return;
        }

        // Assemble information on the field.
        int id = element.getAnnotation(Id.class).value();

        InjectClass injectClass = targetClassMap.get(enclosingElement);
        if (injectClass != null) {
            ViewInjections viewInjections = injectClass.getViewInjection(id);
            if (viewInjections != null) {
                Iterator<FieldViewInjection> iterator = viewInjections.getFieldInjections().iterator();
                if (iterator.hasNext()) {
                    FieldViewInjection existingInjection = iterator.next();
                    error(element,
                            "Attempt to use @Id for an already inject ID %d on '%s'. (%s.%s)", id,
                            existingInjection.getName(), enclosingElement.getQualifiedName(),
                            element.getSimpleName());
                    return;
                }
            }
        } else {
            injectClass = getOrCreateTargetClass(targetClassMap, enclosingElement);
        }

        String name = element.getSimpleName().toString();
        String type = elementType.toString();
        boolean required = isRequiredInjection(element);

        FieldViewInjection injection = new FieldViewInjection(name, type, required);
        injectClass.addField(id, injection);

        // Add the type-erased version to the valid inject targets set.
        erasedTargetNames.add(enclosingElement.toString());

    }

    private void parseIds(Element element, Map<TypeElement, InjectClass> targetClassMap,
                                Set<String> erasedTargetNames) {
        boolean hasError = false;
        TypeElement enclosingElement = (TypeElement) element.getEnclosingElement();

        // Verify that the type is a List or an array.
        TypeMirror elementType = element.asType();
        String erasedType = doubleErasure(elementType);
        TypeMirror viewType = null;
        FieldCollectionViewInjection.Kind kind = null;
        if (elementType.getKind() == TypeKind.ARRAY) {
            ArrayType arrayType = (ArrayType) elementType;
            viewType = arrayType.getComponentType();
            kind = FieldCollectionViewInjection.Kind.ARRAY;
        } else if (LIST_TYPE.equals(erasedType)) {
            DeclaredType declaredType = (DeclaredType) elementType;
            List<? extends TypeMirror> typeArguments = declaredType.getTypeArguments();
            if (typeArguments.size() != 1) {
                error(element, "@Ids List must have a generic component. (%s.%s)",
                        enclosingElement.getQualifiedName(), element.getSimpleName());
                hasError = true;
            } else {
                viewType = typeArguments.get(0);
            }
            kind = FieldCollectionViewInjection.Kind.LIST;
        } else {
            error(element, "@Ids must be a List or array. (%s.%s)",
                    enclosingElement.getQualifiedName(), element.getSimpleName());
            hasError = true;
        }
        if (viewType != null && viewType.getKind() == TypeKind.TYPEVAR) {
            TypeVariable typeVariable = (TypeVariable) viewType;
            viewType = typeVariable.getUpperBound();
        }

        // Verify that the target type extends from View.
        if (viewType != null && !isSubtypeOfType(viewType, VIEW_TYPE) && !isInterface(viewType)) {
            error(element, "@Ids type must extend from View or be an interface. (%s.%s)",
                    enclosingElement.getQualifiedName(), element.getSimpleName());
            hasError = true;
        }

        // Verify common generated code restrictions.
        hasError |= isInaccessibleViaGeneratedCode(Ids.class, "fields", element);
        hasError |= isInjectInWrongPackage(Ids.class, element);

        if (hasError) {
            return;
        }

        // Assemble information on the field.
        String name = element.getSimpleName().toString();
        int[] ids = element.getAnnotation(Ids.class).value();
        if (ids.length == 0) {
            error(element, "@Ids must specify at least one ID. (%s.%s)",
                    enclosingElement.getQualifiedName(), element.getSimpleName());
            return;
        }

        java.lang.Integer duplicateId = findDuplicate(ids);
        if (duplicateId != null) {
            error(element, "@Ids annotation contains duplicate ID %d. (%s.%s)", duplicateId,
                    enclosingElement.getQualifiedName(), element.getSimpleName());
        }

        assert viewType != null; // Always false as hasError would have been true.
        String type = viewType.toString();
        boolean required = isRequiredInjection(element);

        InjectClass injectClass = getOrCreateTargetClass(targetClassMap, enclosingElement);
        FieldCollectionViewInjection inject = new FieldCollectionViewInjection(name, type, kind, required);
        injectClass.addFieldCollection(ids, inject);

        erasedTargetNames.add(enclosingElement.toString());
    }

    private void findAndParseListener(RoundEnvironment env,
                                      Class<? extends Annotation> annotationClass, Map<TypeElement, InjectClass> targetClassMap,
                                      Set<String> erasedTargetNames) {
        for (Element element : env.getElementsAnnotatedWith(annotationClass)) {
            try {
                parseListenerAnnotation(annotationClass, element, targetClassMap, erasedTargetNames);
            } catch (Exception e) {
                StringWriter stackTrace = new StringWriter();
                e.printStackTrace(new PrintWriter(stackTrace));

                error(element, "Unable to generate view Ejector for @%s.\n\n%s",
                        annotationClass.getSimpleName(), stackTrace.toString());
            }
        }
    }

    private String findParentFqcn(TypeElement typeElement, Set<String> parents) {
        TypeMirror type;
        while (true) {
            type = typeElement.getSuperclass();
            if (type.getKind() == TypeKind.NONE) {
                return null;
            }
            typeElement = (TypeElement) ((DeclaredType) type).asElement();
            if (parents.contains(typeElement.toString())) {
                String packageName = getPackageName(typeElement);
                return packageName + "." + getClassName(typeElement, packageName);
            }
        }
    }

    private void parseListenerAnnotation(Class<? extends Annotation> annotationClass, Element element,
                                         Map<TypeElement, InjectClass> targetClassMap, Set<String> erasedTargetNames)
            throws Exception {
        // This should be guarded by the annotation's @Target but it's worth a check for safe casting.
        if (!(element instanceof ExecutableElement) || element.getKind() != METHOD) {
            throw new IllegalStateException(
                    String.format("@%s annotation must be on a method.", annotationClass.getSimpleName()));
        }

        ExecutableElement executableElement = (ExecutableElement) element;
        TypeElement enclosingElement = (TypeElement) element.getEnclosingElement();

        // Assemble information on the method.
        Annotation annotation = element.getAnnotation(annotationClass);
        Method annotationValue = annotationClass.getDeclaredMethod("value");
        if (annotationValue.getReturnType() != int[].class) {
            throw new IllegalStateException(
                    String.format("@%s annotation value() type not int[].", annotationClass));
        }

        int[] ids = (int[]) annotationValue.invoke(annotation);
        String name = executableElement.getSimpleName().toString();
        boolean required = isRequiredInjection(element);

        // Verify that the method and its containing class are accessible via generated code.
        boolean hasError = isInaccessibleViaGeneratedCode(annotationClass, "methods", element);
        hasError |= isInjectInWrongPackage(annotationClass, element);

        java.lang.Integer duplicateId = findDuplicate(ids);
        if (duplicateId != null) {
            error(element, "@%s annotation for method contains duplicate ID %d. (%s.%s)",
                    annotationClass.getSimpleName(), duplicateId, enclosingElement.getQualifiedName(),
                    element.getSimpleName());
            hasError = true;
        }

        Callback listener = annotationClass.getAnnotation(Callback.class);
        if (listener == null) {
            throw new IllegalStateException(
                    String.format("No @%s defined on @%s.", Callback.class.getSimpleName(),
                            annotationClass.getSimpleName()));
        }

        for (int id : ids) {
            if (id == View.NO_ID) {
                if (ids.length == 1) {
                    if (!required) {
                        error(element, "ID free inject must not be annotated with @Nullable. (%s.%s)",
                                enclosingElement.getQualifiedName(), element.getSimpleName());
                        hasError = true;
                    }

                    // Verify target type is valid for a inject without an id.
                    String targetType = listener.target();
                    if (!isSubtypeOfType(enclosingElement.asType(), targetType)
                            && !isInterface(enclosingElement.asType())) {
                        error(element, "@%s annotation without an ID may only be used with an object of type "
                                        + "\"%s\" or an interface. (%s.%s)",
                                annotationClass.getSimpleName(), targetType,
                                enclosingElement.getQualifiedName(), element.getSimpleName());
                        hasError = true;
                    }
                } else {
                    error(element, "@%s annotation contains invalid ID %d. (%s.%s)",
                            annotationClass.getSimpleName(), id, enclosingElement.getQualifiedName(),
                            element.getSimpleName());
                    hasError = true;
                }
            }
        }

        CallMethod method;
        CallMethod[] methods = listener.method();
        if (methods.length > 1) {
            throw new IllegalStateException(String.format("Multiple listener methods specified on @%s.",
                    annotationClass.getSimpleName()));
        } else if (methods.length == 1) {
            if (listener.callbacks() != Callback.NONE.class) {
                throw new IllegalStateException(
                        String.format("Both method() and callback() defined on @%s.",
                                annotationClass.getSimpleName()));
            }
            method = methods[0];
        } else {
            Method annotationCallback = annotationClass.getDeclaredMethod("callback");
            Enum<?> callback = (Enum<?>) annotationCallback.invoke(annotation);
            Field callbackField = callback.getDeclaringClass().getField(callback.name());
            method = callbackField.getAnnotation(CallMethod.class);
            if (method == null) {
                throw new IllegalStateException(
                        String.format("No @%s defined on @%s's %s.%s.", CallMethod.class.getSimpleName(),
                                annotationClass.getSimpleName(), callback.getDeclaringClass().getSimpleName(),
                                callback.name()));
            }
        }

        // Verify that the method has equal to or less than the number of parameters as the listener.
        List<? extends VariableElement> methodParameters = executableElement.getParameters();
        if (methodParameters.size() > method.parameters().length) {
            error(element, "@%s methods can have at most %s parameter(s). (%s.%s)",
                    annotationClass.getSimpleName(), method.parameters().length,
                    enclosingElement.getQualifiedName(), element.getSimpleName());
            hasError = true;
        }

        // Verify method return type matches the listener.
        TypeMirror returnType = executableElement.getReturnType();
        if (returnType instanceof TypeVariable) {
            TypeVariable typeVariable = (TypeVariable) returnType;
            returnType = typeVariable.getUpperBound();
        }
        if (!returnType.toString().equals(method.returnType())) {
            error(element, "@%s methods must have a '%s' return type. (%s.%s)",
                    annotationClass.getSimpleName(), method.returnType(),
                    enclosingElement.getQualifiedName(), element.getSimpleName());
            hasError = true;
        }

        if (hasError) {
            return;
        }

        Parameter[] parameters = Parameter.NONE;
        if (!methodParameters.isEmpty()) {
            parameters = new Parameter[methodParameters.size()];
            BitSet methodParameterUsed = new BitSet(methodParameters.size());
            String[] parameterTypes = method.parameters();
            for (int i = 0; i < methodParameters.size(); i++) {
                VariableElement methodParameter = methodParameters.get(i);
                TypeMirror methodParameterType = methodParameter.asType();
                if (methodParameterType instanceof TypeVariable) {
                    TypeVariable typeVariable = (TypeVariable) methodParameterType;
                    methodParameterType = typeVariable.getUpperBound();
                }

                for (int j = 0; j < parameterTypes.length; j++) {
                    if (methodParameterUsed.get(j)) {
                        continue;
                    }
                    if (isSubtypeOfType(methodParameterType, parameterTypes[j])
                            || isInterface(methodParameterType)) {
                        parameters[i] = new Parameter(j, methodParameterType.toString());
                        methodParameterUsed.set(j);
                        break;
                    }
                }
                if (parameters[i] == null) {
                    StringBuilder builder = new StringBuilder();
                    builder.append("Unable to match @")
                            .append(annotationClass.getSimpleName())
                            .append(" method arguments. (")
                            .append(enclosingElement.getQualifiedName())
                            .append('.')
                            .append(element.getSimpleName())
                            .append(')');
                    for (int j = 0; j < parameters.length; j++) {
                        Parameter parameter = parameters[j];
                        builder.append("\n\n  Parameter #")
                                .append(j + 1)
                                .append(": ")
                                .append(methodParameters.get(j).asType().toString())
                                .append("\n    ");
                        if (parameter == null) {
                            builder.append("did not match any listener parameters");
                        } else {
                            builder.append("matched listener parameter #")
                                    .append(parameter.getListenerPosition() + 1)
                                    .append(": ")
                                    .append(parameter.getType());
                        }
                    }
                    builder.append("\n\nMethods may have up to ")
                            .append(method.parameters().length)
                            .append(" parameter(s):\n");
                    for (String parameterType : method.parameters()) {
                        builder.append("\n  ").append(parameterType);
                    }
                    builder.append(
                            "\n\nThese may be listed in any order but will be searched for from top to bottom.");
                    error(executableElement, builder.toString());
                    return;
                }
            }
        }

        MethodViewInjection inject = new MethodViewInjection(name, Arrays.asList(parameters), required);
        InjectClass injectClass = getOrCreateTargetClass(targetClassMap, enclosingElement);
        for (int id : ids) {
            if (!injectClass.addMethod(id, listener, method, inject)) {
                error(element, "Multiple listener methods with return value specified for ID %d. (%s.%s)",
                        id, enclosingElement.getQualifiedName(), element.getSimpleName());
                return;
            }
        }

        // Add the type-erased version to the valid inject targets set.
        erasedTargetNames.add(enclosingElement.toString());
    }

    private void parseResourceBool(Element element, Map<TypeElement, InjectClass> targetClassMap,
                                   Set<String> erasedTargetNames) {
        boolean hasError = false;
        TypeElement enclosingElement = (TypeElement) element.getEnclosingElement();

        // Verify that the target type is bool.
        if (element.asType().getKind() != TypeKind.BOOLEAN) {
            error(element, "@%s field type must be 'boolean'. (%s.%s)",
                    ResBool.class.getSimpleName(), enclosingElement.getQualifiedName(),
                    element.getSimpleName());
            hasError = true;
        }

        // Verify common generated code restrictions.
        hasError |= isInaccessibleViaGeneratedCode(ResBool.class, "fields", element);
        hasError |= isInjectInWrongPackage(ResBool.class, element);

        if (hasError) {
            return;
        }

        // Assemble information on the field.
        String name = element.getSimpleName().toString();
        int id = element.getAnnotation(ResBool.class).value();

        InjectClass injectClass = getOrCreateTargetClass(targetClassMap, enclosingElement);
        FieldResInjection inject = new FieldResInjection(id, name, "getBoolean");
        injectClass.addResource(inject);

        erasedTargetNames.add(enclosingElement.toString());
    }

    private void parseResourceColor(Element element, Map<TypeElement, InjectClass> targetClassMap,
                                    Set<String> erasedTargetNames) {
        boolean hasError = false;
        TypeElement enclosingElement = (TypeElement) element.getEnclosingElement();

        // Verify that the target type is int or ColorStateList.
        boolean isColorStateList = false;
        TypeMirror elementType = element.asType();
        if (COLOR_STATE_LIST_TYPE.equals(elementType.toString())) {
            isColorStateList = true;
        } else if (elementType.getKind() != TypeKind.INT) {
            error(element, "@%s field type must be 'int' or 'ColorStateList'. (%s.%s)",
                    ResColor.class.getSimpleName(), enclosingElement.getQualifiedName(),
                    element.getSimpleName());
            hasError = true;
        }

        // Verify common generated code restrictions.
        hasError |= isInaccessibleViaGeneratedCode(ResColor.class, "fields", element);
        hasError |= isInjectInWrongPackage(ResColor.class, element);

        if (hasError) {
            return;
        }

        // Assemble information on the field.
        String name = element.getSimpleName().toString();
        int id = element.getAnnotation(ResColor.class).value();

        InjectClass injectClass = getOrCreateTargetClass(targetClassMap, enclosingElement);
        FieldResInjection inject = new FieldResInjection(id, name,
                isColorStateList ? "getColorStateList" : "getColor");
        injectClass.addResource(inject);

        erasedTargetNames.add(enclosingElement.toString());
    }

    private void parseResourceDimen(Element element, Map<TypeElement, InjectClass> targetClassMap,
                                    Set<String> erasedTargetNames) {
        boolean hasError = false;
        TypeElement enclosingElement = (TypeElement) element.getEnclosingElement();

        // Verify that the target type is int or ColorStateList.
        boolean isInt = false;
        TypeMirror elementType = element.asType();
        if (elementType.getKind() == TypeKind.INT) {
            isInt = true;
        } else if (elementType.getKind() != TypeKind.FLOAT) {
            error(element, "@%s field type must be 'int' or 'float'. (%s.%s)",
                    ResDimen.class.getSimpleName(), enclosingElement.getQualifiedName(),
                    element.getSimpleName());
            hasError = true;
        }

        // Verify common generated code restrictions.
        hasError |= isInaccessibleViaGeneratedCode(ResDimen.class, "fields", element);
        hasError |= isInjectInWrongPackage(ResDimen.class, element);

        if (hasError) {
            return;
        }

        // Assemble information on the field.
        String name = element.getSimpleName().toString();
        int id = element.getAnnotation(ResDimen.class).value();

        InjectClass injectClass = getOrCreateTargetClass(targetClassMap, enclosingElement);
        FieldResInjection inject = new FieldResInjection(id, name,
                isInt ? "getDimensionPixelSize" : "getDimension");
        injectClass.addResource(inject);

        erasedTargetNames.add(enclosingElement.toString());
    }

    private void parseResourceDrawable(Element element, Map<TypeElement, InjectClass> targetClassMap,
                                       Set<String> erasedTargetNames) {
        boolean hasError = false;
        TypeElement enclosingElement = (TypeElement) element.getEnclosingElement();

        // Verify that the target type is Drawable.
        if (!DRAWABLE_TYPE.equals(element.asType().toString())) {
            error(element, "@%s field type must be 'Drawable'. (%s.%s)",
                    ResDrawable.class.getSimpleName(), enclosingElement.getQualifiedName(),
                    element.getSimpleName());
            hasError = true;
        }

        // Verify common generated code restrictions.
        hasError |= isInaccessibleViaGeneratedCode(ResDrawable.class, "fields", element);
        hasError |= isInjectInWrongPackage(ResDrawable.class, element);

        if (hasError) {
            return;
        }

        // Assemble information on the field.
        String name = element.getSimpleName().toString();
        int id = element.getAnnotation(ResDrawable.class).value();

        InjectClass injectClass = getOrCreateTargetClass(targetClassMap, enclosingElement);
        FieldResInjection inject = new FieldResInjection(id, name, "getDrawable");
        injectClass.addResource(inject);

        erasedTargetNames.add(enclosingElement.toString());
    }

    private void parseResourceInt(Element element, Map<TypeElement, InjectClass> targetClassMap,
                                  Set<String> erasedTargetNames) {
        boolean hasError = false;
        TypeElement enclosingElement = (TypeElement) element.getEnclosingElement();

        // Verify that the target type is int.
        if (element.asType().getKind() != TypeKind.INT) {
            error(element, "@%s field type must be 'int'. (%s.%s)", ResInt.class.getSimpleName(),
                    enclosingElement.getQualifiedName(), element.getSimpleName());
            hasError = true;
        }

        // Verify common generated code restrictions.
        hasError |= isInaccessibleViaGeneratedCode(ResInt.class, "fields", element);
        hasError |= isInjectInWrongPackage(ResInt.class, element);

        if (hasError) {
            return;
        }

        // Assemble information on the field.
        String name = element.getSimpleName().toString();
        int id = element.getAnnotation(ResInt.class).value();

        InjectClass injectClass = getOrCreateTargetClass(targetClassMap, enclosingElement);
        FieldResInjection inject = new FieldResInjection(id, name, "getInteger");
        injectClass.addResource(inject);

        erasedTargetNames.add(enclosingElement.toString());
    }

    private void parseResourceString(Element element, Map<TypeElement, InjectClass> targetClassMap,
                                     Set<String> erasedTargetNames) {
        boolean hasError = false;
        TypeElement enclosingElement = (TypeElement) element.getEnclosingElement();

        // Verify that the target type is String.
        if (!"java.lang.String".equals(element.asType().toString())) {
            error(element, "@%s field type must be 'String'. (%s.%s)",
                    com.developer.bsince.ioc.ResStr.class.getSimpleName(), enclosingElement.getQualifiedName(),
                    element.getSimpleName());
            hasError = true;
        }

        // Verify common generated code restrictions.
        hasError |= isInaccessibleViaGeneratedCode(com.developer.bsince.ioc.ResStr.class, "fields", element);
        hasError |= isInjectInWrongPackage(com.developer.bsince.ioc.ResStr.class, element);

        if (hasError) {
            return;
        }

        // Assemble information on the field.
        String name = element.getSimpleName().toString();
        int id = element.getAnnotation(com.developer.bsince.ioc.ResStr.class).value();

        InjectClass injectClass = getOrCreateTargetClass(targetClassMap, enclosingElement);
        FieldResInjection inject = new FieldResInjection(id, name, "getString");
        injectClass.addResource(inject);

        erasedTargetNames.add(enclosingElement.toString());
    }

    private boolean isSubtypeOfType(TypeMirror typeMirror, String otherType) {
        if (otherType.equals(typeMirror.toString())) {
            return true;
        }
        if (!(typeMirror instanceof DeclaredType)) {
            return false;
        }
        DeclaredType declaredType = (DeclaredType) typeMirror;
        List<? extends TypeMirror> typeArguments = declaredType.getTypeArguments();
        if (typeArguments.size() > 0) {
            StringBuilder typeString = new StringBuilder(declaredType.asElement().toString());
            typeString.append('<');
            for (int i = 0; i < typeArguments.size(); i++) {
                if (i > 0) {
                    typeString.append(',');
                }
                typeString.append('?');
            }
            typeString.append('>');
            if (typeString.toString().equals(otherType)) {
                return true;
            }
        }
        Element element = declaredType.asElement();
        if (!(element instanceof TypeElement)) {
            return false;
        }
        TypeElement typeElement = (TypeElement) element;
        TypeMirror superType = typeElement.getSuperclass();
        if (isSubtypeOfType(superType, otherType)) {
            return true;
        }
        for (TypeMirror interfaceType : typeElement.getInterfaces()) {
            if (isSubtypeOfType(interfaceType, otherType)) {
                return true;
            }
        }
        return false;
    }
    private boolean isInterface(TypeMirror typeMirror) {
        if (!(typeMirror instanceof DeclaredType)) {
            return false;
        }
        return ((DeclaredType) typeMirror).asElement().getKind() == INTERFACE;
    }
    private boolean isInaccessibleViaGeneratedCode(Class<? extends Annotation> annotationClass,
                                                   String targetThing, Element element) {
        boolean hasError = false;
        TypeElement enclosingElement = (TypeElement) element.getEnclosingElement();

        // Verify method modifiers.
        Set<Modifier> modifiers = element.getModifiers();
        if (modifiers.contains(PRIVATE) || modifiers.contains(STATIC)) {
            error(element, "@%s %s must not be private or static. (%s.%s)",
                    annotationClass.getSimpleName(), targetThing, enclosingElement.getQualifiedName(),
                    element.getSimpleName());
            hasError = true;
        }

        // Verify containing type.
        if (enclosingElement.getKind() != CLASS) {
            error(enclosingElement, "@%s %s may only be contained in classes. (%s.%s)",
                    annotationClass.getSimpleName(), targetThing, enclosingElement.getQualifiedName(),
                    element.getSimpleName());
            hasError = true;
        }

        // Verify containing class visibility is not private.
        if (enclosingElement.getModifiers().contains(PRIVATE)) {
            error(enclosingElement, "@%s %s may not be contained in private classes. (%s.%s)",
                    annotationClass.getSimpleName(), targetThing, enclosingElement.getQualifiedName(),
                    element.getSimpleName());
            hasError = true;
        }

        return hasError;
    }

    private boolean isInjectInWrongPackage(Class<? extends Annotation> annotationClass,
                                            Element element) {
        TypeElement enclosingElement = (TypeElement) element.getEnclosingElement();
        String qualifiedName = enclosingElement.getQualifiedName().toString();

        if (qualifiedName.startsWith(ANDROID_PREFIX)) {
            error(element, "@%s-annotated class incorrectly in Android framework package. (%s)",
                    annotationClass.getSimpleName(), qualifiedName);
            return true;
        }
        if (qualifiedName.startsWith(JAVA_PREFIX)) {
            error(element, "@%s-annotated class incorrectly in Java framework package. (%s)",
                    annotationClass.getSimpleName(), qualifiedName);
            return true;
        }

        return false;
    }
    private static java.lang.Integer findDuplicate(int[] array) {
        Set<java.lang.Integer> seenElements = new LinkedHashSet<java.lang.Integer>();

        for (int element : array) {
            if (!seenElements.add(element)) {
                return element;
            }
        }

        return null;
    }
    private InjectClass getOrCreateTargetClass(Map<TypeElement, InjectClass> targetClassMap,
                                                TypeElement enclosingElement) {
        InjectClass injectClass = targetClassMap.get(enclosingElement);
        if (injectClass == null) {
            String targetType = enclosingElement.getQualifiedName().toString();
            String classPackage = getPackageName(enclosingElement);
            String className = getClassName(enclosingElement, classPackage) + SUFFIX;

            injectClass = new InjectClass(classPackage, className, targetType);
            targetClassMap.put(enclosingElement, injectClass);
        }
        return injectClass;
    }
    private String getPackageName(TypeElement type) {
        return elementUtils.getPackageOf(type).getQualifiedName().toString();
    }
    private static String getClassName(TypeElement type, String packageName) {
        int packageLen = packageName.length() + 1;
        return type.getQualifiedName().toString().substring(packageLen).replace('.', '$');
    }

    private static boolean hasAnnotationWithName(Element element, String simpleName) {
        for (AnnotationMirror mirror : element.getAnnotationMirrors()) {
            String annotationName = mirror.getAnnotationType().asElement().getSimpleName().toString();
            if (simpleName.equals(annotationName)) {
                return true;
            }
        }
        return false;
    }
    private String doubleErasure(TypeMirror elementType) {
        String name = typeUtils.erasure(elementType).toString();
        int typeParamStart = name.indexOf('<');
        if (typeParamStart != -1) {
            name = name.substring(0, typeParamStart);
        }
        return name;
    }

    private static boolean isRequiredInjection(Element element) {
        return !hasAnnotationWithName(element, NULLABLE_ANNOTATION_NAME);
    }

    private void error(Element element, String message, Object... args) {
        if (args.length > 0) {
            message = String.format(message, args);
        }
        processingEnv.getMessager().printMessage(ERROR, message, element);
    }
    private void logParsingError(Element element, Class<? extends Annotation> annotation,
                                 Exception e) {
        StringWriter stackTrace = new StringWriter();
        e.printStackTrace(new PrintWriter(stackTrace));
        error(element, "Unable to parse @%s injecting.\n\n%s", annotation.getSimpleName(), stackTrace);
    }
}
