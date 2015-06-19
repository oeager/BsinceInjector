package com.developer.bsince.ioc.core;

import static com.developer.bsince.ioc.core.BsinceProcessor.VIEW_TYPE;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import android.view.View;

import com.developer.bsince.ioc.CallMethod;
import com.developer.bsince.ioc.Callback;

/**
 * Created by oeager on 2015/5/1.
 */
final class InjectClass {

	private final Map<Integer, ViewInjections> viewIdMap = new LinkedHashMap<>();
	private final Map<FieldCollectionViewInjection, int[]> collectionInjections = new LinkedHashMap<>();
	private final List<FieldResInjection> resourceInjections = new ArrayList<>();
	private final String classPackage;
	private final String className;
	private final String targetClass;
	private String parentViewEjector;

	InjectClass(String classPackage, String className, String targetClass) {
		this.classPackage = classPackage;
		this.className = className;
		this.targetClass = targetClass;
	}

	void addField(int id, FieldViewInjection injection) {
		getOrCreateViewInjections(id).addFieldInjection(injection);
	}

	void addFieldCollection(int[] ids, FieldCollectionViewInjection injection) {
		collectionInjections.put(injection, ids);
	}

	boolean addMethod(int id, Callback listener, CallMethod method,
			MethodViewInjection injection) {
		ViewInjections viewInjections = getOrCreateViewInjections(id);
		if (viewInjections.hasMethodInject(listener, method)
				&& !"void".equals(method.returnType())) {
			return false;
		}
		viewInjections.addMethodInjection(listener, method, injection);
		return true;
	}

	void addResource(FieldResInjection injection) {
		resourceInjections.add(injection);
	}

	void setParentViewBinder(String parentViewBinder) {
		this.parentViewEjector = parentViewBinder;
	}

	ViewInjections getViewInjection(int id) {
		return viewIdMap.get(id);
	}

	private ViewInjections getOrCreateViewInjections(int id) {
		ViewInjections viewId = viewIdMap.get(id);
		if (viewId == null) {
			viewId = new ViewInjections(id);
			viewIdMap.put(id, viewId);
		}
		return viewId;
	}

	String getFqcn() {
		return classPackage + "." + className;
	}

	String brewJava() {
		StringBuilder builder = new StringBuilder();
		builder.append("// Generated code from BsinceManager. Do not modify!\n");
		builder.append("package ").append(classPackage).append(";\n\n");

		if (!resourceInjections.isEmpty()) {
			builder.append("import android.content.res.Resources;\n");
		}
		if (!viewIdMap.isEmpty() || !collectionInjections.isEmpty()) {
			builder.append("import android.view.View;\n");
		}
		builder.append("import com.developer.bsince.ioc.core.BsinceInject.Finder;\n");
		if (parentViewEjector == null) {
			builder.append("import com.developer.bsince.ioc.core.BsinceInject.Ejector;\n");
		}
		builder.append('\n');

		builder.append("public class ").append(className);
		builder.append("<T extends ").append(targetClass).append(">");

		if (parentViewEjector != null) {
			builder.append(" extends ").append(parentViewEjector).append("<T>");
		} else {
			builder.append(" implements Ejector<T>");
		}
		builder.append(" {\n");

		emitInjectMethod(builder);
		builder.append('\n');
		emitOutPourMethod(builder);

		builder.append("}\n");
		return builder.toString();
	}

	private void emitInjectMethod(StringBuilder builder) {
		builder.append("  @Override ")
				.append("public void inject(final Finder finder, final T target, Object source) {\n");

		// Emit a call to the superclass binder, if any.
		if (parentViewEjector != null) {
			builder.append("    super.inject(finder, target, source);\n\n");
		}

		if (!viewIdMap.isEmpty() || !collectionInjections.isEmpty()) {
			// Local variable in which all views will be temporarily stored.
			builder.append("    View view;\n");

			// Loop over each view bindings and emit it.

			for (ViewInjections injections : viewIdMap.values()) {
				emitViewInjections(builder, injections);
			}

			// Loop over each collection binding and emit it.
			for (Map.Entry<FieldCollectionViewInjection, int[]> entry : collectionInjections
					.entrySet()) {
				emitCollectionInjections(builder, entry.getKey(),
						entry.getValue());
			}
		}

		if (!resourceInjections.isEmpty()) {
			builder.append("    Resources res = finder.getContext(source).getResources();\n");

			for (FieldResInjection injection : resourceInjections) {
				builder.append("    target.").append(injection.getName())
						.append(" = res.").append(injection.getMethod())
						.append('(').append(injection.getId()).append(");\n");
			}
		}

		builder.append("  }\n");
	}

	private void emitCollectionInjections(StringBuilder builder,
			FieldCollectionViewInjection injection, int[] ids) {
		builder.append("    target.").append(injection.getName()).append(" = ");

		switch (injection.getKind()) {
		case ARRAY:
			builder.append("Finder.arrayOf(");
			break;
		case LIST:
			builder.append("Finder.listOf(");
			break;
		default:
			throw new IllegalStateException("Unknown kind: "
					+ injection.getKind());
		}

		for (int i = 0; i < ids.length; i++) {
			if (i > 0) {
				builder.append(',');
			}
			builder.append("\n        finder.<")
					.append(injection.getType())
					.append(">")
					.append(injection.isRequired() ? "findRequiredView"
							: "findOptionalView").append("(source, ")
					.append(ids[i]).append(", \"");
			emitHumanDescription(builder, Collections.singleton(injection));
			builder.append("\")");
		}

		builder.append("\n    );\n");
	}

	private void emitViewInjections(StringBuilder builder,
			ViewInjections injections) {
		builder.append("    view = ");

		List<ViewInjection> requiredViewInjections = injections
				.getRequiredInjections();
		if (requiredViewInjections.isEmpty()) {
			builder.append("finder.findOptionalView(source, ")
					.append(injections.getId()).append(", null);\n");
		} else {
			if (injections.getId() == View.NO_ID) {
				builder.append("target;\n");
			} else {
				builder.append("finder.findRequiredView(source, ")
						.append(injections.getId()).append(", \"");
				emitHumanDescription(builder, requiredViewInjections);
				builder.append("\");\n");
			}
		}

		emitFieldInjections(builder, injections);
		emitMethodInjections(builder, injections);
	}

	private void emitFieldInjections(StringBuilder builder,
			ViewInjections injections) {
		Collection<FieldViewInjection> fieldBindings = injections
				.getFieldInjections();
		if (fieldBindings.isEmpty()) {
			return;
		}

		for (FieldViewInjection fieldBinding : fieldBindings) {
			builder.append("    target.").append(fieldBinding.getName())
					.append(" = ");
			if (fieldBinding.requiresCast()) {
				builder.append("finder.castView(view").append(", ")
						.append(injections.getId()).append(", \"");
				emitHumanDescription(builder, fieldBindings);
				builder.append("\");\n");
			} else {
				builder.append("view;\n");
			}
		}
	}

	private void emitMethodInjections(StringBuilder builder,
			ViewInjections injections) {
		Map<Callback, Map<CallMethod, Set<MethodViewInjection>>> classMethodBindings = injections
				.getMethodInjections();
		if (classMethodBindings.isEmpty()) {
			return;
		}

		String extraIndent = "";

		// We only need to emit the null check if there are zero required
		// bindings.
		boolean needsNullChecked = injections.getRequiredInjections().isEmpty();
		if (needsNullChecked) {
			builder.append("    if (view != null) {\n");
			extraIndent = "  ";
		}

		for (Map.Entry<Callback, Map<CallMethod, Set<MethodViewInjection>>> e : classMethodBindings
				.entrySet()) {
			Callback listener = e.getKey();
			Map<CallMethod, Set<MethodViewInjection>> methodInjections = e
					.getValue();

			// Emit: ((OWNER_TYPE) view).SETTER_NAME(
			boolean needsCast = !VIEW_TYPE.equals(listener.target());
			builder.append(extraIndent).append("    ");
			if (needsCast) {
				builder.append("((").append(listener.target());
				if (listener.genericArguments() > 0) {
					builder.append('<');
					for (int i = 0; i < listener.genericArguments(); i++) {
						if (i > 0) {
							builder.append(", ");
						}
						builder.append('?');
					}
					builder.append('>');
				}
				builder.append(") ");
			}
			builder.append("view");
			if (needsCast) {
				builder.append(')');
			}
			builder.append('.').append(listener.setter().name()).append("(\n");

			// Emit: new TYPE() {
			builder.append(extraIndent).append("      new ")
					.append(listener.setter().type()).append("() {\n");

			for (CallMethod method : getListenerMethods(listener)) {
				// Emit: @Override public RETURN_TYPE METHOD_NAME(
				builder.append(extraIndent).append("        @Override public ")
						.append(method.returnType()).append(' ')
						.append(method.name()).append("(\n");

				// Emit listener method arguments, each on their own line.
				String[] parameterTypes = method.parameters();
				for (int i = 0, count = parameterTypes.length; i < count; i++) {
					builder.append(extraIndent).append("          ")
							.append(parameterTypes[i]).append(" p").append(i);
					if (i < count - 1) {
						builder.append(',');
					}
					builder.append('\n');
				}

				// Emit end of parameters, start of body.
				builder.append(extraIndent).append("        ) {\n");

				// Set up the return statement, if needed.
				builder.append(extraIndent).append("          ");
				boolean hasReturnType = !"void".equals(method.returnType());
				if (hasReturnType) {
					builder.append("return ");
				}

				if (methodInjections.containsKey(method)) {
					Set<MethodViewInjection> set = methodInjections.get(method);
					Iterator<MethodViewInjection> iterator = set.iterator();

					while (iterator.hasNext()) {
						MethodViewInjection injection = iterator.next();
						builder.append("target.").append(injection.getName())
								.append('(');
						List<Parameter> parameters = injection.getParameters();
						String[] listenerParameters = method.parameters();
						for (int i = 0, count = parameters.size(); i < count; i++) {
							Parameter parameter = parameters.get(i);
							int listenerPosition = parameter
									.getListenerPosition();

							if (parameter
									.requiresCast(listenerParameters[listenerPosition])) {
								builder.append("finder.<")
										.append(parameter.getType())
										.append(">castParam(p")
										.append(listenerPosition)
										.append(", \"").append(method.name())
										.append("\", ")
										.append(listenerPosition)
										.append(", \"")
										.append(injection.getName())
										.append("\", ").append(i).append(")");
							} else {
								builder.append('p').append(listenerPosition);
							}

							if (i < count - 1) {
								builder.append(", ");
							}
						}
						builder.append(");");
						if (iterator.hasNext()) {
							builder.append("\n").append("          ");
						}
					}
				} else if (hasReturnType) {
					builder.append(method.defaultReturn()).append(';');
				}
				builder.append('\n');

				// Emit end of listener method.
				builder.append(extraIndent).append("        }\n");
			}

			// Emit end of listener class body and close the setter method call.
			builder.append(extraIndent).append("      });\n");
		}

		if (needsNullChecked) {
			builder.append("    }\n");
		}
	}

	static List<CallMethod> getListenerMethods(Callback listener) {
		if (listener.method().length == 1) {
			return Arrays.asList(listener.method());
		}

		try {
			List<CallMethod> methods = new ArrayList<>();
			Class<? extends Enum<?>> callbacks = listener.callbacks();
			for (Enum<?> callbackMethod : callbacks.getEnumConstants()) {
				Field callbackField = callbacks.getField(callbackMethod.name());
				CallMethod method = callbackField
						.getAnnotation(CallMethod.class);
				if (method == null) {
					throw new IllegalStateException(String.format(
							"@%s's %s.%s missing @%s annotation.", callbacks
									.getEnclosingClass().getSimpleName(),
							callbacks.getSimpleName(), callbackMethod.name(),
							CallMethod.class.getSimpleName()));
				}
				methods.add(method);
			}
			return methods;
		} catch (NoSuchFieldException e) {
			throw new AssertionError(e);
		}
	}

	private void emitOutPourMethod(StringBuilder builder) {
		builder.append("  @Override public void outPour(T target) {\n");
		if (parentViewEjector != null) {
			builder.append("    super.outPour(target);\n\n");
		}

		
		for (ViewInjections injections : viewIdMap.values()) {
			for (FieldViewInjection fieldViewInjection : injections
					.getFieldInjections()) {
				builder.append("    target.")
						.append(fieldViewInjection.getName())
						.append(" = null;\n");
			}
		}

		for (FieldCollectionViewInjection fieldCollectionViewInjection : collectionInjections
				.keySet()) {
			builder.append("    target.")
					.append(fieldCollectionViewInjection.getName())
					.append(" = null;\n");
		}
		builder.append("  }\n");
	}

	static void emitHumanDescription(StringBuilder builder,
			Collection<? extends ViewInjection> bindings) {
		Iterator<? extends ViewInjection> iterator = bindings.iterator();
		switch (bindings.size()) {
		case 1:
			builder.append(iterator.next().toString());
			break;
		case 2:
			builder.append(iterator.next().toString()).append(" and ")
					.append(iterator.next().toString());
			break;
		default:
			for (int i = 0, count = bindings.size(); i < count; i++) {
				if (i != 0) {
					builder.append(", ");
				}
				if (i == count - 1) {
					builder.append("and ");
				}
				builder.append(iterator.next().toString());
			}
			break;
		}
	}
}
