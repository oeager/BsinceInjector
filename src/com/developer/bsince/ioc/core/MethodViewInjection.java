package com.developer.bsince.ioc.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by oeager on 2015/5/1.
 */
final class MethodViewInjection implements ViewInjection{
    private final String name;
    private final List<Parameter> parameters;
    private final boolean required;

    MethodViewInjection(String name, List<Parameter> parameters, boolean required) {
        this.name = name;
        this.parameters = Collections.unmodifiableList(new ArrayList<Parameter>(parameters));
        this.required = required;
    }

    public String getName() {
        return name;
    }

    public List<Parameter> getParameters() {
        return parameters;
    }

    @Override public String toString() {
        return "method '" + name + "'";
    }

    public boolean isRequired() {
        return required;
    }
}
