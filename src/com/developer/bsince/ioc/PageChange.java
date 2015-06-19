package com.developer.bsince.ioc;

import android.view.View;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.CLASS;

/**
 * Bind a method to an {@code OnPageChangeListener} on the view for each ID specified.
 * <pre><code>
 * {@literal @}PageChange(R.id.example_pager) void onPageSelected(int position) {
 *   Toast.makeText(this, "Selected " + position + "!", LENGTH_SHORT).show();
 * }
 * </code></pre>
 * Any number of parameters from {@code onPageSelected} may be used on the method.
 * <p/>
 * To bind to methods other than {@code onPageSelected}, specify a different {@code callback}.
 * <pre><code>
 * {@literal @}PageChange(value = R.id.example_pager, callback = PAGE_SCROLL_STATE_CHANGED)
 * void onPageStateChanged(int state) {
 *   Toast.makeText(this, "State changed: " + state + "!", LENGTH_SHORT).show();
 * }
 * </code></pre>
 */
@Target(METHOD)
@Retention(CLASS)
@Callback(
        target = "android.support.v4.view.ViewPager",
        setter = @Setter(name = "setOnPageChangeListener",
                type = "android.support.v4.view.ViewPager.OnPageChangeListener")
        ,
        callbacks = PageChange.Callback.class
)
public @interface PageChange {
    /**
     * View IDs to which the method will be bound.
     */
    int[] value() default {View.NO_ID};

    /**
     * Listener callback to which the method will be bound.
     */
    Callback callback() default Callback.PAGE_SELECTED;

    /**
     * {@code ViewPager.OnPageChangeListener} callback methods.
     */
    enum Callback {
        /**
         * {@code onPageSelected(int)}
         */
        @CallMethod(
                name = "onPageSelected",
                parameters = "int"
        )
        PAGE_SELECTED,

        /**
         * {@code onPageScrolled(int, float, int)}
         */
        @CallMethod(
                name = "onPageScrolled",
                parameters = {
                        "int",
                        "float",
                        "int"
                }
        )
        PAGE_SCROLLED,

        /**
         * {@code onPageScrollStateChanged(int)}
         */
        @CallMethod(
                name = "onPageScrollStateChanged",
                parameters = "int"
        )
        PAGE_SCROLL_STATE_CHANGED,
    }
}
