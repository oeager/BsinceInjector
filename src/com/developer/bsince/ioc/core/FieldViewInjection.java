package com.developer.bsince.ioc.core;
import static com.developer.bsince.ioc.core.BsinceProcessor.VIEW_TYPE;

/**
 * Created by oeager on 2015/5/1.
 */
final class FieldViewInjection implements ViewInjection{

    private final String name;
    private final String type;
    private final boolean required;
    FieldViewInjection(String name, String type, boolean required) {
        this.name = name;
        this.type = type;
        this.required = required;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public boolean isRequired() {
        return required;
    }
    public boolean requiresCast() {
        return !VIEW_TYPE.equals(type);
    }

    @Override
    public String toString() {
        return "field '" + name + "'";
    }
}
