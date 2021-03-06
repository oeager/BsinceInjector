package com.developer.bsince.ioc.core;

/**
 * Created by oeager on 2015/5/1.
 */
final class FieldResInjection {
    private final int id;
    private final String name;
    private final String method;

    FieldResInjection(int id, String name, String method) {
        this.id = id;
        this.name = name;
        this.method = method;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getMethod() {
        return method;
    }
}
