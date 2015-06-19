package com.developer.bsince.ioc;

import android.view.View;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.CLASS;

/**
 * Bind a method to an {@link android.view.View.OnClickListener OnClickListener} on the view for each ID specified.
*/
@Target(METHOD)
@Retention(CLASS)
@Callback(
        target = "android.view.View",
        setter = @Setter(name = "setOnClickListener", type = "android.view.View.OnClickListener"),
        method = @CallMethod(name = "onClick", parameters = "android.view.View")

)
public @interface Click {
    /**
     * View IDs to which the method will be bound.
     */
    int[] value() default {View.NO_ID};
}
