package com.developer.bsince.ioc;

import android.view.View;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.CLASS;

/**
 * Bind a method to an {@link android.view.View.OnTouchListener OnTouchListener} on the view for each ID specified.
 * <pre><code>
 * {@literal @}OnTouch(R.id.example) boolean onTouch() {
 *   Toast.makeText(this, "Touched!", LENGTH_SHORT).show();
 *   return false;
 * }
 * </code></pre>
 * Any number of parameters from
 * {@link android.view.View.OnTouchListener#onTouch(android.view.View, android.view.MotionEvent) onTouch} may be used
 * on the method.
 *
 * @see android.view.View.OnTouchListener
 */
@Target(METHOD)
@Retention(CLASS)
@Callback(
        target = "android.view.View",
        setter = @Setter(name = "setOnTouchListener",
                type = "android.view.View.OnTouchListener")
        ,
        method = @CallMethod(
                name = "onTouch",
                parameters = {
                        "android.view.View",
                        "android.view.MotionEvent"
                },
                returnType = "boolean",
                defaultReturn = "false"
        )
)
public @interface Touch {
    /**
     * View IDs to which the method will be bound.
     */
    int[] value() default {View.NO_ID};
}
