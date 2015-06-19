package com.developer.bsince.ioc;

import android.view.View;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.CLASS;

/**
 * Bind a method to an {@link android.widget.TextView.OnEditorActionListener OnEditorActionListener} on the view for each
 * ID specified.
 * <pre><code>
 * {@literal @}EditorAction(R.id.example) boolean onEditorAction(KeyEvent key) {
 *   Toast.makeText(this, "Pressed: " + key, LENGTH_SHORT).show();
 *   return true;
 * }
 * </code></pre>
 * Any number of parameters from
 * {@link android.widget.TextView.OnEditorActionListener#onEditorAction(android.widget.TextView, int, android.view.KeyEvent)
 * onEditorAction} may be used on the method.
 *
 * @see android.widget.TextView.OnEditorActionListener
 */
@Target(METHOD)
@Retention(CLASS)
@Callback(target = "android.widget.TextView",
        setter = @Setter(name = "setOnEditorActionListener", type = "android.widget.TextView.OnEditorActionListener"),
        method = @CallMethod(name = "onEditorAction",
                parameters = {
                        "android.widget.TextView",
                        "int",
                        "android.view.KeyEvent"
                },
                returnType = "boolean",
                defaultReturn = "false"))
public @interface EditorAction {
    /**
     * View IDs to which the method will be bound.
     */
    int[] value() default {View.NO_ID};
}
