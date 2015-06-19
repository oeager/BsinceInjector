package com.developer.bsince.ioc;

import android.view.View;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.CLASS;

/**
 * Bind a method to an {@link android.widget.AdapterView.OnItemSelectedListener OnItemSelectedListener} on the view for each
 * ID specified.
 * <pre><code>
 * {@literal @}ItemSelected(R.id.example_list) void onItemSelected(int position) {
 *   Toast.makeText(this, "Selected position " + position + "!", LENGTH_SHORT).show();
 * }
 * </code></pre>
 * Any number of parameters from
 * {@link android.widget.AdapterView.OnItemSelectedListener#onItemSelected(android.widget.AdapterView, android.view.View, int,
 * long) onItemSelected} may be used on the method.
 * <p/>
 * To bind to methods other than {@code onItemSelected}, specify a different {@code callback}.
 * <pre><code>
 * {@literal @}ItemSelected(value = R.id.example_list, callback = NOTHING_SELECTED)
 * void onNothingSelected() {
 *   Toast.makeText(this, "Nothing selected!", LENGTH_SHORT).show();
 * }
 * </code></pre>
 *
 * @see android.widget.AdapterView.OnItemSelectedListener
 */
@Target(METHOD)
@Retention(CLASS)
@Callback(
        target = "android.widget.AdapterView<?>",
        setter = @Setter(name = "setOnItemSelectedListener",
                type = "android.widget.AdapterView.OnItemSelectedListener")
        ,
        callbacks = ItemSelected.Callback.class
)
public @interface ItemSelected {
    /**
     * View IDs to which the method will be bound.
     */
    int[] value() default {View.NO_ID};

    /**
     * Listener callback to which the method will be bound.
     */
    Callback callback() default Callback.ITEM_SELECTED;

    /**
     * {@link android.widget.AdapterView.OnItemSelectedListener} callback methods.
     */
    enum Callback {
        /**
         * {@link android.widget.AdapterView.OnItemSelectedListener#onItemSelected(android.widget.AdapterView, android.view.View,
         * int, long)}
         */
        @CallMethod(
                name = "onItemSelected",
                parameters = {
                        "android.widget.AdapterView<?>",
                        "android.view.View",
                        "int",
                        "long"
                }
        )
        ITEM_SELECTED,

        /**
         * {@link android.widget.AdapterView.OnItemSelectedListener#onNothingSelected(android.widget.AdapterView)}
         */
        @CallMethod(
                name = "onNothingSelected",
                parameters = "android.widget.AdapterView<?>"
        )
        NOTHING_SELECTED
    }
}
