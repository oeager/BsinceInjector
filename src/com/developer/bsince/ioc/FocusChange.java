package com.developer.bsince.ioc;

import android.view.View;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.CLASS;

/**
 * Bind a method to an {@link android.view.View.OnFocusChangeListener OnFocusChangeListener} on the view for each ID
 * specified.
 * <pre><code>
 * {@literal @}FocusChange(R.id.example) void onFocusChanged(boolean focused) {
 *   Toast.makeText(this, focused ? "Gained focus" : "Lost focus", LENGTH_SHORT).show();
 * }
 * </code></pre>
 * Any number of parameters from {@link android.view.View.OnFocusChangeListener#onFocusChange(android.view.View,
 * boolean) onFocusChange} may be used on the method.
 *
 * @see android.view.View.OnFocusChangeListener
 */
@Target(METHOD)
@Retention(CLASS)
@Callback(
        target = "android.view.View",
        setter = @Setter(name = "setOnFocusChangeListener",
                type = "android.view.View.OnFocusChangeListener")
        ,
        method = @CallMethod(
                name = "onFocusChange",
                parameters = {
                        "android.view.View",
                        "boolean"
                }
        )
)
public @interface FocusChange {
    /**
     * View IDs to which the method will be bound.
     */
    int[] value() default {View.NO_ID};
}
