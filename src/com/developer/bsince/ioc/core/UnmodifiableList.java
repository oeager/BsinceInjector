package com.developer.bsince.ioc.core;

import java.util.AbstractList;
import java.util.RandomAccess;

/**
 * Created by oeager on 2015/5/1.
 */
public class UnmodifiableList<T> extends AbstractList<T> implements RandomAccess {
    private final T[] views;

    UnmodifiableList(T[] views) {
        this.views = views;
    }

    @Override public T get(int index) {
        return views[index];
    }

    @Override public int size() {
        return views.length;
    }

    @Override public boolean contains(Object o) {
        for (T view : views) {
            if (view == o) {
                return true;
            }
        }
        return false;
    }
}
