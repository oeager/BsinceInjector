package com.developer.bsince.ioc;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by oeager on 2015/5/1.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.ANNOTATION_TYPE)
public @interface Setter {
    /** Name of the setter method  for the listener. */
    java.lang.String name();

    /** Fully-qualified class name of the listener type. */
    java.lang.String type();
}
