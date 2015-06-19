package com.developer.bsince.ioc;

import android.view.View;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.CLASS;

/**
 * Bind a method to an {@link android.widget.AdapterView.OnItemLongClickListener OnItemLongClickListener} on the view for each
 * ID specified.
 * <pre><code>
 * {@literal @}ItemLongClick(R.id.example_list) boolean onItemLongClick(int position) {
 *   Toast.makeText(this, "Long clicked position " + position + "!", LENGTH_SHORT).show();
 *   return true;
 * }
 * </code></pre>
 * Any number of parameters from
 * {@link android.widget.AdapterView.OnItemLongClickListener#onItemLongClick(android.widget.AdapterView, android.view.View,
 * int, long) onItemLongClick} may be used on the method.
 *
 * @see android.widget.AdapterView.OnItemLongClickListener
 */
@Target(METHOD)
@Retention(CLASS)
@Callback(
        target = "android.widget.AdapterView<?>",
        setter = @Setter(name = "setOnItemLongClickListener",
                type = "android.widget.AdapterView.OnItemLongClickListener")
        ,
        method = @CallMethod(
                name = "onItemLongClick",
                parameters = {
                        "android.widget.AdapterView<?>",
                        "android.view.View",
                        "int",
                        "long"
                },
                returnType = "boolean",
                defaultReturn = "false"
        )
)
public @interface ItemLongClick {
    /**
     * View IDs to which the method will be bound.
     */
    int[] value() default {View.NO_ID};
}
