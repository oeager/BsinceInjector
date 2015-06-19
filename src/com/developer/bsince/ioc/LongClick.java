package com.developer.bsince.ioc;

import android.view.View;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.CLASS;

/**
 * Bind a method to an {@link android.view.View.OnLongClickListener OnLongClickListener} on the view for each ID
 * specified.
 * <pre><code>
 * {@literal @}LongClick(R.id.example) boolean onLongClick() {
 *   Toast.makeText(this, "Long clicked!", LENGTH_SHORT).show();
 *   return true;
 * }
 * </code></pre>
 * Any number of parameters from {@link android.view.View.OnLongClickListener#onLongClick(android.view.View)} may be
 * used on the method.
 *
 * @see android.view.View.OnLongClickListener
 */
@Retention(CLASS) @Target(METHOD)
@Callback(
    target = "android.view.View",
    setter = @Setter(name = "setOnLongClickListener",
            type = "android.view.View.OnLongClickListener"),
    method = @CallMethod(
        name = "onLongClick",
        parameters = {
            "android.view.View"
        },
        returnType = "boolean",
        defaultReturn = "false"
    )
)
public @interface LongClick {
  /** View IDs to which the method will be bound. */
  int[] value() default { View.NO_ID };
}
