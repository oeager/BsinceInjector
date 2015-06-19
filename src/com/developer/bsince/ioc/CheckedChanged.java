package com.developer.bsince.ioc;

import android.view.View;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.CLASS;

/**
 * Bind a method to an {@link android.widget.CompoundButton.OnCheckedChangeListener OnCheckedChangeListener} on the view for
 * each ID specified.
 * <pre><code>
 * {@literal @}CheckedChanged(R.id.example) void onChecked(boolean checked) {
 *   Toast.makeText(this, checked ? "Checked!" : "Unchecked!", LENGTH_SHORT).show();
 * }
 * </code></pre>
 * Any number of parameters from
 * {@link android.widget.CompoundButton.OnCheckedChangeListener#onCheckedChanged(android.widget.CompoundButton, boolean)
 * onCheckedChanged} may be used on the method.
 *
 * @see android.widget.CompoundButton.OnCheckedChangeListener
 */
@Target(METHOD)
@Retention(CLASS)
@Callback(
        target = "android.widget.CompoundButton",
        setter = @Setter(name = "setOnCheckedChangeListener",
                type = "android.widget.CompoundButton.OnCheckedChangeListener"),
        method = @CallMethod(
                name = "onCheckedChanged",
                parameters = {
                        "android.widget.CompoundButton",
                        "boolean"
                }
        )
)
public @interface CheckedChanged {
    /**
     * View IDs to which the method will be bound.
     */
    int[] value() default {View.NO_ID};
}
