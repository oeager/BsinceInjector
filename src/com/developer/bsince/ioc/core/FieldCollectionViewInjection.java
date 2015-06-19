package com.developer.bsince.ioc.core;

/**
 * Created by oeager on 2015/5/1.
 */
final class FieldCollectionViewInjection implements ViewInjection{
    enum Kind {
        ARRAY,
        LIST
    }

    private final String name;
    private final String type;
    private final Kind kind;
    private final boolean required;

    FieldCollectionViewInjection(String name, String type, Kind kind, boolean required) {
        this.name = name;
        this.type = type;
        this.kind = kind;
        this.required = required;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public Kind getKind() {
        return kind;
    }

    public boolean isRequired() {
        return required;
    }

    @Override public String toString() {
        return "field '" + name + "'";
    }
}
