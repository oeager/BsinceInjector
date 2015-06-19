package com.developer.bsince.ioc.core;

import com.developer.bsince.ioc.CallMethod;
import com.developer.bsince.ioc.Callback;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by oeager on 2015/5/1.
 */
final class ViewInjections {
    private final int id;
    private final Set<FieldViewInjection> fieldInjections = new LinkedHashSet<>();
    private final LinkedHashMap<Callback,Map<CallMethod,Set<MethodViewInjection>>> methodInjections = new LinkedHashMap<>();


    ViewInjections(int id){

        this.id = id;
    }
    public int getId() {
        return id;
    }
    public Set<FieldViewInjection> getFieldInjections() {
        return fieldInjections;
    }

    public LinkedHashMap<Callback, Map<CallMethod, Set<MethodViewInjection>>> getMethodInjections() {
        return methodInjections;
    }

    public boolean hasMethodInject(Callback callback,CallMethod method){

        Map<CallMethod, Set<MethodViewInjection>> methods = methodInjections.get(callback);
        return methods != null && methods.containsKey(method);
    }

    public void addMethodInjection(Callback callback,CallMethod method,MethodViewInjection injection){
     Map<CallMethod,Set<MethodViewInjection>> methods =  methodInjections.get(callback);
        Set<MethodViewInjection> set = null;
        if (methods == null) {
            methods = new LinkedHashMap<>();
            methodInjections.put(callback, methods);
        } else {
            set = methods.get(method);
        }
        if (set == null) {
            set = new LinkedHashSet<>();
            methods.put(method, set);
        }
        set.add(injection);
    }

    public void addFieldInjection(FieldViewInjection fieldViewInjection) {
        fieldInjections.add(fieldViewInjection);
    }

    public List<ViewInjection> getRequiredInjections() {
        List<ViewInjection> requiredViewInjections = new ArrayList<>();
        for (FieldViewInjection fieldInjection : fieldInjections) {
            if (fieldInjection.isRequired()) {
                requiredViewInjections.add(fieldInjection);
            }
        }
        for (Map<CallMethod, Set<MethodViewInjection>> methodBinding : methodInjections.values()) {
            for (Set<MethodViewInjection> set : methodBinding.values()) {
                for (MethodViewInjection binding : set) {
                    if (binding.isRequired()) {
                        requiredViewInjections.add(binding);
                    }
                }
            }
        }
        return requiredViewInjections;
    }
}
