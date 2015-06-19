package com.developer.bsince.ioc;

import android.view.View;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.CLASS;

/**
 * Bind a method to an {@link android.text.TextWatcher TextWatcher} on the view for each ID specified.
 * <pre><code>
 * {@literal @}TextChanged(R.id.example) void onTextChanged(CharSequence text) {
 *   Toast.makeText(this, "Text changed: " + text, LENGTH_SHORT).show();
 * }
 * </code></pre>
 * Any number of parameters from {@link android.text.TextWatcher#onTextChanged(CharSequence, int, int, int)
 * onTextChanged} may be used on the method.
 * <p/>
 * To bind to methods other than {@code onTextChanged}, specify a different {@code callback}.
 * <pre><code>
 * {@literal @}TextChanged(value = R.id.example, callback = BEFORE_TEXT_CHANGED)
 * void onBeforeTextChanged(CharSequence text) {
 *   Toast.makeText(this, "Before text changed: " + text, LENGTH_SHORT).show();
 * }
 * </code></pre>
 *
 * @see android.text.TextWatcher
 */
@Target(METHOD)
@Retention(CLASS)
@Callback(
        target = "android.widget.TextView",
        setter = @Setter(name = "addTextChangedListener",
                type = "android.text.TextWatcher")
        ,
        callbacks = TextChanged.Callback.class
)
public @interface TextChanged {
    /**
     * View IDs to which the method will be bound.
     */
    int[] value() default {View.NO_ID};

    /**
     * Listener callback to which the method will be bound.
     */
    Callback callback() default Callback.TEXT_CHANGED;

    /**
     * {@link android.text.TextWatcher} callback methods.
     */
    enum Callback {
        /**
         * {@link android.text.TextWatcher#onTextChanged(CharSequence, int, int, int)}
         */
        @CallMethod(
                name = "onTextChanged",
                parameters = {
                        "java.lang.CharSequence",
                        "int",
                        "int",
                        "int"
                }
        )
        TEXT_CHANGED,

        /**
         * {@link android.text.TextWatcher#beforeTextChanged(CharSequence, int, int, int)}
         */
        @CallMethod(
                name = "beforeTextChanged",
                parameters = {
                        "java.lang.CharSequence",
                        "int",
                        "int",
                        "int"
                }
        )
        BEFORE_TEXT_CHANGED,

        /**
         * {@link android.text.TextWatcher#afterTextChanged(android.text.Editable)}
         */
        @CallMethod(
                name = "afterTextChanged",
                parameters = "android.text.Editable"
        )
        AFTER_TEXT_CHANGED,
    }
}
