package com.developer.bsince.ioc;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import android.support.annotation.IdRes;

/**
 * Created by oeager on 2015/5/1.
 */
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.FIELD)
public @interface Id {

	@IdRes
    int value();
}
