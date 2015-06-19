package com.developer.bsince.ioc;


/**
 * Created by oeager on 2015/5/1.
 */
public @interface CallMethod {
    /** Name of the listener method for which this annotation applies. */
    java.lang.String name();

    /** List of method parameters. If the type is not a primitive it must be fully-qualified. */
    java.lang.String[] parameters() default { };

    /** Primative or fully-qualified return type of the listener method. May also be {@code void}. */
    java.lang.String returnType() default "void";

    /** If {@link #returnType()} is not {@code void} this value is returned when no binding exists. */
    java.lang.String defaultReturn() default "null";
}
