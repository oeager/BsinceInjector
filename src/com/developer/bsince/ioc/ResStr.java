package com.developer.bsince.ioc;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import android.support.annotation.StringRes;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.CLASS;

/**
 * Bind a field to the specified string resource ID.
 * 
 * <pre>
 * <code>
 * {@literal @}ResourceString(R.string.username_error) String usernameErrorText;
 * </code>
 * </pre>
 */
@Retention(CLASS)
@Target(FIELD)
public @interface ResStr {
	/** String resource ID to which the field will be bound. */
	@StringRes
	int value();
}
